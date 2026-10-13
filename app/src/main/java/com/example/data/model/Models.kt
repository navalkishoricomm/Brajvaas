package com.example.data.model

enum class ListingType(val label: String) {
    RENT("Rent"),
    SELL("Sell"),
    SHORT_STAY("Short Stay (Vrindavan)")
}

enum class PropertyType(val displayName: String) {
    APARTMENT("Apartment / Flat"),
    ASHRAM_STUDIO("Ashram Studio / 1RK"),
    INDEPENDENT_VILLA("Independent Kothi / Villa"),
    BUILDER_FLOOR("Builder Floor"),
    PLOT_LAND("Plot / Holy Land"),
    COMMERCIAL("Commercial / Dharamshala"),
    VRINDAVAN_HOMESTAY("Vrindavan Devotee Homestay / Suite")
}

enum class FurnishingStatus(val displayName: String) {
    FURNISHED("Fully Furnished"),
    SEMI_FURNISHED("Semi-Furnished"),
    UNFURNISHED("Unfurnished")
}

enum class ApprovalStatus(val displayName: String) {
    PENDING_APPROVAL("Pending Admin Approval"),
    APPROVED("Approved & Live"),
    REJECTED("Rejected")
}

enum class PaymentStatus(val displayName: String) {
    PAID("Paid"),
    PENDING("Due Soon"),
    OVERDUE("Overdue")
}

data class TempleDistances(
    val premMandirKm: Double,
    val bankeyBihariKm: Double,
    val iskconKm: Double,
    val janmabhoomiKm: Double,
    val yamunaGhatKm: Double
)

data class PropertyListing(
    val id: String,
    val title: String,
    val listingType: ListingType,
    val propertyType: PropertyType,
    val locality: String, // e.g. "Raman Reti", "Chhatikara Road"
    val city: String,     // "Vrindavan", "Mathura", "Govardhan"
    val fullAddress: String,
    val landmark: String,
    val price: Long,      // Monthly rent in INR, Total Sale Price, or Nightly Rate
    val pricePerNight: Long = if (listingType == ListingType.SHORT_STAY) price else 0L,
    val deposit: Long = 0,
    val maintenanceCharges: Long = 0,
    val bhk: String,      // "1 RK", "1 BHK", "2 BHK", "3 BHK", "Plot", "Homestay Suite", etc.
    val builtUpAreaSqFt: Int,
    val carpetAreaSqFt: Int,
    val floor: Int = 1,
    val totalFloors: Int = 4,
    val furnishing: FurnishingStatus,
    val facing: String = "East / Temple Facing",
    val availableFrom: String = "Immediately",
    val preferredTenants: String = "Families & Pilgrims / Devotees",
    val isPureSattvik: Boolean = true,
    val amenities: List<String>,
    val templeDistances: TempleDistances,
    val legalStatus: String = "MVDA Approved & Freehold",
    val approvalStatus: ApprovalStatus = ApprovalStatus.PENDING_APPROVAL,
    val adminFeedback: String? = null,
    val isVerified: Boolean = false,
    val imageDrawableRes: Int? = null,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val loggedCoordinates: String = "27.5794° N, 77.6881° E",
    val loggedLocality: String = "Raman Reti, Vrindavan",
    // Strictly confidential - never exposed to seekers in search or details
    val ownerId: String = "user_default",
    val ownerName: String = "Property Owner",
    val ownerPhone: String = "+91 98765 43210"
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val propertyId: String,
    val senderId: String,
    val senderName: String,
    val messageText: String,
    val timestamp: Long,
    val isFromMe: Boolean
)

data class ConversationThread(
    val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val propertyLocality: String,
    val propertyPrice: Long,
    val listingType: ListingType,
    val recipientName: String, // e.g. "Property Owner"
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0
)

data class RentPaymentRecord(
    val id: String,
    val propertyTitle: String,
    val locality: String,
    val landlordName: String,
    val tenantName: String,
    val monthYear: String,
    val baseRent: Long,
    val maintenanceCharges: Long,
    val totalAmount: Long,
    val dueDate: String,
    val status: PaymentStatus,
    val paidDate: String? = null,
    val transactionId: String? = null,
    val paymentMode: String = "UPI / Brajvaas Pay",
    val receiptNumber: String
)

data class SupportTicket(
    val id: String,
    val category: String,
    val subject: String,
    val description: String,
    val status: String,
    val createdAtFormatted: String
)

