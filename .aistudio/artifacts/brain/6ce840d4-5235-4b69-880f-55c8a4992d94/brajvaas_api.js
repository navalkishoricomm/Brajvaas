/**
 * Brajvaas Central Backend API Server (Port 5004)
 * Zero external npm dependencies — runs on standard Node.js (v14+).
 * 
 * PERSISTENT DATABASE:
 * Stores data safely at /var/data/brajvaas_db.json (outside /var/www/brajvaas).
 * Uploading or replacing frontend HTML files will NEVER wipe or reset this database!
 * 
 * Usage on VPS:
 *   node brajvaas_api.js
 * Or with PM2 (recommended):
 *   pm2 start brajvaas_api.js --name brajvaas
 *   pm2 save
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 5004;

// Determine safe storage directory (outside web root so uploads don't delete data)
function resolveDbPath() {
  const preferredDirs = ['/var/data', '/var/brajvaas_data', path.join(__dirname, '..', 'brajvaas_data')];
  for (const dir of preferredDirs) {
    try {
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }
      return path.join(dir, 'brajvaas_db.json');
    } catch (e) {
      // try next
    }
  }
  return path.join(__dirname, 'brajvaas_db.json');
}

const DB_PATH = resolveDbPath();
const BACKUP_PATH = DB_PATH + '.bak';

console.log(`[Brajvaas API] Persistent Database location: ${DB_PATH}`);

// Initial database schema (Starts with 0 entries as requested, visitorCount starts at 101)
const defaultDb = {
  properties: [],
  agreements: [],
  leads: [],
  users: [],
  shortStays: [],
  visitorCount: 101,
  version: '2026.10',
  lastUpdated: Date.now()
};

let db = { ...defaultDb };

// Load database from disk
function loadDb() {
  try {
    if (fs.existsSync(DB_PATH)) {
      const data = fs.readFileSync(DB_PATH, 'utf8');
      const parsed = JSON.parse(data);
      db = {
        properties: Array.isArray(parsed.properties) ? parsed.properties : [],
        agreements: Array.isArray(parsed.agreements) ? parsed.agreements : [],
        leads: Array.isArray(parsed.leads) ? parsed.leads : [],
        users: Array.isArray(parsed.users) ? parsed.users : [],
        shortStays: Array.isArray(parsed.shortStays) ? parsed.shortStays : [],
        visitorCount: (typeof parsed.visitorCount === 'number' && parsed.visitorCount >= 101) ? parsed.visitorCount : 101,
        version: parsed.version || '2026.10',
        lastUpdated: parsed.lastUpdated || Date.now()
      };
      console.log(`[Brajvaas API] Loaded ${db.properties.length} properties, ${db.agreements.length} agreements, visitorCount=${db.visitorCount} from disk.`);
    } else {
      saveDb();
    }
  } catch (err) {
    console.error(`[Brajvaas API] Error reading ${DB_PATH}:`, err.message);
    // try to load backup
    try {
      if (fs.existsSync(BACKUP_PATH)) {
        const bakData = fs.readFileSync(BACKUP_PATH, 'utf8');
        db = JSON.parse(bakData);
        console.log(`[Brajvaas API] Restored from backup ${BACKUP_PATH}`);
      }
    } catch (_) {}
  }
}

// Save database to disk atomically
function saveDb() {
  try {
    db.lastUpdated = Date.now();
    const jsonStr = JSON.stringify(db, null, 2);
    // atomic write
    const tempPath = DB_PATH + '.tmp';
    fs.writeFileSync(tempPath, jsonStr, 'utf8');
    if (fs.existsSync(DB_PATH)) {
      try { fs.copyFileSync(DB_PATH, BACKUP_PATH); } catch (_) {}
    }
    fs.renameSync(tempPath, DB_PATH);
  } catch (err) {
    console.error(`[Brajvaas API] Error saving database to ${DB_PATH}:`, err.message);
  }
}

// Initial load
loadDb();

// Helper to send JSON responses with CORS headers
function sendJson(res, statusCode, data) {
  const json = JSON.stringify(data);
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Requested-With',
    'Cache-Control': 'no-cache'
  });
  res.end(json);
}

// Helper to parse JSON body
function parseBody(req) {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
      if (body.length > 50 * 1024 * 1024) { // 50MB limit for photos
        req.destroy();
      }
    });
    req.on('end', () => {
      if (!body) return resolve({});
      try {
        resolve(JSON.parse(body));
      } catch (e) {
        // Fallback for form-urlencoded
        const params = new URLSearchParams(body);
        const obj = {};
        for (const [k, v] of params.entries()) obj[k] = v;
        resolve(obj);
      }
    });
  });
}

// Main HTTP request handler
const server = http.createServer(async (req, res) => {
  // CORS Preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Requested-With'
    });
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname.replace(/\/+$/, '') || '/';
  const query = parsedUrl.query;
  const method = req.method.toUpperCase();

  console.log(`[Brajvaas API] ${method} ${pathname}`);

  try {
    // 1. Health check
    if (pathname === '/health' || pathname === '/api/health') {
      return sendJson(res, 200, { 
        status: 'healthy', 
        version: db.version, 
        propertiesCount: db.properties.length,
        visitorCount: db.visitorCount || 101
      });
    }

    // 2. Full data snapshot for Web Console
    if ((pathname === '/api/data' || pathname === '/data') && method === 'GET') {
      return sendJson(res, 200, {
        properties: db.properties,
        agreements: db.agreements,
        leads: db.leads,
        users: db.users,
        shortStays: db.shortStays,
        visitorCount: db.visitorCount || 101,
        lastUpdated: db.lastUpdated
      });
    }

    // 2b. Live Site Visitor Counter (starts at 101, increments on visit)
    if (pathname === '/api/visitors' || pathname === '/visitors') {
      if (query.peek === 'true' || method === 'HEAD') {
        // Just peek without incrementing
        return sendJson(res, 200, { count: db.visitorCount || 101 });
      }
      // Increment visitor count
      db.visitorCount = (typeof db.visitorCount === 'number' && db.visitorCount >= 101) ? db.visitorCount + 1 : 101;
      saveDb();
      return sendJson(res, 200, { success: true, count: db.visitorCount });
    }

    // 3. Properties endpoints
    if (pathname === '/api/properties' || pathname === '/properties') {
      if (method === 'GET') {
        const status = query.status;
        const availability = query.availability;
        let list = db.properties;
        if (status) {
          list = list.filter(p => p.approvalStatus === status);
        }
        if (availability) {
          list = list.filter(p => p.availabilityStatus === availability);
        }
        return sendJson(res, 200, { success: true, count: list.length, properties: list });
      }

      if (method === 'POST') {
        const body = await parseBody(req);
        if (!body.title) {
          return sendJson(res, 400, { error: 'Property title is required' });
        }

        const newProp = {
          id: body.id || `prop_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
          title: body.title,
          listingType: body.listingType || 'RENT',
          propertyType: body.propertyType || 'APARTMENT',
          locality: body.locality || 'Raman Reti',
          city: body.city || 'Vrindavan',
          fullAddress: body.fullAddress || '',
          landmark: body.landmark || '',
          price: Number(body.price) || 0,
          deposit: Number(body.deposit) || 0,
          maintenanceCharges: Number(body.maintenanceCharges || body.maintenance) || 0,
          bhk: body.bhk || '2 BHK',
          builtUpAreaSqFt: Number(body.builtUpAreaSqFt || body.builtUpArea) || 0,
          carpetAreaSqFt: Number(body.carpetAreaSqFt || body.carpetArea) || 0,
          floor: Number(body.floor) || 1,
          totalFloors: Number(body.totalFloors) || 4,
          furnishing: body.furnishing || 'SEMI_FURNISHED',
          facing: body.facing || 'East',
          preferredTenants: body.preferredTenants || 'Devotees / Families',
          isPureSattvik: body.isPureSattvik !== false,
          amenities: Array.isArray(body.amenities) ? body.amenities : [],
          legalStatus: body.legalStatus || 'MVDA Compliant',
          approvalStatus: 'PENDING_APPROVAL', // Goes directly to admin pending queue!
          availabilityStatus: 'AVAILABLE',
          isVerified: false,
          photos: Array.isArray(body.photos) ? body.photos : [],
          loggedCoordinates: body.loggedCoordinates || '27.5794° N, 77.6881° E',
          loggedLocality: body.loggedLocality || `${body.locality || 'Raman Reti'}, Vrindavan`,
          ownerId: body.ownerId || 'user_owner',
          ownerName: body.ownerName || 'Devotee Owner',
          ownerPhone: body.ownerPhone || '',
          createdAt: Date.now()
        };

        // Add to top of list
        db.properties.unshift(newProp);
        saveDb();

        console.log(`[Brajvaas API] New property created: "${newProp.title}" (${newProp.id}) - Pending Admin Review`);
        return sendJson(res, 201, { success: true, message: 'Property submitted for Admin Approval', property: newProp });
      }
    }

    // Property individual actions: /api/properties/:id or /api/approve, etc.
    if (pathname.startsWith('/api/properties/') || pathname.startsWith('/properties/')) {
      const parts = pathname.split('/').filter(Boolean);
      const propId = parts[2];
      const subAction = parts[3];

      const idx = db.properties.findIndex(p => p.id === propId);
      if (idx === -1) {
        return sendJson(res, 404, { error: 'Property not found', id: propId });
      }

      if (method === 'GET') {
        return sendJson(res, 200, { success: true, property: db.properties[idx] });
      }

      if (method === 'DELETE') {
        const deleted = db.properties.splice(idx, 1)[0];
        saveDb();
        return sendJson(res, 200, { success: true, message: 'Property deleted', property: deleted });
      }

      if (method === 'PUT' || method === 'POST') {
        const body = await parseBody(req);

        if (subAction === 'approve' || body.action === 'approve') {
          db.properties[idx].approvalStatus = 'APPROVED';
          db.properties[idx].availabilityStatus = 'AVAILABLE';
          saveDb();
          return sendJson(res, 200, { success: true, message: 'Property approved & live', property: db.properties[idx] });
        }

        if (subAction === 'reject' || body.action === 'reject') {
          db.properties[idx].approvalStatus = 'REJECTED';
          db.properties[idx].adminFeedback = body.reason || query.reason || 'Verification required';
          saveDb();
          return sendJson(res, 200, { success: true, message: 'Property rejected', property: db.properties[idx] });
        }

        if (subAction === 'availability' || body.availabilityStatus) {
          const newStatus = body.availabilityStatus || query.status || 'AVAILABLE';
          db.properties[idx].availabilityStatus = newStatus;
          db.properties[idx].closedAt = (newStatus !== 'AVAILABLE') ? Date.now() : null;
          saveDb();
          return sendJson(res, 200, { success: true, message: `Availability set to ${newStatus}`, property: db.properties[idx] });
        }

        // General update / edit details
        Object.assign(db.properties[idx], body);
        saveDb();
        return sendJson(res, 200, { success: true, message: 'Property updated', property: db.properties[idx] });
      }
    }

    // Direct compatibility endpoints for existing web console query-string calls
    if (pathname === '/api/approve') {
      const id = query.id;
      const prop = db.properties.find(p => p.id === id);
      if (prop) {
        prop.approvalStatus = 'APPROVED';
        prop.availabilityStatus = 'AVAILABLE';
        saveDb();
      }
      return sendJson(res, 200, { success: true, action: 'approved', id });
    }

    if (pathname === '/api/reject') {
      const id = query.id;
      const reason = query.reason || 'Verification required';
      const prop = db.properties.find(p => p.id === id);
      if (prop) {
        prop.approvalStatus = 'REJECTED';
        prop.adminFeedback = reason;
        saveDb();
      }
      return sendJson(res, 200, { success: true, action: 'rejected', id });
    }

    if (pathname === '/api/mark-availability') {
      const id = query.id;
      const status = query.status || 'AVAILABLE';
      const prop = db.properties.find(p => p.id === id);
      if (prop) {
        prop.availabilityStatus = status;
        prop.closedAt = (status !== 'AVAILABLE') ? Date.now() : null;
        saveDb();
      }
      return sendJson(res, 200, { success: true, id, status });
    }

    if (pathname === '/api/edit-property') {
      const id = query.id;
      const prop = db.properties.find(p => p.id === id);
      if (prop) {
        if (query.title) prop.title = query.title;
        if (query.price) prop.price = Number(query.price);
        if (query.bhk) prop.bhk = query.bhk;
        if (query.locality) prop.locality = query.locality;
        if (query.builtUpAreaSqFt) prop.builtUpAreaSqFt = Number(query.builtUpAreaSqFt);
        if (query.fullAddress) prop.fullAddress = query.fullAddress;
        if (query.landmark) prop.landmark = query.landmark;
        if (query.ownerName) prop.ownerName = query.ownerName;
        if (query.ownerPhone) prop.ownerPhone = query.ownerPhone;
        saveDb();
      }
      return sendJson(res, 200, { success: true, id });
    }

    // 4. Agreements Endpoints (starts empty with 0 entries!)
    if (pathname === '/api/agreements' || pathname === '/agreements') {
      if (method === 'GET') {
        return sendJson(res, 200, { success: true, count: db.agreements.length, agreements: db.agreements });
      }

      if (method === 'POST') {
        const body = await parseBody(req);
        const newAgr = {
          id: body.id || `AGR-${Date.now()}`,
          propertyId: body.propertyId || '',
          propertyTitle: body.propertyTitle || 'Sacred Ashram Flat',
          locality: body.locality || 'Raman Reti, Vrindavan',
          monthlyRent: Number(body.monthlyRent) || 15000,
          brokerage15DaysFee: Number(body.brokerage15DaysFee) || 7500,
          tenantName: body.tenantName || 'Devotee Tenant',
          landlordName: body.landlordName || 'Devotee Landlord',
          agreementStatus: body.agreementStatus || 'DRAFTING',
          policeVerificationStatus: body.policeVerificationStatus || 'DOCUMENTS_COLLECTED',
          agreementNumber: body.agreementNumber || `BV-AGR-2026-${Math.floor(Math.random() * 9000 + 1000)}`,
          policeAckNumber: body.policeAckNumber || `UPCOP-VRN-2026-${Math.floor(Math.random() * 90000 + 10000)}`,
          initiatedDate: new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }),
          targetPossessionDate: 'Within 7 Days',
          isBrokeragePaidByTenant: false,
          isBrokeragePaidByLandlord: false
        };
        db.agreements.unshift(newAgr);
        saveDb();
        return sendJson(res, 201, { success: true, agreement: newAgr });
      }
    }

    // 5. Leads Endpoints
    if (pathname === '/api/leads' || pathname === '/leads') {
      if (method === 'GET') {
        return sendJson(res, 200, { success: true, count: db.leads.length, leads: db.leads });
      }

      if (method === 'POST') {
        const body = await parseBody(req);
        const newLead = {
          id: body.id || `LEAD-${Date.now()}`,
          userId: body.userId || 'devotee',
          userName: body.userName || 'Devotee',
          userPhone: body.userPhone || '',
          userEmail: body.userEmail || '',
          userExactLocation: body.userExactLocation || '27.5794° N, 77.6881° E (Vrindavan)',
          propertyId: body.propertyId || '',
          propertyTitle: body.propertyTitle || '',
          propertyLocality: body.propertyLocality || 'Vrindavan',
          listingType: body.listingType || 'RENT',
          propertyPrice: Number(body.propertyPrice) || 0,
          leadType: body.leadType || 'Inquiry',
          status: 'New Lead - Pending Callback',
          adminNotes: body.adminNotes || 'Interested seeker ready for callback',
          timestamp: Date.now()
        };
        db.leads.unshift(newLead);
        saveDb();
        return sendJson(res, 201, { success: true, lead: newLead });
      }
    }

    // 6. Backup & Restore Endpoints
    if (pathname === '/api/export' || pathname === '/export') {
      res.writeHead(200, {
        'Content-Type': 'application/json; charset=utf-8',
        'Content-Disposition': `attachment; filename="brajvaas_backup_${new Date().toISOString().slice(0, 10)}.json"`,
        'Access-Control-Allow-Origin': '*'
      });
      return res.end(JSON.stringify(db, null, 2));
    }

    if ((pathname === '/api/import' || pathname === '/import') && method === 'POST') {
      const body = await parseBody(req);
      if (body && Array.isArray(body.properties)) {
        db = {
          properties: body.properties || [],
          agreements: body.agreements || [],
          leads: body.leads || [],
          users: body.users || [],
          shortStays: body.shortStays || [],
          visitorCount: typeof body.visitorCount === 'number' ? body.visitorCount : (db.visitorCount || 101),
          version: body.version || '2026.10',
          lastUpdated: Date.now()
        };
        saveDb();
        return sendJson(res, 200, { success: true, message: 'Database restored successfully', counts: { properties: db.properties.length, agreements: db.agreements.length, visitorCount: db.visitorCount } });
      }
      return sendJson(res, 400, { error: 'Invalid backup JSON file' });
    }

    // 7. Clear all postings / reset database
    if (pathname === '/api/clear-all' || (pathname === '/api/properties' && method === 'DELETE')) {
      db.properties = [];
      db.agreements = [];
      db.leads = [];
      saveDb();
      console.log('[Brajvaas API] All postings and agreements cleared by Admin.');
      return sendJson(res, 200, { success: true, message: 'All postings and agreements cleared fresh.' });
    }

    // 404 for unknown endpoints
    sendJson(res, 404, { error: 'Endpoint not found', path: pathname });

  } catch (error) {
    console.error(`[Brajvaas API] Server Error on ${pathname}:`, error);
    sendJson(res, 500, { error: 'Internal Server Error', details: error.message });
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`====================================================`);
  console.log(`[Brajvaas Central API Server] Active on port ${PORT}`);
  console.log(`Endpoint: http://localhost:${PORT}/api/properties`);
  console.log(`Database File: ${DB_PATH}`);
  console.log(`====================================================`);
});
