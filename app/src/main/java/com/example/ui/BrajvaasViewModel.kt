package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.*
import com.example.data.repository.BrajvaasRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    PROPERTY_DETAIL,
    POST_PROPERTY,
    ADMIN_PORTAL,
    MESSAGES,
    CHAT_DETAIL,
    RENT_PAY,
    SUPPORT,
    AUTH,
    USER_ACTIVITY,
    MANAGED_AGREEMENTS,
    SHORT_STAYS,
    WEB_ADMIN_PANEL,
    CONTACT_US
}

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String
)

class BrajvaasViewModel(
    application: android.app.Application
) : androidx.lifecycle.AndroidViewModel(application) {

    private val repository: BrajvaasRepository = BrajvaasRepository(application.applicationContext)

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenStack = MutableStateFlow(listOf(AppScreen.HOME))

    // Selected Property
    private val _selectedPropertyId = MutableStateFlow<String?>(null)
    val selectedPropertyId: StateFlow<String?> = _selectedPropertyId.asStateFlow()

    // Active Chat Conversation
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    // Active Receipt for preview
    private val _activeReceiptRecord = MutableStateFlow<RentPaymentRecord?>(null)
    val activeReceiptRecord: StateFlow<RentPaymentRecord?> = _activeReceiptRecord.asStateFlow()

    // Filter & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(SearchFilter())
    val filter: StateFlow<SearchFilter> = _filter.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    // Notification / Toast banner
    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // Admin Mode toggle in top bar
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Admin filter tab (0: Pending, 1: Approved, 2: Rejected, 3: Users, 4: Leads, 5: Short Stays, 6: Locations)
    private val _adminTab = MutableStateFlow(0)
    val adminTab: StateFlow<Int> = _adminTab.asStateFlow()

    // Support Mitra quick bot messages
    private val _mitraChatMessages = MutableStateFlow<List<ChatMessage>>(getInitialMitraMessages())
    val mitraChatMessages: StateFlow<List<ChatMessage>> = _mitraChatMessages.asStateFlow()

    // Repository flows
    val allProperties = repository.properties
    val conversations = repository.conversations
    val allMessages = repository.messages
    val rentPayments = repository.rentPayments
    val supportTickets = repository.supportTickets

    // Auth & User Profile
    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn

    // User Management (Admin)
    val usersList: StateFlow<List<UserProfile>> = repository.usersList

    // Shortlist & Visited
    val shortlistedPropertyIds: StateFlow<Set<String>> = repository.shortlistedPropertyIds
    val visitedProperties: StateFlow<List<VisitedPropertyRecord>> = repository.visitedProperties

    // Short Stays (Vrindavan Airbnb)
    val shortStayBookings: StateFlow<List<ShortStayBooking>> = repository.shortStayBookings

    // User Interest Leads (Admin Contact Portal)
    val userInterestLeads: StateFlow<List<UserInterestLead>> = repository.userInterestLeads

    // Location Audit Logs (Exact User Location Tracking)
    val locationAuditLogs: StateFlow<List<UserLocationAuditLog>> = repository.locationAuditLogs

    // Embedded Web Server for Web Admin Panel
    private val adminWebServer = com.example.data.server.AdminWebServer(repository)
    private val _webServerUrl = MutableStateFlow("http://127.0.0.1:8080/admin")
    val webServerUrl: StateFlow<String> = _webServerUrl.asStateFlow()

    init {
        val port = adminWebServer.start(8080)
        if (port > 0) {
            _webServerUrl.value = "http://127.0.0.1:$port/admin"
        }
    }

    override fun onCleared() {
        super.onCleared()
        adminWebServer.stop()
    }

    fun getAdminWebHtml(): String = adminWebServer.generateWebAdminHtml()

    // Managed Agreements & Police Verification
    val agreements: StateFlow<List<RentalAgreementTracker>> = repository.agreements

    // Derived: Shortlisted Properties list
    val shortlistedProperties: StateFlow<List<PropertyListing>> = combine(
        repository.properties,
        repository.shortlistedPropertyIds
    ) { props, ids ->
        props.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived: Visited Properties list with timestamp
    val visitedPropertiesWithListing: StateFlow<List<Pair<PropertyListing, Long>>> = combine(
        repository.properties,
        repository.visitedProperties
    ) { props, visits ->
        val propMap = props.associateBy { it.id }
        visits.mapNotNull { v ->
            val p = propMap[v.propertyId]
            if (p != null) Pair(p, v.visitedTimestamp) else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived: Vrindavan Short Stays list (strictly available only)
    val vrindavanShortStays: StateFlow<List<PropertyListing>> = repository.properties.map { list ->
        list.filter {
            it.listingType == ListingType.SHORT_STAY &&
            it.city.equals("Vrindavan", ignoreCase = true) &&
            it.approvalStatus == ApprovalStatus.APPROVED &&
            it.availabilityStatus == AvailabilityStatus.AVAILABLE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered properties for seekers: Strictly ONLY properties where approvalStatus == APPROVED and availabilityStatus == AVAILABLE!
    val visibleProperties: StateFlow<List<PropertyListing>> = combine(
        repository.properties,
        _searchQuery,
        _filter
    ) { props, query, f ->
        props.filter { p ->
            p.approvalStatus == ApprovalStatus.APPROVED &&
            p.availabilityStatus == AvailabilityStatus.AVAILABLE &&
            p.listingType == f.listingType &&
            (f.locality == "All" || p.locality.equals(f.locality, ignoreCase = true) || p.city.equals(f.locality, ignoreCase = true)) &&
            (f.bhkList.isEmpty() || f.bhkList.contains(p.bhk)) &&
            (f.propertyTypes.isEmpty() || f.propertyTypes.contains(p.propertyType)) &&
            (!f.onlyVerified || p.isVerified) &&
            (!f.onlySattvik || p.isPureSattvik) &&
            (p.price in f.minPrice..f.maxPrice) &&
            (p.templeDistances.premMandirKm <= f.maxPremMandirDistanceKm) &&
            (p.templeDistances.bankeyBihariKm <= f.maxBankeyBihariDistanceKm) &&
            (query.isBlank() ||
                    p.title.contains(query, ignoreCase = true) ||
                    p.locality.contains(query, ignoreCase = true) ||
                    p.city.contains(query, ignoreCase = true) ||
                    p.landmark.contains(query, ignoreCase = true) ||
                    p.bhk.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Archived Properties (Sold, Rented Out, Fully Booked) - ONLY visible to Admin
    val archivedProperties: StateFlow<List<PropertyListing>> = repository.properties.map { list ->
        list.filter { it.availabilityStatus != AvailabilityStatus.AVAILABLE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My Posted Properties (for current user / poster to view and mark sold/rented)
    val myProperties: StateFlow<List<PropertyListing>> = combine(
        repository.properties,
        repository.currentUser
    ) { props, user ->
        if (user == null) emptyList()
        else props.filter {
            it.ownerPhone == user.phone ||
            it.ownerName.equals(user.fullName, ignoreCase = true) ||
            it.ownerId == user.id
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently viewed property in detail screen
    val selectedProperty: StateFlow<PropertyListing?> = combine(
        repository.properties,
        _selectedPropertyId
    ) { props, id ->
        props.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current conversation messages
    val currentChatMessages: StateFlow<List<ChatMessage>> = combine(
        repository.messages,
        _activeConversationId
    ) { msgMap, convId ->
        if (convId != null) msgMap[convId] ?: emptyList() else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending listings count for admin badge
    val pendingListingsCount: StateFlow<Int> = repository.properties.map { list ->
        list.count { it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Total leads count for admin
    val totalLeadsCount: StateFlow<Int> = repository.userInterestLeads.map { list ->
        list.count { it.status.startsWith("New") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenStack.update { it + screen }
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        val stack = _screenStack.value
        return if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            _screenStack.value = newStack
            _currentScreen.value = newStack.last()
            true
        } else {
            false
        }
    }

    fun selectProperty(property: PropertyListing) {
        _selectedPropertyId.value = property.id
        // Record visit history automatically
        recordPropertyVisit(property.id)
        navigateTo(AppScreen.PROPERTY_DETAIL)
    }

    fun toggleShortlist(propertyId: String) {
        repository.toggleShortlist(propertyId)
        val isShortlisted = !repository.shortlistedPropertyIds.value.contains(propertyId)
        showNotification(if (isShortlisted) "Shortlisted! Admin notified for your assisted darshan visit." else "Removed from Shortlist")
    }

    fun recordPropertyVisit(propertyId: String) {
        repository.recordPropertyVisit(propertyId)
    }

    // Auth actions: Automatically redirects to homepage as requested!
    fun login(phoneOrEmail: String, role: UserRole = UserRole.SEEKER) {
        repository.login(phoneOrEmail, role)
        showNotification("Welcome back to Brajvaas, ${repository.currentUser.value?.fullName}!")
        // Explicitly redirect to Homepage
        _screenStack.value = listOf(AppScreen.HOME)
        _currentScreen.value = AppScreen.HOME
    }

    fun signUp(name: String, phone: String, email: String, role: UserRole, city: String) {
        repository.signUp(name, phone, email, role, city)
        showNotification("Account created! Welcome to Brajvaas.")
        // Explicitly redirect to Homepage
        _screenStack.value = listOf(AppScreen.HOME)
        _currentScreen.value = AppScreen.HOME
    }

    fun logout() {
        repository.logout()
        showNotification("Logged out successfully.")
        _screenStack.value = listOf(AppScreen.HOME)
        _currentScreen.value = AppScreen.HOME
    }

    // Short Stays booking
    fun bookShortStay(
        property: PropertyListing,
        guestName: String,
        guestPhone: String,
        checkInDate: String,
        checkOutDate: String,
        guestsCount: Int,
        nightsCount: Int
    ) {
        repository.bookShortStay(
            property = property,
            guestName = guestName,
            guestPhone = guestPhone,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            guestsCount = guestsCount,
            nightsCount = nightsCount
        )
        showNotification("Short stay booked! ₹200 fee applied. Brajvaas Admin will coordinate check-in.")
        navigateTo(AppScreen.USER_ACTIVITY)
    }

    // Admin Lead updates
    fun updateLeadStatus(leadId: String, newStatus: String, notes: String = "") {
        repository.updateLeadStatus(leadId, newStatus, notes)
        showNotification("Lead status updated to '$newStatus'")
    }

    // Admin User management
    fun deleteUser(userId: String) {
        repository.deleteUser(userId)
        showNotification("User removed from system.")
    }

    fun toggleUserKyc(userId: String) {
        repository.toggleUserKyc(userId)
        showNotification("User KYC verification status updated.")
    }

    // Admin Property management
    fun deleteProperty(propertyId: String) {
        repository.deleteProperty(propertyId)
        showNotification("Property removed.")
    }

    // Agreement & Police Verification actions
    fun requestRentAgreementAndVerification(property: PropertyListing) {
        val tenantName = currentUser.value?.fullName ?: "Mukesh Garg"
        repository.requestRentAgreementAndVerification(property, tenantName)
        showNotification("Agreement request initiated! 15 days brokerage applies. Brajvaas will complete agreement & police verification.")
        navigateTo(AppScreen.MANAGED_AGREEMENTS)
    }

    fun advanceAgreementStep(agreementId: String) {
        repository.advanceAgreementStep(agreementId)
        showNotification("Agreement step updated!")
    }

    fun advancePoliceVerification(agreementId: String) {
        repository.advancePoliceVerification(agreementId)
        showNotification("Police verification status updated!")
    }

    fun openChatWithPropertyOwner(property: PropertyListing) {
        val convId = repository.getOrCreateConversation(property)
        _activeConversationId.value = convId
        navigateTo(AppScreen.CHAT_DETAIL)
    }

    fun openConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        navigateTo(AppScreen.CHAT_DETAIL)
    }

    fun sendChatMessage(text: String) {
        val convId = _activeConversationId.value ?: return
        val conv = repository.conversations.value.find { it.id == convId } ?: return
        repository.sendMessage(convId, conv.propertyId, text)
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setListingType(type: ListingType) {
        _filter.update { it.copy(listingType = type) }
    }

    fun setLocalityFilter(loc: String) {
        _filter.update { it.copy(locality = loc) }
    }

    fun updateFilter(newFilter: SearchFilter) {
        _filter.value = newFilter
        _showFilterSheet.value = false
    }

    fun toggleFilterSheet(show: Boolean) {
        _showFilterSheet.value = show
    }

    fun toggleAdminMode() {
        val next = !_isAdminMode.value
        _isAdminMode.value = next
        if (next) {
            showNotification("Switched to Admin Mode: View user management, leads, and moderation")
        }
    }

    fun setAdminTab(tab: Int) {
        _adminTab.value = tab
    }

    fun adminApproveProperty(propertyId: String, grantVerified: Boolean = true) {
        repository.adminApprove(propertyId, grantVerified)
        showNotification("Property approved! It is now visible to all seekers.")
    }

    fun adminRejectProperty(propertyId: String, reason: String) {
        repository.adminReject(propertyId, reason)
        showNotification("Property rejected with note sent to poster.")
    }

    fun adminToggleVerification(propertyId: String) {
        repository.adminToggleVerification(propertyId)
    }

    fun markPropertyAvailability(propertyId: String, newStatus: AvailabilityStatus) {
        repository.updatePropertyAvailability(propertyId, newStatus)
        val statusLabel = when (newStatus) {
            AvailabilityStatus.RENTED_OUT -> "Rented Out"
            AvailabilityStatus.SOLD -> "Sold"
            AvailabilityStatus.FULLY_BOOKED -> "Fully Booked"
            AvailabilityStatus.AVAILABLE -> "Available (Live)"
        }
        showNotification("Property marked as $statusLabel.")
    }

    fun updatePropertyDetails(updated: PropertyListing) {
        repository.updatePropertyDetails(updated)
        showNotification("Property '${updated.title}' updated successfully.")
    }

    fun clearAllPostings() {
        repository.clearAllProperties()
        showNotification("All postings cleared. Starting fresh from beginning.")
    }

    fun submitNewProperty(
        title: String,
        listingType: ListingType,
        propertyType: PropertyType,
        locality: String,
        city: String,
        fullAddress: String,
        landmark: String,
        price: Long,
        deposit: Long,
        maintenance: Long,
        bhk: String,
        builtUpArea: Int,
        carpetArea: Int,
        floor: Int,
        totalFloors: Int,
        furnishing: FurnishingStatus,
        facing: String,
        preferredTenants: String,
        isPureSattvik: Boolean,
        amenities: List<String>,
        premMandirKm: Double,
        bankeyBihariKm: Double,
        iskconKm: Double,
        janmabhoomiKm: Double,
        yamunaGhatKm: Double,
        legalStatus: String,
        ownerName: String,
        ownerPhone: String,
        photos: List<String> = emptyList(),
        loggedCoordinates: String = "27.5794° N, 77.6881° E",
        loggedLocality: String = "Vrindavan"
    ) {
        val newListing = PropertyListing(
            id = "prop_${System.currentTimeMillis()}",
            title = title,
            listingType = listingType,
            propertyType = propertyType,
            locality = locality,
            city = city,
            fullAddress = fullAddress,
            landmark = landmark,
            price = price,
            deposit = deposit,
            maintenanceCharges = maintenance,
            bhk = bhk,
            builtUpAreaSqFt = builtUpArea,
            carpetAreaSqFt = carpetArea,
            floor = floor,
            totalFloors = totalFloors,
            furnishing = furnishing,
            facing = facing,
            availableFrom = "Immediately",
            preferredTenants = preferredTenants,
            isPureSattvik = isPureSattvik,
            amenities = amenities,
            templeDistances = TempleDistances(
                premMandirKm = premMandirKm,
                bankeyBihariKm = bankeyBihariKm,
                iskconKm = iskconKm,
                janmabhoomiKm = janmabhoomiKm,
                yamunaGhatKm = yamunaGhatKm
            ),
            legalStatus = legalStatus,
            approvalStatus = ApprovalStatus.PENDING_APPROVAL,
            isVerified = false,
            imageDrawableRes = R.drawable.img_ashram_villa,
            photos = photos,
            loggedCoordinates = loggedCoordinates,
            loggedLocality = loggedLocality,
            ownerName = ownerName,
            ownerPhone = ownerPhone
        )

        repository.postProperty(newListing)
        showNotification("Listing submitted! Under Admin Review. Location logged.")
        navigateBack()
    }

    fun payRent(paymentId: String) {
        repository.payRent(paymentId)
        val record = repository.rentPayments.value.find { it.id == paymentId }
        _activeReceiptRecord.value = record?.copy(status = PaymentStatus.PAID)
        showNotification("Rent paid successfully! Official digital receipt generated.")
    }

    fun viewReceipt(record: RentPaymentRecord) {
        _activeReceiptRecord.value = record
    }

    fun dismissReceipt() {
        _activeReceiptRecord.value = null
    }

    fun createSupportTicket(category: String, subject: String, desc: String) {
        repository.createSupportTicket(category, subject, desc)
        showNotification("Ticket raised! Brajvaas Mitra will review within 2 business hours.")
    }

    fun sendMitraMessage(text: String) {
        val now = System.currentTimeMillis()
        val userMsg = ChatMessage(
            id = "mitra_${now}",
            conversationId = "mitra_bot",
            propertyId = "",
            senderId = "me",
            senderName = "You",
            messageText = text,
            timestamp = now,
            isFromMe = true
        )

        val botReply = when {
            text.contains("short stay", ignoreCase = true) || text.contains("homestay", ignoreCase = true) ->
                "Brajvaas offers Vrindavan Short Stays & Homestays exclusively for Vrindavan. A flat ₹200 platform fee is calculated upon booking, irrespective of duration."
            text.contains("brokerage", ignoreCase = true) || text.contains("charge", ignoreCase = true) ->
                "For long-term rentals, we charge 15 days rent as brokerage. We draft the 11-month e-stamped agreement and complete Mathura police verification directly."
            text.contains("location", ignoreCase = true) || text.contains("address", ignoreCase = true) ->
                "Every property is GPS audited while the full exact address remains confidential for owner privacy until booking or visit confirmation."
            else ->
                "Radhe Radhe! Brajvaas Mitra is at your service for Vrindavan properties, holy short stays, and rental agreements."
        }

        val botMsg = ChatMessage(
            id = "mitra_${now + 1000}",
            conversationId = "mitra_bot",
            propertyId = "",
            senderId = "mitra",
            senderName = "Brajvaas Mitra",
            messageText = botReply,
            timestamp = now + 1000,
            isFromMe = false
        )

        _mitraChatMessages.update { it + listOf(userMsg, botMsg) }
    }

    fun showNotification(msg: String) {
        _notification.value = UiNotification(message = msg)
    }

    fun clearNotification() {
        _notification.value = null
    }

    private fun getInitialMitraMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "m_intro",
                conversationId = "mitra_bot",
                propertyId = "",
                senderId = "mitra",
                senderName = "Brajvaas Mitra",
                messageText = "Radhe Radhe! Welcome to Brajvaas. Explore Vrindavan short stays (₹200 flat fee), rentals with police verification, or managed sales.",
                timestamp = System.currentTimeMillis() - 10000,
                isFromMe = false
            )
        )
    }
}
