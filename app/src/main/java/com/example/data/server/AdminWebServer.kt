package com.example.data.server

import android.util.Log
import com.example.data.model.ApprovalStatus
import com.example.data.model.ListingType
import com.example.data.repository.BrajvaasRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Embedded Local HTTP Server running inside the Android app.
 * Serves the Web-based Admin Portal on http://127.0.0.1:8080/admin
 * Enables true web access from in-app WebView, local Android Chrome browser,
 * and any connected browser environment.
 */
class AdminWebServer(private val repository: BrajvaasRepository) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    var boundPort: Int = 8080
        private set
    var isRunning: Boolean = false
        private set

    fun start(preferredPort: Int = 8080): Int {
        if (isRunning) return boundPort

        var port = preferredPort
        var socket: ServerSocket? = null
        for (attempt in 0..5) {
            try {
                socket = ServerSocket(port)
                boundPort = port
                break
            } catch (e: Exception) {
                port++
            }
        }

        if (socket == null) {
            println("[AdminWebServer] Failed to bind server socket to port $preferredPort")
            return -1
        }

        serverSocket = socket
        isRunning = true
        println("[AdminWebServer] Admin Web Server started on http://127.0.0.1:$boundPort/admin")

        serverJob = scope.launch {
            while (isRunning && !socket.isClosed) {
                try {
                    val client = socket.accept()
                    scope.launch { handleClient(client) }
                } catch (e: Exception) {
                    if (!isRunning) break
                }
            }
        }

        return boundPort
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
    }

    private fun handleClient(client: Socket) {
        try {
            client.use { s ->
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val out = s.getOutputStream()

                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return

                val method = parts[0]
                val fullPath = parts[1]
                val path = fullPath.substringBefore("?")
                val queryString = fullPath.substringAfter("?", "")

                val params = parseQueryString(queryString)

                when {
                    path == "/" || path == "/admin" || path.startsWith("/admin") || path.endsWith(".html") -> {
                        val html = generateWebAdminHtml()
                        sendResponse(out, "200 OK", "text/html; charset=utf-8", html.toByteArray(StandardCharsets.UTF_8))
                    }
                    path == "/api/data" -> {
                        val json = generateJsonSnapshot()
                        sendResponse(out, "200 OK", "application/json; charset=utf-8", json.toByteArray(StandardCharsets.UTF_8))
                    }
                    path == "/api/approve" -> {
                        val id = params["id"] ?: ""
                        if (id.isNotBlank()) {
                            repository.adminApprove(id, true)
                        }
                        sendResponse(out, "200 OK", "application/json; charset=utf-8", "{\"success\":true,\"action\":\"approved\",\"id\":\"$id\"}".toByteArray(StandardCharsets.UTF_8))
                    }
                    path == "/api/reject" -> {
                        val id = params["id"] ?: ""
                        val reason = params["reason"] ?: "Documents require verification"
                        if (id.isNotBlank()) {
                            repository.adminReject(id, reason)
                        }
                        sendResponse(out, "200 OK", "application/json; charset=utf-8", "{\"success\":true,\"action\":\"rejected\",\"id\":\"$id\"}".toByteArray(StandardCharsets.UTF_8))
                    }
                    path == "/api/lead" -> {
                        val id = params["id"] ?: ""
                        val status = params["status"] ?: "Contacted"
                        if (id.isNotBlank()) {
                            repository.updateLeadStatus(id, status, "Updated via Web Console")
                        }
                        sendResponse(out, "200 OK", "application/json; charset=utf-8", "{\"success\":true,\"leadId\":\"$id\",\"newStatus\":\"$status\"}".toByteArray(StandardCharsets.UTF_8))
                    }
                    path == "/api/user/kyc" -> {
                        val id = params["id"] ?: ""
                        if (id.isNotBlank()) {
                            repository.toggleUserKyc(id)
                        }
                        sendResponse(out, "200 OK", "application/json; charset=utf-8", "{\"success\":true,\"userId\":\"$id\"}".toByteArray(StandardCharsets.UTF_8))
                    }
                    path == "/health" -> {
                        sendResponse(out, "200 OK", "text/plain", "OK".toByteArray(StandardCharsets.UTF_8))
                    }
                    else -> {
                        val html = generateWebAdminHtml()
                        sendResponse(out, "200 OK", "text/html; charset=utf-8", html.toByteArray(StandardCharsets.UTF_8))
                    }
                }
            }
        } catch (e: Exception) {
            println("[AdminWebServer] Error processing request: $e")
        }
    }

    private fun parseQueryString(qs: String): Map<String, String> {
        if (qs.isBlank()) return emptyMap()
        val map = mutableMapOf<String, String>()
        for (pair in qs.split("&")) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                map[key] = value
            }
        }
        return map
    }

    private fun sendResponse(out: OutputStream, status: String, contentType: String, body: ByteArray) {
        val header = "HTTP/1.1 $status\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${body.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(body)
        out.flush()
    }

    private fun escapeJs(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
            .replace("\r", "")
            .replace("'", "\\'")
    }

    fun generateJsonSnapshot(): String {
        val users = repository.usersList.value
        val properties = repository.properties.value
        val leads = repository.userInterestLeads.value
        val shortStays = repository.shortStayBookings.value
        val logs = repository.locationAuditLogs.value

        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"users\":[")
        users.forEachIndexed { i, u ->
            if (i > 0) sb.append(",")
            sb.append("{\"id\":\"${escapeJs(u.id)}\",\"name\":\"${escapeJs(u.fullName)}\",\"phone\":\"${escapeJs(u.phone)}\",\"email\":\"${escapeJs(u.email)}\",\"role\":\"${u.role.name}\",\"kyc\":${u.isKycVerified},\"city\":\"${escapeJs(u.city)}\",\"coordinates\":\"${escapeJs(u.lastKnownCoordinates)}\",\"locality\":\"${escapeJs(u.lastKnownLocality)}\"}")
        }
        sb.append("],\"properties\":[")
        properties.forEachIndexed { i, p ->
            if (i > 0) sb.append(",")
            sb.append("{\"id\":\"${escapeJs(p.id)}\",\"title\":\"${escapeJs(p.title)}\",\"locality\":\"${escapeJs(p.locality)}\",\"price\":${p.price},\"status\":\"${p.approvalStatus.name}\",\"type\":\"${p.listingType.name}\",\"owner\":\"${escapeJs(p.ownerName)}\",\"phone\":\"${escapeJs(p.ownerPhone)}\",\"temple\":\"${p.templeDistances.premMandirKm} km from Prem Mandir\",\"verified\":${p.isVerified},\"coords\":\"${escapeJs(p.loggedCoordinates)}\"}")
        }
        sb.append("],\"leads\":[")
        leads.forEachIndexed { i, l ->
            if (i > 0) sb.append(",")
            sb.append("{\"id\":\"${escapeJs(l.id)}\",\"userName\":\"${escapeJs(l.userName)}\",\"userPhone\":\"${escapeJs(l.userPhone)}\",\"propertyTitle\":\"${escapeJs(l.propertyTitle)}\",\"locality\":\"${escapeJs(l.propertyLocality)}\",\"price\":${l.propertyPrice},\"status\":\"${escapeJs(l.status)}\",\"coords\":\"${escapeJs(l.userExactLocation)}\",\"type\":\"${l.listingType.name}\"}")
        }
        sb.append("],\"shortStays\":[")
        shortStays.forEachIndexed { i, s ->
            if (i > 0) sb.append(",")
            sb.append("{\"id\":\"${escapeJs(s.id)}\",\"homestay\":\"${escapeJs(s.propertyTitle)}\",\"guest\":\"${escapeJs(s.guestName)}\",\"phone\":\"${escapeJs(s.guestPhone)}\",\"tenantFee\":${s.tenantFee},\"landlordFee\":${s.landlordFee},\"dates\":\"${escapeJs(s.checkInDate)} to ${escapeJs(s.checkOutDate)}\",\"guests\":${s.numberOfGuests},\"total\":${s.totalGuestPayable},\"status\":\"${escapeJs(s.status)}\"}")
        }
        sb.append("],\"locationLogs\":[")
        logs.forEachIndexed { i, g ->
            if (i > 0) sb.append(",")
            sb.append("{\"id\":\"${escapeJs(g.id)}\",\"user\":\"${escapeJs(g.userName)}\",\"action\":\"${escapeJs(g.actionType)}\",\"locality\":\"${escapeJs(g.geoLocality)}\",\"coords\":\"${escapeJs(g.exactCoordinates)}\",\"details\":\"${escapeJs(g.actionDescription)}\"}")
        }
        sb.append("]}")
        return sb.toString()
    }

    fun generateWebAdminHtml(): String {
        val users = repository.usersList.value
        val properties = repository.properties.value
        val leads = repository.userInterestLeads.value
        val shortStays = repository.shortStayBookings.value
        val logs = repository.locationAuditLogs.value

        val pendingCount = properties.count { it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }
        val newLeadsCount = leads.count { it.status.contains("New", ignoreCase = true) }
        val totalRevenueFromShortStays = shortStays.size * 400 // ₹200 from tenant + ₹200 from landlord

        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Brajvaas Web Admin Console</title>
  <style>
    :root {
      --primary: #0F5A47;
      --primary-light: #E8F5F1;
      --primary-dark: #09372B;
      --accent: #D97706;
      --bg: #F8FAFC;
      --surface: #FFFFFF;
      --text: #0F172A;
      --muted: #64748B;
      --border: #E2E8F0;
      --success: #16A34A;
      --danger: #DC2626;
      --warning: #EA580C;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    body { background-color: var(--bg); color: var(--text); line-height: 1.5; padding-bottom: 40px; }
    header { background: #1E293B; color: white; padding: 16px 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid var(--primary); }
    .logo-row { display: flex; align-items: center; gap: 12px; }
    .badge-live { background: var(--primary); color: white; font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 999px; text-transform: uppercase; letter-spacing: 0.5px; }
    .server-pill { background: #334155; padding: 6px 12px; border-radius: 6px; font-size: 12px; color: #38BDF8; font-family: monospace; display: flex; align-items: center; gap: 6px; }
    .indicator { width: 8px; height: 8px; background: #22C55E; border-radius: 50%; box-shadow: 0 0 8px #22C55E; display: inline-block; }
    
    .stats-bar { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; padding: 24px; max-width: 1400px; margin: 0 auto; }
    .stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: 10px; padding: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    .stat-title { font-size: 12px; font-weight: 600; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; }
    .stat-value { font-size: 26px; font-weight: 800; color: var(--primary-dark); margin-top: 4px; }
    .stat-sub { font-size: 11px; color: var(--accent); font-weight: 600; margin-top: 2px; }

    .nav-tabs { display: flex; gap: 8px; border-bottom: 1px solid var(--border); background: var(--surface); padding: 0 24px; max-width: 1400px; margin: 0 auto; overflow-x: auto; }
    .tab-btn { background: none; border: none; padding: 14px 18px; font-size: 14px; font-weight: 600; color: var(--muted); cursor: pointer; border-bottom: 3px solid transparent; transition: all 0.2s; white-space: nowrap; }
    .tab-btn:hover { color: var(--primary); }
    .tab-btn.active { color: var(--primary); border-bottom-color: var(--primary); background: var(--primary-light); }
    .tab-count { background: #E2E8F0; color: #475569; font-size: 11px; padding: 2px 6px; border-radius: 10px; margin-left: 6px; }
    .tab-btn.active .tab-count { background: var(--primary); color: white; }

    .container { max-width: 1400px; margin: 24px auto; padding: 0 24px; }
    .panel-section { display: none; }
    .panel-section.active { display: block; }
    
    .card { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.03); margin-bottom: 24px; }
    .card-title { font-size: 18px; font-weight: 700; color: var(--text); margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; }
    
    table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 13px; }
    th { background: #F1F5F9; color: var(--muted); text-align: left; padding: 10px 14px; font-weight: 600; text-transform: uppercase; font-size: 11px; letter-spacing: 0.5px; border-bottom: 2px solid var(--border); }
    td { padding: 12px 14px; border-bottom: 1px solid var(--border); vertical-align: middle; }
    tr:hover td { background: #F8FAFC; }
    
    .status-badge { display: inline-block; padding: 3px 8px; border-radius: 6px; font-size: 11px; font-weight: 700; }
    .badge-approved { background: #DCFCE7; color: #15803D; }
    .badge-pending { background: #FEF3C7; color: #B45309; }
    .badge-rejected { background: #FEE2E2; color: #B91C1C; }
    .badge-lead { background: #E0E7FF; color: #4338CA; }
    .badge-kyc-yes { background: #D1FAE5; color: #065F46; }
    .badge-kyc-no { background: #F3F4F6; color: #6B7280; }
    
    .btn { display: inline-flex; align-items: center; gap: 4px; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; border: none; text-decoration: none; transition: opacity 0.2s; }
    .btn:hover { opacity: 0.85; }
    .btn-approve { background: #16A34A; color: white; }
    .btn-reject { background: #DC2626; color: white; }
    .btn-primary { background: var(--primary); color: white; }
    .btn-outline { background: white; border: 1px solid var(--border); color: var(--text); }
    .btn-contact { background: #0284C7; color: white; }
    
    .search-box { width: 100%; max-width: 400px; padding: 8px 14px; border: 1px solid var(--border); border-radius: 8px; font-size: 13px; margin-bottom: 16px; outline: none; }
    .search-box:focus { border-color: var(--primary); box-shadow: 0 0 0 2px var(--primary-light); }
    
    .banner-notice { background: #FFFBEB; border-left: 4px solid #F59E0B; padding: 14px 18px; border-radius: 6px; margin-bottom: 20px; font-size: 13px; color: #92400E; }
    .banner-notice strong { color: #78350F; }

    .fee-badge { background: #FEF3C7; color: #92400E; font-weight: 800; font-size: 11px; padding: 4px 8px; border-radius: 4px; border: 1px dashed #F59E0B; }
    .mono { font-family: monospace; font-size: 12px; color: #334155; }
    .map-link { color: #0284C7; text-decoration: none; font-weight: 600; }
    .map-link:hover { text-decoration: underline; }
  </style>
</head>
<body>

  <header>
    <div class="logo-row">
      <h2 style="font-weight: 800; letter-spacing: -0.5px;">Brajvaas</h2>
      <span class="badge-live">Web Admin Console</span>
      <span style="font-size: 12px; color: #94A3B8;">Mathura & Vrindavan Central Portal</span>
    </div>
    <div style="display: flex; align-items: center; gap: 12px;">
      <div class="server-pill">
        <span class="indicator"></span>
        <span>Local HTTP Server: http://127.0.0.1:$boundPort/admin</span>
      </div>
      <button class="btn btn-primary" onclick="location.reload()">Refresh Data</button>
    </div>
  </header>

  <div class="stats-bar">
    <div class="stat-card">
      <div class="stat-title">Total Users</div>
      <div class="stat-value">${users.size}</div>
      <div class="stat-sub">${users.count { it.isKycVerified }} KYC Verified</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">Pending Moderation</div>
      <div class="stat-value" style="color: var(--warning);">$pendingCount</div>
      <div class="stat-sub">Review User Postings</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">Devotee Interest Leads</div>
      <div class="stat-value" style="color: #4338CA;">${leads.size}</div>
      <div class="stat-sub">$newLeadsCount New Follow-ups</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">Vrindavan Short Stays</div>
      <div class="stat-value" style="color: #0284C7;">${shortStays.size}</div>
      <div class="stat-sub">₹$totalRevenueFromShortStays Service Revenue (₹200+₹200)</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">Location Audit Logs</div>
      <div class="stat-value">${logs.size}</div>
      <div class="stat-sub">Exact GPS Coordinates</div>
    </div>
  </div>

  <nav class="nav-tabs">
    <button class="tab-btn active" onclick="switchTab('overview')">Overview</button>
    <button class="tab-btn" onclick="switchTab('users')">Users <span class="tab-count">${users.size}</span></button>
    <button class="tab-btn" onclick="switchTab('properties')">Properties <span class="tab-count">${properties.size}</span></button>
    <button class="tab-btn" onclick="switchTab('leads')">Interest Leads (Likes) <span class="tab-count">${leads.size}</span></button>
    <button class="tab-btn" onclick="switchTab('shortstays')">Vrindavan Short Stays <span class="tab-count">${shortStays.size}</span></button>
    <button class="tab-btn" onclick="switchTab('locations')">Exact GPS Logs <span class="tab-count">${logs.size}</span></button>
  </nav>

  <div class="container">
    <div class="banner-notice">
      <strong>Active Policy & Business Rules:</strong>
      Long-term rentals charge <strong>15 days rent as brokerage</strong> (Rent agreement & police verification managed by Brajvaas).
      Vrindavan Short Stays & Homestays charge a flat <strong>₹200 platform fee</strong> upon booking.
      All user actions (posting, renting, buying, short stays) log <strong>exact GPS coordinates</strong> displayed below.
    </div>

    <!-- OVERVIEW TAB -->
    <div id="tab-overview" class="panel-section active">
      <div class="card">
        <div class="card-title">
          <span>Pending Properties Moderation Queue</span>
          <span class="badge-pending">$pendingCount Require Action</span>
        </div>
        <table>
          <thead>
            <tr>
              <th>Property</th>
              <th>Owner / Contact</th>
              <th>Locality</th>
              <th>Purpose & Brokerage</th>
              <th>Rent / Price</th>
              <th>Coordinates</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${
              properties.filter { it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }.joinToString("") { p ->
                """
                <tr>
                  <td><strong>${p.title}</strong><br><small style="color:var(--muted);">${p.templeDistances.premMandirKm} km to Prem Mandir</small></td>
                  <td>${p.ownerName}<br><a href="tel:${p.ownerPhone}" class="map-link">${p.ownerPhone}</a></td>
                  <td>${p.locality}</td>
                  <td>
                    ${if (p.listingType == ListingType.RENT) "<span class='fee-badge'>Rent (15-Day Brokerage)</span>" else if (p.listingType == ListingType.SHORT_STAY) "<span class='fee-badge'>Short Stay (₹200 Fee)</span>" else "<span class='status-badge badge-lead'>Direct Sale</span>"}
                  </td>
                  <td><strong>₹${p.price}</strong></td>
                  <td class="mono">${p.loggedCoordinates}</td>
                  <td>
                    <button class="btn btn-approve" onclick="approveProperty('${p.id}')">Approve</button>
                    <button class="btn btn-reject" onclick="rejectProperty('${p.id}')">Reject</button>
                  </td>
                </tr>
                """
              }.ifEmpty { "<tr><td colspan='7' style='text-align:center; color:var(--muted); padding:24px;'>All posted properties have been approved or reviewed!</td></tr>" }
            }
          </tbody>
        </table>
      </div>

      <div class="card">
        <div class="card-title">
          <span>Recent User Interest Leads (Devotees Who Liked Properties)</span>
          <a href="#" onclick="switchTab('leads')" class="map-link">View All Leads →</a>
        </div>
        <table>
          <thead>
            <tr>
              <th>Devotee Name</th>
              <th>Contact</th>
              <th>Interested In</th>
              <th>Rent/Price</th>
              <th>Exact Location</th>
              <th>Status</th>
              <th>Admin Action</th>
            </tr>
          </thead>
          <tbody>
            ${
              leads.take(5).joinToString("") { l ->
                """
                <tr>
                  <td><strong>${l.userName}</strong></td>
                  <td><a href="tel:${l.userPhone}" class="map-link">${l.userPhone}</a></td>
                  <td>${l.propertyTitle}<br><small style="color:var(--muted);">${l.propertyLocality}</small></td>
                  <td>₹${l.propertyPrice}</td>
                  <td class="mono">${l.userExactLocation}</td>
                  <td><span class="status-badge badge-lead">${l.status}</span></td>
                  <td>
                    <button class="btn btn-contact" onclick="updateLead('${l.id}', 'Contacted by Admin')">Mark Contacted</button>
                  </td>
                </tr>
                """
              }
            }
          </tbody>
        </table>
      </div>
    </div>

    <!-- USERS TAB -->
    <div id="tab-users" class="panel-section">
      <div class="card">
        <div class="card-title">User Management</div>
        <input type="text" id="userSearch" class="search-box" placeholder="Search user by name, phone, email, or locality..." onkeyup="filterUsers()">
        <table id="usersTable">
          <thead>
            <tr>
              <th>User Name</th>
              <th>Role</th>
              <th>Phone</th>
              <th>Email</th>
              <th>City</th>
              <th>KYC Verified</th>
              <th>Exact GPS Coordinates</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${
              users.joinToString("") { u ->
                """
                <tr>
                  <td><strong>${u.fullName}</strong><br><small style="color:var(--muted);">${u.id}</small></td>
                  <td><span class="status-badge badge-lead">${u.role.name}</span></td>
                  <td><a href="tel:${u.phone}" class="map-link">${u.phone}</a></td>
                  <td>${u.email}</td>
                  <td>${u.city}</td>
                  <td>
                    <span class="status-badge ${if (u.isKycVerified) "badge-kyc-yes" else "badge-kyc-no"}">
                      ${if (u.isKycVerified) "Verified" else "Pending"}
                    </span>
                  </td>
                  <td class="mono">${u.lastKnownCoordinates}<br><small style="color:var(--muted);">${u.lastKnownLocality}</small></td>
                  <td>
                    <button class="btn btn-outline" onclick="toggleUserKyc('${u.id}')">Toggle KYC</button>
                  </td>
                </tr>
                """
              }
            }
          </tbody>
        </table>
      </div>
    </div>

    <!-- PROPERTIES TAB -->
    <div id="tab-properties" class="panel-section">
      <div class="card">
        <div class="card-title">Property Listings Moderation</div>
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Owner</th>
              <th>Type / Policy</th>
              <th>Locality</th>
              <th>Price</th>
              <th>Status</th>
              <th>Exact GPS</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${
              properties.joinToString("") { p ->
                val badgeClass = when(p.approvalStatus) {
                  ApprovalStatus.APPROVED -> "badge-approved"
                  ApprovalStatus.PENDING_APPROVAL -> "badge-pending"
                  ApprovalStatus.REJECTED -> "badge-rejected"
                }
                """
                <tr>
                  <td><strong>${p.title}</strong><br><small style="color:var(--muted);">${p.templeDistances.premMandirKm} km from Prem Mandir</small></td>
                  <td>${p.ownerName}<br><a href="tel:${p.ownerPhone}" class="map-link">${p.ownerPhone}</a></td>
                  <td>
                    ${if (p.listingType == ListingType.RENT) "<span class='fee-badge'>Rent (15d Fee)</span>" else if (p.listingType == ListingType.SHORT_STAY) "<span class='fee-badge'>Short Stay (₹200 Fee)</span>" else "<span class='status-badge badge-lead'>Sale</span>"}
                  </td>
                  <td>${p.locality}</td>
                  <td>₹${p.price}</td>
                  <td><span class="status-badge $badgeClass">${p.approvalStatus.name}</span></td>
                  <td class="mono">${p.loggedCoordinates}</td>
                  <td>
                    ${if (p.approvalStatus == ApprovalStatus.PENDING_APPROVAL) """
                      <button class="btn btn-approve" onclick="approveProperty('${p.id}')">Approve</button>
                      <button class="btn btn-reject" onclick="rejectProperty('${p.id}')">Reject</button>
                    """ else """
                      <button class="btn btn-outline" onclick="rejectProperty('${p.id}')">Revoke</button>
                    """}
                  </td>
                </tr>
                """
              }
            }
          </tbody>
        </table>
      </div>
    </div>

    <!-- LEADS TAB -->
    <div id="tab-leads" class="panel-section">
      <div class="card">
        <div class="card-title">User Interest Leads (Devotees Who Liked or Shortlisted)</div>
        <p style="font-size:13px; color:var(--muted); margin-bottom:12px;">When any user likes or shortlists a property, their interest appears here immediately so admin can contact them for agreement and police verification.</p>
        <table>
          <thead>
            <tr>
              <th>Devotee Name</th>
              <th>Phone</th>
              <th>Property Liked</th>
              <th>Locality</th>
              <th>Rent / Price</th>
              <th>Exact Location</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${
              leads.joinToString("") { l ->
                """
                <tr>
                  <td><strong>${l.userName}</strong></td>
                  <td><a href="tel:${l.userPhone}" class="map-link">${l.userPhone}</a></td>
                  <td>${l.propertyTitle}</td>
                  <td>${l.propertyLocality}</td>
                  <td>₹${l.propertyPrice}</td>
                  <td class="mono">${l.userExactLocation}</td>
                  <td><span class="status-badge badge-lead">${l.status}</span></td>
                  <td>
                    <button class="btn btn-contact" onclick="updateLead('${l.id}', 'Contacted')">Mark Contacted</button>
                    <button class="btn btn-approve" onclick="updateLead('${l.id}', 'Agreement Initiated')">Initiate Agreement</button>
                  </td>
                </tr>
                """
              }
            }
          </tbody>
        </table>
      </div>
    </div>

    <!-- SHORT STAYS TAB -->
    <div id="tab-shortstays" class="panel-section">
      <div class="card">
        <div class="card-title">
          <span>Vrindavan Short Stays & Homestays</span>
          <span class="fee-badge">₹200 Flat Platform Fee</span>
        </div>
        <p style="font-size:13px; color:var(--muted); margin-bottom:12px;">Exclusive to Vrindavan locality. Irrespective of stay duration, Brajvaas calculates a flat ₹200 platform fee upon booking.</p>
        <table>
          <thead>
            <tr>
              <th>Homestay Title</th>
              <th>Guest Name & Phone</th>
              <th>Dates</th>
              <th>Guests</th>
              <th>Tenant Fee</th>
              <th>Landlord Fee</th>
              <th>Total Platform Fee</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            ${
              shortStays.joinToString("") { s ->
                """
                <tr>
                  <td><strong>${s.propertyTitle}</strong><br><small style="color:var(--muted);">${s.id}</small></td>
                  <td>${s.guestName}<br><a href="tel:${s.guestPhone}" class="map-link">${s.guestPhone}</a></td>
                  <td>${s.checkInDate} to ${s.checkOutDate}</td>
                  <td>${s.numberOfGuests} Devotees</td>
                  <td><strong style="color:var(--primary);">₹${s.tenantFee}</strong></td>
                  <td><strong style="color:var(--primary);">₹${s.landlordFee}</strong></td>
                  <td><span class="fee-badge">₹${s.tenantFee + s.landlordFee}</span></td>
                  <td><span class="status-badge badge-approved">${s.status}</span></td>
                </tr>
                """
              }
            }
          </tbody>
        </table>
      </div>
    </div>

    <!-- EXACT LOCATION LOGS TAB -->
    <div id="tab-locations" class="panel-section">
      <div class="card">
        <div class="card-title">
          <span>Exact User Location Audit Logs</span>
          <span class="badge-live">${logs.size} Logged Events</span>
        </div>
        <p style="font-size:13px; color:var(--muted); margin-bottom:12px;">Mandatory security & fraud prevention audit: Logs exact latitude & longitude of users whosoever posts, takes on rent, buys, or books short stays.</p>
        <table>
          <thead>
            <tr>
              <th>User Name</th>
              <th>Action Type</th>
              <th>Locality</th>
              <th>Exact GPS Coordinates</th>
              <th>Details</th>
              <th>Map Pin</th>
            </tr>
          </thead>
          <tbody>
            ${
              logs.joinToString("") { g ->
                """
                <tr>
                  <td><strong>${g.userName}</strong></td>
                  <td><span class="status-badge badge-lead">${g.actionType}</span></td>
                  <td>${g.geoLocality}</td>
                  <td class="mono"><strong>${g.exactCoordinates}</strong></td>
                  <td>${g.actionDescription}</td>
                  <td>
                    <a href="https://maps.google.com/?q=${g.exactCoordinates.replace("°", "").replace(" ", "")}" target="_blank" class="map-link">View on Google Maps ↗</a>
                  </td>
                </tr>
                """
              }
            }
          </tbody>
        </table>
      </div>
    </div>

  </div>

  <script>
    function switchTab(tabId) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.panel-section').forEach(s => s.classList.remove('active'));
      
      const targetBtn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.textContent.toLowerCase().includes(tabId));
      if (targetBtn) targetBtn.classList.add('active');
      
      const targetSection = document.getElementById('tab-' + tabId);
      if (targetSection) targetSection.classList.add('active');
    }

    function approveProperty(id) {
      fetch('/api/approve?id=' + encodeURIComponent(id))
        .then(() => { alert('Property Approved! Listing is now live.'); location.reload(); })
        .catch(() => { alert('Action dispatched.'); location.reload(); });
    }

    function rejectProperty(id) {
      const reason = prompt('Enter rejection reason (e.g., MVDA clearance needed):', 'Requires clearer ownership documents or verification');
      if (reason) {
        fetch('/api/reject?id=' + encodeURIComponent(id) + '&reason=' + encodeURIComponent(reason))
          .then(() => { alert('Property rejected.'); location.reload(); })
          .catch(() => { alert('Action dispatched.'); location.reload(); });
      }
    }

    function updateLead(id, status) {
      fetch('/api/lead?id=' + encodeURIComponent(id) + '&status=' + encodeURIComponent(status))
        .then(() => { alert('Lead status updated to ' + status); location.reload(); })
        .catch(() => { alert('Lead status updated.'); location.reload(); });
    }

    function toggleUserKyc(id) {
      fetch('/api/user/kyc?id=' + encodeURIComponent(id))
        .then(() => { alert('KYC status toggled.'); location.reload(); })
        .catch(() => { alert('KYC updated.'); location.reload(); });
    }

    function filterUsers() {
      const q = document.getElementById('userSearch').value.toLowerCase();
      const rows = document.querySelectorAll('#usersTable tbody tr');
      rows.forEach(r => {
        const text = r.innerText.toLowerCase();
        r.style.display = text.includes(q) ? '' : 'none';
      });
    }
  </script>
</body>
</html>
        """.trimIndent()
    }
}
