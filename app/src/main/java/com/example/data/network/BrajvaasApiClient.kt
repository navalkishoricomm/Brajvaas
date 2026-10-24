package com.example.data.network

import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Client for interacting with the Brajvaas Central Backend on VPS (admin.brajvaas.in).
 * Ensures that properties posted from any phone are immediately transmitted to the VPS
 * and appear in the Admin Portal.
 */
object BrajvaasApiClient {

    private const val TAG = "BrajvaasApiClient"

    // Primary VPS endpoint + Fallback domains
    private val BASE_URLS = listOf(
        "https://admin.brajvaas.in/api",
        "https://brajvaas.in/api",
        "http://admin.brajvaas.in:5004/api"
    )

    private const val TIMEOUT_MS = 6000

    /**
     * Posts a newly submitted property to the VPS backend.
     */
    suspend fun postProperty(listing: PropertyListing): Boolean = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("id", listing.id)
            put("title", listing.title)
            put("listingType", listing.listingType.name)
            put("propertyType", listing.propertyType.name)
            put("locality", listing.locality)
            put("city", listing.city)
            put("fullAddress", listing.fullAddress)
            put("landmark", listing.landmark)
            put("price", listing.price)
            put("deposit", listing.deposit)
            put("maintenanceCharges", listing.maintenanceCharges)
            put("bhk", listing.bhk)
            put("builtUpAreaSqFt", listing.builtUpAreaSqFt)
            put("carpetAreaSqFt", listing.carpetAreaSqFt)
            put("floor", listing.floor)
            put("totalFloors", listing.totalFloors)
            put("furnishing", listing.furnishing.name)
            put("facing", listing.facing)
            put("preferredTenants", listing.preferredTenants)
            put("isPureSattvik", listing.isPureSattvik)
            put("amenities", JSONArray(listing.amenities))
            put("legalStatus", listing.legalStatus)
            put("approvalStatus", listing.approvalStatus.name)
            put("availabilityStatus", listing.availabilityStatus.name)
            put("isVerified", listing.isVerified)
            put("photos", JSONArray(listing.photos))
            put("loggedCoordinates", listing.loggedCoordinates)
            put("loggedLocality", listing.loggedLocality)
            put("ownerId", listing.ownerId)
            put("ownerName", listing.ownerName)
            put("ownerPhone", listing.ownerPhone)
        }

        for (base in BASE_URLS) {
            try {
                val url = URL("$base/properties")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                    doOutput = true
                }

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(json.toString()) }

                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) {
                    Log.i(TAG, "Successfully posted property ${listing.id} to $base")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed posting to $base: ${e.message}")
            }
        }
        false
    }

    /**
     * Fetches all properties from the VPS backend.
     */
    suspend fun fetchProperties(): List<PropertyListing>? = withContext(Dispatchers.IO) {
        for (base in BASE_URLS) {
            try {
                val url = URL("$base/properties")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                    conn.disconnect()
                    val obj = JSONObject(text)
                    val arr = obj.optJSONArray("properties") ?: JSONArray()
                    val list = mutableListOf<PropertyListing>()

                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        list.add(parsePropertyJson(item))
                    }
                    Log.i(TAG, "Fetched ${list.size} properties from $base")
                    return@withContext list
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Failed fetching from $base: ${e.message}")
            }
        }
        null
    }

    /**
     * Approves a property on the VPS backend.
     */
    suspend fun approveProperty(id: String): Boolean = withContext(Dispatchers.IO) {
        for (base in BASE_URLS) {
            try {
                val url = URL("$base/approve?id=${id}")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }
                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) return@withContext true
            } catch (_: Exception) {}
        }
        false
    }

    /**
     * Rejects a property on the VPS backend.
     */
    suspend fun rejectProperty(id: String, reason: String): Boolean = withContext(Dispatchers.IO) {
        for (base in BASE_URLS) {
            try {
                val url = URL("$base/reject?id=${id}&reason=${java.net.URLEncoder.encode(reason, "UTF-8")}")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }
                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) return@withContext true
            } catch (_: Exception) {}
        }
        false
    }

    /**
     * Updates availability status on the VPS backend.
     */
    suspend fun updateAvailability(id: String, status: AvailabilityStatus): Boolean = withContext(Dispatchers.IO) {
        for (base in BASE_URLS) {
            try {
                val url = URL("$base/mark-availability?id=${id}&status=${status.name}")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }
                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) return@withContext true
            } catch (_: Exception) {}
        }
        false
    }

    /**
     * Updates property details on the VPS backend.
     */
    suspend fun editProperty(listing: PropertyListing): Boolean = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("title", listing.title)
            put("price", listing.price)
            put("bhk", listing.bhk)
            put("locality", listing.locality)
            put("builtUpAreaSqFt", listing.builtUpAreaSqFt)
            put("fullAddress", listing.fullAddress)
            put("landmark", listing.landmark)
            put("ownerName", listing.ownerName)
            put("ownerPhone", listing.ownerPhone)
        }

        for (base in BASE_URLS) {
            try {
                val url = URL("$base/properties/${listing.id}")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                    doOutput = true
                }
                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(json.toString()) }
                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) return@withContext true
            } catch (_: Exception) {}
        }
        false
    }

    /**
     * Clears all postings from VPS backend.
     */
    suspend fun clearAll(): Boolean = withContext(Dispatchers.IO) {
        for (base in BASE_URLS) {
            try {
                val url = URL("$base/clear-all")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = TIMEOUT_MS
                }
                val code = conn.responseCode
                conn.disconnect()
                if (code in 200..299) return@withContext true
            } catch (_: Exception) {}
        }
        false
    }

    private fun parsePropertyJson(item: JSONObject): PropertyListing {
        val photosArr = item.optJSONArray("photos")
        val photoList = mutableListOf<String>()
        if (photosArr != null) {
            for (p in 0 until photosArr.length()) {
                photoList.add(photosArr.getString(p))
            }
        }

        val typeStr = item.optString("listingType", item.optString("type", "RENT"))
        val listingType = try { ListingType.valueOf(typeStr) } catch (_: Exception) { ListingType.RENT }

        val appStatusStr = item.optString("approvalStatus", item.optString("status", "PENDING_APPROVAL"))
        val approvalStatus = try { ApprovalStatus.valueOf(appStatusStr) } catch (_: Exception) { ApprovalStatus.PENDING_APPROVAL }

        val availStatusStr = item.optString("availabilityStatus", "AVAILABLE")
        val availabilityStatus = try { AvailabilityStatus.valueOf(availStatusStr) } catch (_: Exception) { AvailabilityStatus.AVAILABLE }

        val propTypeStr = item.optString("propertyType", "APARTMENT")
        val propertyType = try { PropertyType.valueOf(propTypeStr) } catch (_: Exception) { PropertyType.APARTMENT }

        val furnStr = item.optString("furnishing", "SEMI_FURNISHED")
        val furnishing = try { FurnishingStatus.valueOf(furnStr) } catch (_: Exception) { FurnishingStatus.SEMI_FURNISHED }

        return PropertyListing(
            id = item.optString("id", "prop_${System.currentTimeMillis()}"),
            title = item.optString("title", "Spiritual Ashram Living"),
            listingType = listingType,
            propertyType = propertyType,
            locality = item.optString("locality", "Raman Reti"),
            city = item.optString("city", "Vrindavan"),
            fullAddress = item.optString("fullAddress", item.optString("address", "")),
            landmark = item.optString("landmark", ""),
            price = item.optLong("price", 15000L),
            deposit = item.optLong("deposit", 30000L),
            maintenanceCharges = item.optLong("maintenanceCharges", 0L),
            bhk = item.optString("bhk", "2 BHK"),
            builtUpAreaSqFt = item.optInt("builtUpAreaSqFt", item.optInt("area", 1000)),
            carpetAreaSqFt = item.optInt("carpetAreaSqFt", 850),
            floor = item.optInt("floor", 1),
            totalFloors = item.optInt("totalFloors", 4),
            furnishing = furnishing,
            facing = item.optString("facing", "East"),
            availableFrom = item.optString("availableFrom", "Immediately"),
            preferredTenants = item.optString("preferredTenants", "Devotees / Pilgrims"),
            isPureSattvik = item.optBoolean("isPureSattvik", true),
            amenities = emptyList(),
            templeDistances = TempleDistances(
                premMandirKm = 1.2,
                bankeyBihariKm = 2.4,
                iskconKm = 0.8,
                janmabhoomiKm = 9.5,
                yamunaGhatKm = 2.0
            ),
            legalStatus = item.optString("legalStatus", "MVDA Compliant"),
            approvalStatus = approvalStatus,
            availabilityStatus = availabilityStatus,
            closedAtTimestamp = item.optLong("closedAt", 0L).takeIf { it > 0 },
            adminFeedback = item.optString("adminFeedback", null),
            isVerified = item.optBoolean("isVerified", item.optBoolean("verified", false)),
            photos = photoList,
            createdAtTimestamp = item.optLong("createdAt", System.currentTimeMillis()),
            loggedCoordinates = item.optString("loggedCoordinates", item.optString("coords", "27.5794° N, 77.6881° E")),
            loggedLocality = item.optString("loggedLocality", "Raman Reti, Vrindavan"),
            ownerId = item.optString("ownerId", "owner"),
            ownerName = item.optString("ownerName", item.optString("owner", "Devotee Owner")),
            ownerPhone = item.optString("ownerPhone", item.optString("phone", ""))
        )
    }
}