data class SearchFilter(
    val listingType: ListingType = ListingType.RENT,
    val locality: String = "All", // "All" or specific locality
    val minPrice: Long = 0,
    val maxPrice: Long = 250000000,
    val bhkList: Set<String> = emptySet(),
    val propertyTypes: Set<PropertyType> = emptySet(),
    val maxPremMandirDistanceKm: Double = 20.0,
    val maxBankeyBihariDistanceKm: Double = 20.0,
    val onlyVerified: Boolean = false,
    val onlySattvik: Boolean = false,
    val furnishing: FurnishingStatus? = null
)

enum class UserRole(val label: String) {
    SEEKER("Devotee / Tenant"),
    LANDLORD("Property Owner / Landlord"),
    ADMIN("Brajvaas Admin")
}

data class UserProfile(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val role: UserRole,
    val city: String = "Vrindavan",
    val isKycVerified: Boolean = true,
    val joinedDate: String = "October 2026",
    val lastKnownCoordinates: String = "27.5794° N, 77.6881° E",
    val lastKnownLocality: String = "Raman Reti, Vrindavan, Mathura District"
)

enum class AgreementStepStatus(val label: String) {
    DRAFTING("Drafting by Brajvaas Legal"),
    E_STAMPED("E-Stamped (₹100 Stamp)"),
    SIGNED_BY_BOTH("Signed by Tenant & Owner"),
    COMPLETED("Active & Notarized")
}

enum class PoliceVerificationStatus(val label: String) {
    DOCUMENTS_COLLECTED("Aadhaar & Photo Submitted"),
    BRAJVAAS_INSPECTION("Brajvaas Physical KYC Done"),
    UPCOP_POLICE_SUBMITTED("Submitted to Mathura Police"),
    VERIFIED_CLEARED("Police Verification Cleared ✓")
}

data class RentalAgreementTracker(
    val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val locality: String,
    val monthlyRent: Long,
    val brokerage15DaysFee: Long, // 15 Days Rent charged to each party
    val tenantName: String,
    val landlordName: String,
    val agreementStatus: AgreementStepStatus,
    val policeVerificationStatus: PoliceVerificationStatus,
    val agreementNumber: String,
    val policeAckNumber: String,
    val initiatedDate: String,
    val targetPossessionDate: String,
    val isBrokeragePaidByTenant: Boolean = true,
    val isBrokeragePaidByLandlord: Boolean = false
)

data class VisitedPropertyRecord(
    val propertyId: String,
    val visitedTimestamp: Long,
    val userCoordinates: String = "27.5794° N, 77.6881° E"
)

// Short Stays (like Airbnb for Vrindavan locality only - Rs 200 from tenant, Rs 200 from landlord)
data class ShortStayBooking(
    val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val locality: String = "Vrindavan",
    val guestName: String,
    val guestPhone: String,
    val hostName: String,
    val hostPhone: String,
    val checkInDate: String,
    val checkOutDate: String,
    val numberOfGuests: Int,
    val numberOfNights: Int,
    val ratePerNight: Long,
    val staySubtotal: Long, // ratePerNight * numberOfNights
    val tenantFee: Long = 200L, // Fixed flat Rs 200 per stay irrespective of duration
    val landlordFee: Long = 200L, // Fixed flat Rs 200 per stay irrespective of duration
    val totalGuestPayable: Long, // staySubtotal + tenantFee
    val guestExactCoordinates: String = "27.5794° N, 77.6881° E (Near ISKCON, Vrindavan)",
    val status: String = "Confirmed",
    val bookingTimestamp: Long = System.currentTimeMillis()
)

// User Interest / Like Lead for Admin Management
data class UserInterestLead(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val userEmail: String,
    val userExactLocation: String, // Exact logged GPS & Locality
    val propertyId: String,
    val propertyTitle: String,
    val propertyLocality: String,
    val listingType: ListingType,
    val propertyPrice: Long,
    val leadType: String, // "Liked / Shortlisted", "Visit Requested", "Agreement Initiated", "Short Stay Booked"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "New Lead - Pending Contact", // "New Lead", "Contacted", "Visit Scheduled", "Closed"
    val adminNotes: String = ""
)

// User Location Audit Log
data class UserLocationAuditLog(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val actionType: String, // "POSTED_PROPERTY", "RENT_INQUIRY", "BUY_INQUIRY", "SHORT_STAY_BOOKING", "PROPERTY_LIKED"
    val actionDescription: String,
    val propertyId: String?,
    val propertyTitle: String?,
    val exactCoordinates: String, // e.g. "27.5794° N, 77.6881° E"
    val geoLocality: String, // e.g. "Raman Reti, Vrindavan"
    val timestamp: Long = System.currentTimeMillis()
)
