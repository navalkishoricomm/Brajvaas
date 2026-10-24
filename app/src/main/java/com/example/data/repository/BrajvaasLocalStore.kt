package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * On-device local persistence for Brajvaas.
 * Ensures properties, agreements, and offline drafts survive app restarts.
 */
class BrajvaasLocalStore(private val context: Context) {

    private val TAG = "BrajvaasLocalStore"
    private val propertiesFile: File
        get() = File(context.filesDir, "brajvaas_local_properties.json")

    private val agreementsFile: File
        get() = File(context.filesDir, "brajvaas_local_agreements.json")

    fun saveProperties(list: List<PropertyListing>) {
        try {
            val arr = JSONArray()
            for (p in list) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("listingType", p.listingType.name)
                    put("propertyType", p.propertyType.name)
                    put("locality", p.locality)
                    put("city", p.city)
                    put("fullAddress", p.fullAddress)
                    put("landmark", p.landmark)
                    put("price", p.price)
                    put("deposit", p.deposit)
                    put("maintenanceCharges", p.maintenanceCharges)
                    put("bhk", p.bhk)
                    put("builtUpAreaSqFt", p.builtUpAreaSqFt)
                    put("carpetAreaSqFt", p.carpetAreaSqFt)
                    put("floor", p.floor)
                    put("totalFloors", p.totalFloors)
                    put("furnishing", p.furnishing.name)
                    put("facing", p.facing)
                    put("preferredTenants", p.preferredTenants)
                    put("isPureSattvik", p.isPureSattvik)
                    put("legalStatus", p.legalStatus)
                    put("approvalStatus", p.approvalStatus.name)
                    put("availabilityStatus", p.availabilityStatus.name)
                    put("closedAtTimestamp", p.closedAtTimestamp ?: 0L)
                    put("isVerified", p.isVerified)
                    put("photos", JSONArray(p.photos))
                    put("loggedCoordinates", p.loggedCoordinates)
                    put("loggedLocality", p.loggedLocality)
                    put("ownerId", p.ownerId)
                    put("ownerName", p.ownerName)
                    put("ownerPhone", p.ownerPhone)
                }
                arr.put(obj)
            }
            propertiesFile.writeText(arr.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving properties locally: ${e.message}")
        }
    }

    fun loadProperties(): List<PropertyListing>? {
        if (!propertiesFile.exists()) return null
        return try {
            val text = propertiesFile.readText()
            val arr = JSONArray(text)
            val list = mutableListOf<PropertyListing>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val typeStr = item.optString("listingType", "RENT")
                val listingType = try { ListingType.valueOf(typeStr) } catch (_: Exception) { ListingType.RENT }

                val appStatusStr = item.optString("approvalStatus", "PENDING_APPROVAL")
                val approvalStatus = try { ApprovalStatus.valueOf(appStatusStr) } catch (_: Exception) { ApprovalStatus.PENDING_APPROVAL }

                val availStatusStr = item.optString("availabilityStatus", "AVAILABLE")
                val availabilityStatus = try { AvailabilityStatus.valueOf(availStatusStr) } catch (_: Exception) { AvailabilityStatus.AVAILABLE }

                val photosArr = item.optJSONArray("photos")
                val photos = mutableListOf<String>()
                if (photosArr != null) {
                    for (p in 0 until photosArr.length()) photos.add(photosArr.getString(p))
                }

                list.add(
                    PropertyListing(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        listingType = listingType,
                        propertyType = PropertyType.APARTMENT,
                        locality = item.optString("locality"),
                        city = item.optString("city"),
                        fullAddress = item.optString("fullAddress"),
                        landmark = item.optString("landmark"),
                        price = item.optLong("price"),
                        deposit = item.optLong("deposit"),
                        maintenanceCharges = item.optLong("maintenanceCharges"),
                        bhk = item.optString("bhk"),
                        builtUpAreaSqFt = item.optInt("builtUpAreaSqFt"),
                        carpetAreaSqFt = item.optInt("carpetAreaSqFt"),
                        furnishing = FurnishingStatus.SEMI_FURNISHED,
                        amenities = emptyList(),
                        templeDistances = TempleDistances(1.2, 2.4, 0.8, 9.5, 2.0),
                        approvalStatus = approvalStatus,
                        availabilityStatus = availabilityStatus,
                        closedAtTimestamp = item.optLong("closedAtTimestamp").takeIf { it > 0 },
                        isVerified = item.optBoolean("isVerified"),
                        photos = photos,
                        loggedCoordinates = item.optString("loggedCoordinates", "27.5794° N, 77.6881° E"),
                        loggedLocality = item.optString("loggedLocality", "Vrindavan"),
                        ownerId = item.optString("ownerId"),
                        ownerName = item.optString("ownerName"),
                        ownerPhone = item.optString("ownerPhone")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading properties locally: ${e.message}")
            null
        }
    }

    fun saveAgreements(list: List<RentalAgreementTracker>) {
        try {
            val arr = JSONArray()
            for (a in list) {
                val obj = JSONObject().apply {
                    put("id", a.id)
                    put("propertyId", a.propertyId)
                    put("propertyTitle", a.propertyTitle)
                    put("locality", a.locality)
                    put("monthlyRent", a.monthlyRent)
                    put("brokerage15DaysFee", a.brokerage15DaysFee)
                    put("tenantName", a.tenantName)
                    put("landlordName", a.landlordName)
                    put("agreementStatus", a.agreementStatus.name)
                    put("policeVerificationStatus", a.policeVerificationStatus.name)
                    put("agreementNumber", a.agreementNumber)
                    put("policeAckNumber", a.policeAckNumber)
                    put("initiatedDate", a.initiatedDate)
                    put("targetPossessionDate", a.targetPossessionDate)
                }
                arr.put(obj)
            }
            agreementsFile.writeText(arr.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving agreements locally: ${e.message}")
        }
    }

    fun loadAgreements(): List<RentalAgreementTracker>? {
        if (!agreementsFile.exists()) return null
        return try {
            val text = agreementsFile.readText()
            val arr = JSONArray(text)
            val list = mutableListOf<RentalAgreementTracker>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val statusStr = item.optString("agreementStatus", "DRAFTING")
                val status = try { AgreementStepStatus.valueOf(statusStr) } catch (_: Exception) { AgreementStepStatus.DRAFTING }

                val pvStr = item.optString("policeVerificationStatus", "DOCUMENTS_COLLECTED")
                val pvStatus = try { PoliceVerificationStatus.valueOf(pvStr) } catch (_: Exception) { PoliceVerificationStatus.DOCUMENTS_COLLECTED }

                list.add(
                    RentalAgreementTracker(
                        id = item.optString("id"),
                        propertyId = item.optString("propertyId"),
                        propertyTitle = item.optString("propertyTitle"),
                        locality = item.optString("locality"),
                        monthlyRent = item.optLong("monthlyRent"),
                        brokerage15DaysFee = item.optLong("brokerage15DaysFee"),
                        tenantName = item.optString("tenantName"),
                        landlordName = item.optString("landlordName"),
                        agreementStatus = status,
                        policeVerificationStatus = pvStatus,
                        agreementNumber = item.optString("agreementNumber"),
                        policeAckNumber = item.optString("policeAckNumber"),
                        initiatedDate = item.optString("initiatedDate"),
                        targetPossessionDate = item.optString("targetPossessionDate")
                    )
                )
            }
            list
        } catch (e: Exception) {
            null
        }
    }

    fun clearAll() {
        try {
            propertiesFile.delete()
            agreementsFile.delete()
        } catch (_: Exception) {}
    }
}
