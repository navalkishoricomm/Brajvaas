package com.example.data.repository

import com.example.R
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class BrajvaasRepository {

    private val _properties = MutableStateFlow<List<PropertyListing>>(getInitialProperties())
    val properties: StateFlow<List<PropertyListing>> = _properties.asStateFlow()

    // Authentication State
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "user_mukesh_01",
            fullName = "Mukesh Garg",
            phone = "+91 98765 43210",
            email = "mukgarg11@gmail.com",
            role = UserRole.SEEKER,
            city = "Vrindavan",
            lastKnownCoordinates = "27.5794° N, 77.6881° E",
            lastKnownLocality = "Raman Reti, Vrindavan"
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // All Registered Users (For Admin User Management)
    private val _usersList = MutableStateFlow<List<UserProfile>>(getInitialUsers())
    val usersList: StateFlow<List<UserProfile>> = _usersList.asStateFlow()

    // Shortlisted Property IDs
    private val _shortlistedPropertyIds = MutableStateFlow<Set<String>>(setOf("prop_1", "prop_short_1"))
    val shortlistedPropertyIds: StateFlow<Set<String>> = _shortlistedPropertyIds.asStateFlow()

    // Visited Property Records
    private val _visitedProperties = MutableStateFlow<List<VisitedPropertyRecord>>(
        listOf(
            VisitedPropertyRecord("prop_1", System.currentTimeMillis() - 7200000, "27.5794° N, 77.6881° E"),
            VisitedPropertyRecord("prop_short_1", System.currentTimeMillis() - 14400000, "27.5812° N, 77.6795° E"),
            VisitedPropertyRecord("prop_2", System.currentTimeMillis() - 86400000, "27.5681° N, 77.6942° E")
        )
    )
    val visitedProperties: StateFlow<List<VisitedPropertyRecord>> = _visitedProperties.asStateFlow()

    // Short Stays Bookings (Vrindavan Airbnb - Flat Rs 200 fee from tenant & landlord)
    private val _shortStayBookings = MutableStateFlow<List<ShortStayBooking>>(getInitialShortStayBookings())
    val shortStayBookings: StateFlow<List<ShortStayBooking>> = _shortStayBookings.asStateFlow()

    // User Interest Leads (Admin panel tracks when a user likes/shortlists/inquiries a property)
    private val _userInterestLeads = MutableStateFlow<List<UserInterestLead>>(getInitialInterestLeads())
    val userInterestLeads: StateFlow<List<UserInterestLead>> = _userInterestLeads.asStateFlow()

    // Exact Location Audit Logs (Logs location of whoever is posting, taking on rent, buying, or short stays)
    private val _locationAuditLogs = MutableStateFlow<List<UserLocationAuditLog>>(getInitialLocationLogs())
    val locationAuditLogs: StateFlow<List<UserLocationAuditLog>> = _locationAuditLogs.asStateFlow()

    // Managed Rent Agreements & Police Verification
    private val _agreements = MutableStateFlow<List<RentalAgreementTracker>>(getInitialAgreements())
    val agreements: StateFlow<List<RentalAgreementTracker>> = _agreements.asStateFlow()

    private val _conversations = MutableStateFlow<List<ConversationThread>>(getInitialConversations())
    val conversations: StateFlow<List<ConversationThread>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(getInitialMessages())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _rentPayments = MutableStateFlow<List<RentPaymentRecord>>(getInitialRentPayments())
    val rentPayments: StateFlow<List<RentPaymentRecord>> = _rentPayments.asStateFlow()

    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(getInitialSupportTickets())
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    // Auth actions
    fun login(phoneOrEmail: String, role: UserRole = UserRole.SEEKER) {
        val name = if (phoneOrEmail.contains("@")) {
            phoneOrEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        } else "Devotee User"

        val coordinates = "27.5794° N, 77.6881° E"
        val locality = "Raman Reti, Vrindavan"

        val user = UserProfile(
            id = "user_${System.currentTimeMillis()}",
            fullName = name,
            phone = if (phoneOrEmail.contains("@")) "+91 98765 43210" else phoneOrEmail,
            email = if (phoneOrEmail.contains("@")) phoneOrEmail else "$phoneOrEmail@brajvaas.in",
            role = role,
            city = "Vrindavan",
            lastKnownCoordinates = coordinates,
            lastKnownLocality = locality
        )
        _currentUser.value = user
        _isLoggedIn.value = true

        // Upsert into users list
        _usersList.update { list ->
            if (list.none { it.phone == user.phone || it.email == user.email }) {
                listOf(user) + list
            } else list
        }
    }

    fun signUp(name: String, phone: String, email: String, role: UserRole, city: String) {
        val coordinates = "27.5812° N, 77.6795° E"
        val locality = "$city, Mathura District"

        val user = UserProfile(
            id = "user_${System.currentTimeMillis()}",
            fullName = name,
            phone = phone,
            email = email,
            role = role,
            city = city,
            lastKnownCoordinates = coordinates,
            lastKnownLocality = locality
        )
        _currentUser.value = user
        _isLoggedIn.value = true

        _usersList.update { listOf(user) + it }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    // Shortlist / Likes Action - Automatically generates Admin Interest Lead!
    fun toggleShortlist(propertyId: String) {
        val prop = _properties.value.find { it.id == propertyId }
        val user = _currentUser.value
        val isNowShortlisted = !_shortlistedPropertyIds.value.contains(propertyId)

        _shortlistedPropertyIds.update { set ->
            if (isNowShortlisted) set + propertyId else set - propertyId
        }

        if (isNowShortlisted && prop != null && user != null) {
            val coords = user.lastKnownCoordinates
            val loc = user.lastKnownLocality

            // 1. Log Location for Liked / Shortlisted action
            logUserLocation(
                userId = user.id,
                userName = user.fullName,
                userPhone = user.phone,
                actionType = "PROPERTY_LIKED",
                actionDescription = "User shortlisted ${prop.title} (${prop.bhk})",
                propertyId = prop.id,
                propertyTitle = prop.title,
                coordinates = coords,
                geoLocality = loc
            )

            // 2. Generate Admin Lead so Admin sees interest and contacts the user!
            val newLead = UserInterestLead(
                id = "LEAD-${(1000..9999).random()}",
                userId = user.id,
                userName = user.fullName,
                userPhone = user.phone,
                userEmail = user.email,
                userExactLocation = "$coords ($loc)",
                propertyId = prop.id,
                propertyTitle = prop.title,
                propertyLocality = "${prop.locality}, ${prop.city}",
                listingType = prop.listingType,
                propertyPrice = prop.price,
                leadType = "Liked / Shortlisted",
                status = "New Lead - Pending Contact",
                adminNotes = "Devotee expressed interest. Ready for admin callback."
            )
            _userInterestLeads.update { listOf(newLead) + it }
        }
    }

    fun recordPropertyVisit(propertyId: String) {
        val now = System.currentTimeMillis()
        val user = _currentUser.value
        val coords = user?.lastKnownCoordinates ?: "27.5794° N, 77.6881° E"

        _visitedProperties.update { list ->
            listOf(VisitedPropertyRecord(propertyId, now, coords)) + list.filter { it.propertyId != propertyId }
        }
    }

    // Book Short Stay (Airbnb style for Vrindavan - Flat Rs 200 tenant + Rs 200 landlord fee)
    fun bookShortStay(
        property: PropertyListing,
        guestName: String,
        guestPhone: String,
        checkInDate: String,
        checkOutDate: String,
        guestsCount: Int,
        nightsCount: Int
    ) {
        val rate = if (property.pricePerNight > 0) property.pricePerNight else 1400L
        val subtotal = rate * nightsCount
        val tenantFee = 200L
        val landlordFee = 200L
        val totalPayable = subtotal + tenantFee

        val user = _currentUser.value
        val coords = user?.lastKnownCoordinates ?: "27.5794° N, 77.6881° E"
        val loc = "Raman Reti, Vrindavan"

        val booking = ShortStayBooking(
            id = "VRN-STAY-${(1000..9999).random()}",
            propertyId = property.id,
            propertyTitle = property.title,
            locality = property.locality,
            guestName = guestName,
            guestPhone = guestPhone,
            hostName = property.ownerName,
            hostPhone = property.ownerPhone,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            numberOfGuests = guestsCount,
            numberOfNights = nightsCount,
            ratePerNight = rate,
            staySubtotal = subtotal,
            tenantFee = tenantFee,
            landlordFee = landlordFee,
            totalGuestPayable = totalPayable,
            guestExactCoordinates = "$coords ($loc)",
            status = "Confirmed"
        )

        _shortStayBookings.update { listOf(booking) + it }

        // Log location of user booking short stay
        logUserLocation(
            userId = user?.id ?: "guest",
            userName = guestName,
            userPhone = guestPhone,
            actionType = "SHORT_STAY_BOOKING",
            actionDescription = "Booked Vrindavan Short Stay for $nightsCount nights (₹$totalPayable incl. ₹200 fee)",
            propertyId = property.id,
            propertyTitle = property.title,
            coordinates = coords,
            geoLocality = loc
        )

        // Generate Admin Lead
        val lead = UserInterestLead(
            id = "LEAD-${(1000..9999).random()}",
            userId = user?.id ?: "guest",
            userName = guestName,
            userPhone = guestPhone,
            userEmail = user?.email ?: "guest@brajvaas.in",
            userExactLocation = "$coords ($loc)",
            propertyId = property.id,
            propertyTitle = property.title,
            propertyLocality = "${property.locality}, Vrindavan",
            listingType = ListingType.SHORT_STAY,
            propertyPrice = totalPayable,
            leadType = "Short Stay Booked",
            status = "New Lead - Confirm Host Access",
            adminNotes = "Guest booked $nightsCount nights. Coordinate ₹200 landlord fee & check-in."
        )
        _userInterestLeads.update { listOf(lead) + it }
    }

    // Managed Agreements & Police Verification
    fun requestRentAgreementAndVerification(property: PropertyListing, tenantName: String) {
        val nowStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }
        val targetDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)

        val user = _currentUser.value
        val coords = user?.lastKnownCoordinates ?: "27.5794° N, 77.6881° E"
        val loc = user?.lastKnownLocality ?: "Vrindavan"

        val newAgreement = RentalAgreementTracker(
            id = "AGR-${(1000..9999).random()}",
            propertyId = property.id,
            propertyTitle = property.title,
            locality = "${property.locality}, ${property.city}",
            monthlyRent = property.price,
            brokerage15DaysFee = property.price / 2, // 15 Days Rent (Transparent Brokerage)
            tenantName = tenantName,
            landlordName = property.ownerName,
            agreementStatus = AgreementStepStatus.DRAFTING,
            policeVerificationStatus = PoliceVerificationStatus.DOCUMENTS_COLLECTED,
            agreementNumber = "BV-AGR-2026-${(100..999).random()}",
            policeAckNumber = "UPCOP-VRN-2026-${(10000..99999).random()}",
            initiatedDate = nowStr,
            targetPossessionDate = targetDate,
            isBrokeragePaidByTenant = false,
            isBrokeragePaidByLandlord = false
        )

        _agreements.update { listOf(newAgreement) + it }

        // Log location of user taking on rent
        logUserLocation(
            userId = user?.id ?: "tenant",
            userName = tenantName,
            userPhone = user?.phone ?: "+91 98765 43210",
            actionType = if (property.listingType == ListingType.SELL) "BUY_INQUIRY" else "RENT_INQUIRY",
            actionDescription = "Initiated Agreement for ${property.title} (Rent: ₹${property.price})",
            propertyId = property.id,
            propertyTitle = property.title,
            coordinates = coords,
            geoLocality = loc
        )

        // Lead for Admin
        val lead = UserInterestLead(
            id = "LEAD-${(1000..9999).random()}",
            userId = user?.id ?: "tenant",
            userName = tenantName,
            userPhone = user?.phone ?: "+91 98765 43210",
            userEmail = user?.email ?: "user@brajvaas.in",
            userExactLocation = "$coords ($loc)",
            propertyId = property.id,
            propertyTitle = property.title,
            propertyLocality = "${property.locality}, ${property.city}",
            listingType = property.listingType,
            propertyPrice = property.price,
            leadType = "Rent Agreement Inquired",
            status = "New Lead - Contact for E-Stamp",
            adminNotes = "Devotee requested 15-day agreement & police verification. Immediate contact advised."
        )
        _userInterestLeads.update { listOf(lead) + it }
    }

    // Post Property with Location Logging
    fun postProperty(newListing: PropertyListing) {
        val user = _currentUser.value
        val coords = newListing.loggedCoordinates.ifBlank { user?.lastKnownCoordinates ?: "27.5794° N, 77.6881° E" }
        val loc = newListing.loggedLocality.ifBlank { "${newListing.locality}, ${newListing.city}" }

        _properties.update { current ->
            listOf(newListing.copy(
                approvalStatus = ApprovalStatus.PENDING_APPROVAL,
                loggedCoordinates = coords,
                loggedLocality = loc
            )) + current
        }

        // Log exact location of user posting property
        logUserLocation(
            userId = user?.id ?: "owner",
            userName = newListing.ownerName,
            userPhone = newListing.ownerPhone,
            actionType = "POSTED_PROPERTY",
            actionDescription = "Posted ${newListing.bhk} ${newListing.title} (${newListing.listingType.label})",
            propertyId = newListing.id,
            propertyTitle = newListing.title,
            coordinates = coords,
            geoLocality = loc
        )
    }

    private fun logUserLocation(
        userId: String,
        userName: String,
        userPhone: String,
        actionType: String,
        actionDescription: String,
        propertyId: String?,
        propertyTitle: String?,
        coordinates: String,
        geoLocality: String
    ) {
        val log = UserLocationAuditLog(
            id = "LOC-${(10000..99999).random()}",
            userId = userId,
            userName = userName,
            userPhone = userPhone,
            actionType = actionType,
            actionDescription = actionDescription,
            propertyId = propertyId,
            propertyTitle = propertyTitle,
            exactCoordinates = coordinates,
            geoLocality = geoLocality,
            timestamp = System.currentTimeMillis()
        )
        _locationAuditLogs.update { listOf(log) + it }
    }

    // Admin Lead Updates
    fun updateLeadStatus(leadId: String, newStatus: String, notes: String = "") {
        _userInterestLeads.update { list ->
            list.map {
                if (it.id == leadId) {
                    it.copy(
                        status = newStatus,
                        adminNotes = if (notes.isNotBlank()) notes else it.adminNotes
                    )
                } else it
            }
        }
    }

    fun deleteUser(userId: String) {
        _usersList.update { it.filter { user -> user.id != userId } }
    }

    fun toggleUserKyc(userId: String) {
        _usersList.update { list ->
            list.map { if (it.id == userId) it.copy(isKycVerified = !it.isKycVerified) else it }
        }
    }

    fun deleteProperty(propertyId: String) {
        _properties.update { it.filter { p -> p.id != propertyId } }
    }

    fun advanceAgreementStep(agreementId: String) {
        _agreements.update { list ->
            list.map { agr ->
                if (agr.id == agreementId) {
                    val nextStatus = when (agr.agreementStatus) {
                        AgreementStepStatus.DRAFTING -> AgreementStepStatus.E_STAMPED
                        AgreementStepStatus.E_STAMPED -> AgreementStepStatus.SIGNED_BY_BOTH
                        AgreementStepStatus.SIGNED_BY_BOTH -> AgreementStepStatus.COMPLETED
                        AgreementStepStatus.COMPLETED -> AgreementStepStatus.COMPLETED
                    }
                    agr.copy(agreementStatus = nextStatus)
                } else agr
            }
        }
    }

    fun advancePoliceVerification(agreementId: String) {
        _agreements.update { list ->
            list.map { agr ->
                if (agr.id == agreementId) {
                    val nextPv = when (agr.policeVerificationStatus) {
                        PoliceVerificationStatus.DOCUMENTS_COLLECTED -> PoliceVerificationStatus.BRAJVAAS_INSPECTION
                        PoliceVerificationStatus.BRAJVAAS_INSPECTION -> PoliceVerificationStatus.UPCOP_POLICE_SUBMITTED
                        PoliceVerificationStatus.UPCOP_POLICE_SUBMITTED -> PoliceVerificationStatus.VERIFIED_CLEARED
                        PoliceVerificationStatus.VERIFIED_CLEARED -> PoliceVerificationStatus.VERIFIED_CLEARED
                    }
                    agr.copy(policeVerificationStatus = nextPv)
                } else agr
            }
        }
    }

    fun adminApprove(propertyId: String, verifyBadge: Boolean) {
        _properties.update { list ->
            list.map {
                if (it.id == propertyId) {
                    it.copy(
                        approvalStatus = ApprovalStatus.APPROVED,
                        isVerified = if (verifyBadge) true else it.isVerified,
                        adminFeedback = "Approved by Brajvaas Moderation Cell. Listing is now live."
                    )
                } else it
            }
        }
    }

    fun adminReject(propertyId: String, feedback: String) {
        _properties.update { list ->
            list.map {
                if (it.id == propertyId) {
                    it.copy(
                        approvalStatus = ApprovalStatus.REJECTED,
                        adminFeedback = feedback
                    )
                } else it
            }
        }
    }

    fun adminToggleVerification(propertyId: String) {
        _properties.update { list ->
            list.map {
                if (it.id == propertyId) {
                    it.copy(isVerified = !it.isVerified)
                } else it
            }
        }
    }

    fun getOrCreateConversation(property: PropertyListing): String {
        val convId = "conv_${property.id}"
        val existing = _conversations.value.find { it.id == convId }
        if (existing == null) {
            val newConv = ConversationThread(
                id = convId,
                propertyId = property.id,
                propertyTitle = property.title,
                propertyLocality = "${property.locality}, ${property.city}",
                propertyPrice = property.price,
                listingType = property.listingType,
                recipientName = "Owner (${property.bhk})",
                lastMessage = "Brajvaas managed channel opened for this property.",
                lastTimestamp = System.currentTimeMillis(),
                unreadCount = 0
            )
            _conversations.update { listOf(newConv) + it }
            _messages.update { current ->
                val initMsg = listOf(
                    ChatMessage(
                        id = "msg_${System.currentTimeMillis()}",
                        conversationId = convId,
                        propertyId = property.id,
                        senderId = "system",
                        senderName = "Brajvaas Desk",
                        messageText = "Radhe Radhe! Direct communication connected. Brajvaas charges 15 days rent brokerage upon agreement. Full rental agreement and Mathura police verification will be executed by our team.",
                        timestamp = System.currentTimeMillis() - 5000,
                        isFromMe = false
                    )
                )
                current + (convId to initMsg)
            }
        }
        return convId
    }

    fun sendMessage(conversationId: String, propertyId: String, text: String) {
        val timestamp = System.currentTimeMillis()
        val userMsg = ChatMessage(
            id = "msg_$timestamp",
            conversationId = conversationId,
            propertyId = propertyId,
            senderId = "me",
            senderName = "Seeker (You)",
            messageText = text,
            timestamp = timestamp,
            isFromMe = true
        )

        _messages.update { current ->
            val list = current[conversationId] ?: emptyList()
            current + (conversationId to (list + userMsg))
        }

        _conversations.update { convs ->
            convs.map {
                if (it.id == conversationId) {
                    it.copy(lastMessage = text, lastTimestamp = timestamp)
                } else it
            }
        }

        // Auto-reply simulation from property owner
        val autoReply = when {
            text.contains("available", ignoreCase = true) ->
                "Radhe Radhe! Yes, the property is available for sacred Braj residency. When would you like to arrange a visit with Brajvaas executive?"
            text.contains("short stay", ignoreCase = true) || text.contains("night", ignoreCase = true) ->
                "Hare Krishna! Short stay bookings in Vrindavan are managed via Brajvaas with flat Rs 200 platform fee. You are most welcome."
            text.contains("visit", ignoreCase = true) || text.contains("darshan", ignoreCase = true) || text.contains("see", ignoreCase = true) ->
                "You are most welcome. Brajvaas Mitra will accompany you for darshan and property inspection between 10:00 AM and 5:30 PM."
            text.contains("agreement", ignoreCase = true) || text.contains("police", ignoreCase = true) || text.contains("brokerage", ignoreCase = true) ->
                "Yes, Brajvaas will draft the e-stamped 11-month agreement and complete Mathura police verification for 15 days brokerage."
            text.contains("backup", ignoreCase = true) || text.contains("power", ignoreCase = true) ->
                "Yes, we have 24/7 dedicated generator backup for AC, fans, and lift."
            else ->
                "Hare Krishna! Thank you for your inquiry on Brajvaas. I have received your message and will coordinate with Brajvaas desk shortly."
        }

        val replyMsg = ChatMessage(
            id = "msg_${timestamp + 1200}",
            conversationId = conversationId,
            propertyId = propertyId,
            senderId = "owner",
            senderName = "Property Owner",
            messageText = autoReply,
            timestamp = timestamp + 1200,
            isFromMe = false
        )

        _messages.update { current ->
            val list = current[conversationId] ?: emptyList()
            current + (conversationId to (list + replyMsg))
        }

        _conversations.update { convs ->
            convs.map {
                if (it.id == conversationId) {
                    it.copy(lastMessage = autoReply, lastTimestamp = timestamp + 1200)
                } else it
            }
        }
    }

    fun payRent(paymentId: String, paymentMode: String = "UPI / Brajvaas Pay") {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        val paidDateStr = dateFormat.format(Date())
        val txnId = "BV-TXN-" + (100000..999999).random()

        _rentPayments.update { list ->
            list.map {
                if (it.id == paymentId) {
                    it.copy(
                        status = PaymentStatus.PAID,
                        paidDate = paidDateStr,
                        transactionId = txnId,
                        paymentMode = paymentMode
                    )
                } else it
            }
        }
    }

    fun createSupportTicket(category: String, subject: String, description: String) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val newTicket = SupportTicket(
            id = "TKT-${(1000..9999).random()}",
            category = category,
            subject = subject,
            description = description,
            status = "In Review",
            createdAtFormatted = dateFormat.format(Date())
        )
        _supportTickets.update { listOf(newTicket) + it }
    }

    // INITIAL SEED DATA
    private fun getInitialUsers(): List<UserProfile> {
        return listOf(
            UserProfile(
                id = "user_mukesh_01",
                fullName = "Mukesh Garg",
                phone = "+91 98765 43210",
                email = "mukgarg11@gmail.com",
                role = UserRole.SEEKER,
                city = "Vrindavan",
                isKycVerified = true,
                joinedDate = "10 Oct 2026",
                lastKnownCoordinates = "27.5794° N, 77.6881° E",
                lastKnownLocality = "Raman Reti, Vrindavan"
            ),
            UserProfile(
                id = "user_govind_02",
                fullName = "Govind Das Sharma",
                phone = "+91 98223 11223",
                email = "govind.sharma@vrindavan.org",
                role = UserRole.LANDLORD,
                city = "Vrindavan",
                isKycVerified = true,
                joinedDate = "05 Oct 2026",
                lastKnownCoordinates = "27.5812° N, 77.6795° E",
                lastKnownLocality = "Chhatikara Road, Vrindavan"
            ),
            UserProfile(
                id = "user_radheshyam_03",
                fullName = "Radhe Shyam Agarwal",
                phone = "+91 98111 88776",
                email = "radheshyam.mathura@gmail.com",
                role = UserRole.LANDLORD,
                city = "Mathura",
                isKycVerified = true,
                joinedDate = "08 Oct 2026",
                lastKnownCoordinates = "27.4924° N, 77.6737° E",
                lastKnownLocality = "Krishna Nagar, Mathura"
            ),
            UserProfile(
                id = "user_ananya_04",
                fullName = "Ananya Devi (Pilgrim)",
                phone = "+91 94120 55443",
                email = "ananya.pilgrim@outlook.com",
                role = UserRole.SEEKER,
                city = "Vrindavan",
                isKycVerified = false,
                joinedDate = "12 Oct 2026",
                lastKnownCoordinates = "27.5847° N, 77.6978° E",
                lastKnownLocality = "Bankey Bihari Parikrama Marg, Vrindavan"
            )
        )
    }

    private fun getInitialShortStayBookings(): List<ShortStayBooking> {
        return listOf(
            ShortStayBooking(
                id = "VRN-STAY-1092",
                propertyId = "prop_short_1",
                propertyTitle = "Divine 1 BHK Ashram Homestay (Near Prem Mandir)",
                locality = "Chhatikara Road, Vrindavan",
                guestName = "Ananya Devi",
                guestPhone = "+91 94120 55443",
                hostName = "Govind Das Sharma",
                hostPhone = "+91 98223 11223",
                checkInDate = "15 Oct 2026",
                checkOutDate = "18 Oct 2026",
                numberOfGuests = 3,
                numberOfNights = 3,
                ratePerNight = 1400L,
                staySubtotal = 4200L,
                tenantFee = 200L, // Rs 200 flat
                landlordFee = 200L, // Rs 200 flat
                totalGuestPayable = 4400L,
                guestExactCoordinates = "27.5812° N, 77.6795° E (Prem Mandir VIP Cut, Vrindavan)",
                status = "Confirmed"
            ),
            ShortStayBooking(
                id = "VRN-STAY-1088",
                propertyId = "prop_short_2",
                propertyTitle = "Peacock Grove Spiritual Guest Suite",
                locality = "Raman Reti, Vrindavan",
                guestName = "Mukesh Garg",
                guestPhone = "+91 98765 43210",
                hostName = "Braj Bhumi Trust",
                hostPhone = "+91 98765 43210",
                checkInDate = "08 Oct 2026",
                checkOutDate = "10 Oct 2026",
                numberOfGuests = 2,
                numberOfNights = 2,
                ratePerNight = 1800L,
                staySubtotal = 3600L,
                tenantFee = 200L,
                landlordFee = 200L,
                totalGuestPayable = 3800L,
                guestExactCoordinates = "27.5794° N, 77.6881° E (Near ISKCON, Vrindavan)",
                status = "Completed"
            )
        )
    }

    private fun getInitialInterestLeads(): List<UserInterestLead> {
        return listOf(
            UserInterestLead(
                id = "LEAD-4819",
                userId = "user_mukesh_01",
                userName = "Mukesh Garg",
                userPhone = "+91 98765 43210",
                userEmail = "mukgarg11@gmail.com",
                userExactLocation = "27.5794° N, 77.6881° E (Raman Reti, Vrindavan)",
                propertyId = "prop_1",
                propertyTitle = "2 BHK Spiritual Ashram Flat with Balcony & Mandir",
                propertyLocality = "Raman Reti, Vrindavan",
                listingType = ListingType.RENT,
                propertyPrice = 16500L,
                leadType = "Liked / Shortlisted",
                status = "New Lead - Pending Contact",
                adminNotes = "User shortlisted this property twice. Call regarding weekend darshan visit."
            ),
            UserInterestLead(
                id = "LEAD-4820",
                userId = "user_ananya_04",
                userName = "Ananya Devi (Pilgrim)",
                userPhone = "+91 94120 55443",
                userEmail = "ananya.pilgrim@outlook.com",
                userExactLocation = "27.5847° N, 77.6978° E (Bankey Bihari Marg, Vrindavan)",
                propertyId = "prop_short_1",
                propertyTitle = "Divine 1 BHK Ashram Homestay (Near Prem Mandir)",
                propertyLocality = "Chhatikara Road, Vrindavan",
                listingType = ListingType.SHORT_STAY,
                propertyPrice = 1400L,
                leadType = "Short Stay Booked",
                status = "Contacted by Admin",
                adminNotes = "Confirmed 3-night stay. Informed about Rs 200 platform fee and key pickup."
            ),
            UserInterestLead(
                id = "LEAD-4821",
                userId = "user_mukesh_01",
                userName = "Mukesh Garg",
                userPhone = "+91 98765 43210",
                userEmail = "mukgarg11@gmail.com",
                userExactLocation = "27.5794° N, 77.6881° E (Raman Reti, Vrindavan)",
                propertyId = "prop_3",
                propertyTitle = "3 BHK Luxury Kothi with Private Garden & Satsang Courtyard",
                propertyLocality = "Rukmini Vihar, Vrindavan",
                listingType = ListingType.SELL,
                propertyPrice = 14500000L,
                leadType = "Liked / Shortlisted",
                status = "Darshan Visit Scheduled",
                adminNotes = "High-value sale buyer. Accompanied visit scheduled for Saturday 4 PM."
            )
        )
    }

    private fun getInitialLocationLogs(): List<UserLocationAuditLog> {
        val now = System.currentTimeMillis()
        return listOf(
            UserLocationAuditLog(
                id = "LOC-98101",
                userId = "user_govind_02",
                userName = "Govind Das Sharma",
                userPhone = "+91 98223 11223",
                actionType = "POSTED_PROPERTY",
                actionDescription = "Posted 2 BHK Ashram Flat in Raman Reti for Rent (₹16,500/mo)",
                propertyId = "prop_1",
                propertyTitle = "2 BHK Spiritual Ashram Flat with Balcony & Mandir",
                exactCoordinates = "27.5794° N, 77.6881° E",
                geoLocality = "Raman Reti, Vrindavan, Mathura District",
                timestamp = now - 36000000
            ),
            UserLocationAuditLog(
                id = "LOC-98102",
                userId = "user_mukesh_01",
                userName = "Mukesh Garg",
                userPhone = "+91 98765 43210",
                actionType = "PROPERTY_LIKED",
                actionDescription = "Shortlisted 2 BHK Krishna Krupa Flat near ISKCON",
                propertyId = "prop_1",
                propertyTitle = "2 BHK Spiritual Ashram Flat with Balcony & Mandir",
                exactCoordinates = "27.5794° N, 77.6881° E",
                geoLocality = "Near Bhaktivedanta Hospice, Raman Reti, Vrindavan",
                timestamp = now - 18000000
            ),
            UserLocationAuditLog(
                id = "LOC-98103",
                userId = "user_ananya_04",
                userName = "Ananya Devi",
                userPhone = "+91 94120 55443",
                actionType = "SHORT_STAY_BOOKING",
                actionDescription = "Booked 3 Nights Vrindavan Homestay near Prem Mandir",
                propertyId = "prop_short_1",
                propertyTitle = "Divine 1 BHK Ashram Homestay (Near Prem Mandir)",
                exactCoordinates = "27.5812° N, 77.6795° E",
                geoLocality = "Chhatikara Road, Vrindavan, Mathura District",
                timestamp = now - 7200000
            ),
            UserLocationAuditLog(
                id = "LOC-98104",
                userId = "user_mukesh_01",
                userName = "Mukesh Garg",
                userPhone = "+91 98765 43210",
                actionType = "RENT_INQUIRY",
                actionDescription = "Requested 15-Day Agreement & Mathura Police Verification",
                propertyId = "prop_1",
                propertyTitle = "2 BHK Krishna Krupa Flat (A-304)",
                exactCoordinates = "27.5794° N, 77.6881° E",
                geoLocality = "Raman Reti, Vrindavan, Mathura District",
                timestamp = now - 3600000
            ),
            UserLocationAuditLog(
                id = "LOC-98105",
                userId = "user_mukesh_01",
                userName = "Mukesh Garg",
                userPhone = "+91 98765 43210",
                actionType = "BUY_INQUIRY",
                actionDescription = "Shortlisted 3 BHK Luxury Kothi in Rukmini Vihar (₹1.45 Cr)",
                propertyId = "prop_3",
                propertyTitle = "3 BHK Luxury Kothi with Private Garden",
                exactCoordinates = "27.5610° N, 77.6520° E",
                geoLocality = "Rukmini Vihar MVDA Colony, Vrindavan",
                timestamp = now - 1200000
            )
        )
    }

    private fun getInitialAgreements(): List<RentalAgreementTracker> {
        return listOf(
            RentalAgreementTracker(
                id = "AGR-1024",
                propertyId = "prop_1",
                propertyTitle = "2 BHK Krishna Krupa Flat (A-304)",
                locality = "Raman Reti, Vrindavan",
                monthlyRent = 16500,
                brokerage15DaysFee = 8250, // 15 Days Rent
                tenantName = "Mukesh Garg",
                landlordName = "Govind Das Sharma",
                agreementStatus = AgreementStepStatus.E_STAMPED,
                policeVerificationStatus = PoliceVerificationStatus.UPCOP_POLICE_SUBMITTED,
                agreementNumber = "BV-AGR-2026-8812",
                policeAckNumber = "UPCOP-VRN-2026-94812",
                initiatedDate = "05 Oct 2026",
                targetPossessionDate = "15 Oct 2026",
                isBrokeragePaidByTenant = true,
                isBrokeragePaidByLandlord = false
            ),
            RentalAgreementTracker(
                id = "AGR-1025",
                propertyId = "prop_2",
                propertyTitle = "1 RK Divine Studio Suite",
                locality = "Ganpati Infinity, Chhatikara Rd",
                monthlyRent = 8500,
                brokerage15DaysFee = 4250, // 15 Days Rent
                tenantName = "Mukesh Garg",
                landlordName = "Radhe Shyam Agarwal",
                agreementStatus = AgreementStepStatus.COMPLETED,
                policeVerificationStatus = PoliceVerificationStatus.VERIFIED_CLEARED,
                agreementNumber = "BV-AGR-2026-7201",
                policeAckNumber = "UPCOP-VRN-2026-82190",
                initiatedDate = "20 Sep 2026",
                targetPossessionDate = "01 Oct 2026",
                isBrokeragePaidByTenant = true,
                isBrokeragePaidByLandlord = true
            )
        )
    }

    private fun getInitialProperties(): List<PropertyListing> {
        return listOf(
            PropertyListing(
                id = "prop_1",
                title = "2 BHK Spiritual Ashram Flat with Balcony & Mandir",
                listingType = ListingType.RENT,
                propertyType = PropertyType.APARTMENT,
                locality = "Raman Reti",
                city = "Vrindavan",
                fullAddress = "A-304, Krishna Krupa Society, Near ISKCON, Raman Reti, Vrindavan",
                landmark = "500m from ISKCON Vrindavan, behind Bhaktivedanta Hospice",
                price = 16500,
                deposit = 33000,
                maintenanceCharges = 1500,
                bhk = "2 BHK",
                builtUpAreaSqFt = 1100,
                carpetAreaSqFt = 920,
                floor = 3,
                totalFloors = 5,
                furnishing = FurnishingStatus.FURNISHED,
                facing = "North-East (Temple Facing)",
                availableFrom = "Immediately",
                preferredTenants = "Devotee Families / ISKCON Pilgrims",
                isPureSattvik = true,
                amenities = listOf("24/7 Power Backup", "Sweet RO Water", "Society Mandir", "Covered Car Parking", "Elevator (Lift)", "24/7 Guard & CCTV", "Satsang Hall", "Air Conditioning"),
                templeDistances = TempleDistances(premMandirKm = 1.2, bankeyBihariKm = 2.4, iskconKm = 0.5, janmabhoomiKm = 11.5, yamunaGhatKm = 0.9),
                legalStatus = "MVDA Approved & Regd. Society",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_ashram_villa,
                loggedCoordinates = "27.5794° N, 77.6881° E",
                loggedLocality = "Raman Reti, Vrindavan",
                ownerName = "Govind Das Sharma",
                ownerPhone = "+91 98223 11223"
            ),
            // VRINDAVAN SHORT STAYS (Airbnb style - exclusive for Vrindavan)
            PropertyListing(
                id = "prop_short_1",
                title = "Divine 1 BHK Ashram Homestay (Near Prem Mandir)",
                listingType = ListingType.SHORT_STAY,
                propertyType = PropertyType.VRINDAVAN_HOMESTAY,
                locality = "Chhatikara Road",
                city = "Vrindavan",
                fullAddress = "Suite 204, Radha Kripa Villa, Chhatikara Rd, Vrindavan",
                landmark = "400m from Prem Mandir Gate 2",
                price = 1400, // Rs 1400 / night
                pricePerNight = 1400,
                deposit = 0,
                maintenanceCharges = 0,
                bhk = "1 BHK Suite",
                builtUpAreaSqFt = 550,
                carpetAreaSqFt = 480,
                floor = 2,
                totalFloors = 3,
                furnishing = FurnishingStatus.FURNISHED,
                facing = "Prem Mandir View",
                availableFrom = "Immediate Check-in",
                preferredTenants = "Pilgrims / Devotee Couples & Families",
                isPureSattvik = true,
                amenities = listOf("Air Conditioned", "Geyser 24/7 Hot Water", "Sattvik Kitchenette", "Elevator", "Power Backup", "Wi-Fi", "Prem Mandir Darshan Balcony"),
                templeDistances = TempleDistances(premMandirKm = 0.4, bankeyBihariKm = 3.6, iskconKm = 2.0, janmabhoomiKm = 10.5, yamunaGhatKm = 3.0),
                legalStatus = "Homestay Registered",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_brajvaas_hero,
                loggedCoordinates = "27.5812° N, 77.6795° E",
                loggedLocality = "Chhatikara Road, Vrindavan",
                ownerName = "Govind Das Sharma",
                ownerPhone = "+91 98223 11223"
            ),
            PropertyListing(
                id = "prop_short_2",
                title = "Peacock Grove Spiritual Guest Suite",
                listingType = ListingType.SHORT_STAY,
                propertyType = PropertyType.VRINDAVAN_HOMESTAY,
                locality = "Raman Reti",
                city = "Vrindavan",
                fullAddress = "B-12, Krishna Garden Enclave, Raman Reti, Vrindavan",
                landmark = "Walking distance to ISKCON Vrindavan temple",
                price = 1800, // Rs 1800 / night
                pricePerNight = 1800,
                deposit = 0,
                maintenanceCharges = 0,
                bhk = "Luxury Studio",
                builtUpAreaSqFt = 620,
                carpetAreaSqFt = 540,
                floor = 1,
                totalFloors = 2,
                furnishing = FurnishingStatus.FURNISHED,
                facing = "Garden & Temple Facing",
                availableFrom = "Immediate Check-in",
                preferredTenants = "Devotee Families / Solo Pilgrims",
                isPureSattvik = true,
                amenities = listOf("King Bed + Devotee Bed", "Pure Sattvik RO Water", "24/7 Inverter Power Backup", "Dedicated Car Parking", "Lush Tulsi Garden"),
                templeDistances = TempleDistances(premMandirKm = 1.0, bankeyBihariKm = 2.2, iskconKm = 0.4, janmabhoomiKm = 11.0, yamunaGhatKm = 0.8),
                legalStatus = "Brajvaas Certified Homestay",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_ashram_villa,
                loggedCoordinates = "27.5794° N, 77.6881° E",
                loggedLocality = "Raman Reti, Vrindavan",
                ownerName = "Braj Bhumi Trust",
                ownerPhone = "+91 98765 43210"
            ),
            PropertyListing(
                id = "prop_short_3",
                title = "Yamuna Ghat View Holy Haven Suite",
                listingType = ListingType.SHORT_STAY,
                propertyType = PropertyType.VRINDAVAN_HOMESTAY,
                locality = "Bankey Bihari Zone",
                city = "Vrindavan",
                fullAddress = "Top Floor, Keshi Ghat Marg, Parikrama Path, Vrindavan",
                landmark = "100m from sacred Keshi Ghat & Yamuna Aarti",
                price = 1100, // Rs 1100 / night
                pricePerNight = 1100,
                deposit = 0,
                maintenanceCharges = 0,
                bhk = "1 RK Ashram Suite",
                builtUpAreaSqFt = 400,
                carpetAreaSqFt = 360,
                floor = 3,
                totalFloors = 3,
                furnishing = FurnishingStatus.FURNISHED,
                facing = "Direct Yamuna River Facing",
                availableFrom = "Immediate Check-in",
                preferredTenants = "Parikrama Pilgrims",
                isPureSattvik = true,
                amenities = listOf("Rooftop Aarti View", "24/7 Sweet Water", "Geyser Hot Water", "Inverter Backup", "Peaceful Atmosphere"),
                templeDistances = TempleDistances(premMandirKm = 3.2, bankeyBihariKm = 0.6, iskconKm = 2.5, janmabhoomiKm = 12.0, yamunaGhatKm = 0.1),
                legalStatus = "Parikrama Marg Homestay",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_brajvaas_hero,
                loggedCoordinates = "27.5855° N, 77.7012° E",
                loggedLocality = "Keshi Ghat, Vrindavan",
                ownerName = "Madhav Das",
                ownerPhone = "+91 98111 22334"
            ),
            PropertyListing(
                id = "prop_2",
                title = "1 RK Divine Studio Suite near Prem Mandir",
                listingType = ListingType.RENT,
                propertyType = PropertyType.ASHRAM_STUDIO,
                locality = "Chhatikara Road",
                city = "Vrindavan",
                fullAddress = "Tower 2, Flat 104, Ganpati Infinity, Chhatikara Rd, Vrindavan",
                landmark = "Opposite Prem Mandir Gate 3",
                price = 8500,
                deposit = 17000,
                maintenanceCharges = 800,
                bhk = "1 RK",
                builtUpAreaSqFt = 450,
                carpetAreaSqFt = 380,
                floor = 1,
                totalFloors = 8,
                furnishing = FurnishingStatus.FURNISHED,
                facing = "East Facing",
                availableFrom = "1st of Next Month",
                preferredTenants = "Spiritual Seekers / Working Single",
                isPureSattvik = true,
                amenities = listOf("24/7 Power Backup", "Sweet Drinking Water", "Gated Security", "Elevator (Lift)", "Society Canteen (Sattvik Food)", "High Speed Wi-Fi"),
                templeDistances = TempleDistances(premMandirKm = 0.4, bankeyBihariKm = 3.8, iskconKm = 2.1, janmabhoomiKm = 10.0, yamunaGhatKm = 3.2),
                legalStatus = "MVDA RERA Registered",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_brajvaas_hero,
                loggedCoordinates = "27.5812° N, 77.6795° E",
                loggedLocality = "Chhatikara Road, Vrindavan",
                ownerName = "Radhe Shyam Agarwal",
                ownerPhone = "+91 98111 88776"
            ),
            PropertyListing(
                id = "prop_3",
                title = "3 BHK Luxury Kothi with Private Garden & Satsang Courtyard",
                listingType = ListingType.SELL,
                propertyType = PropertyType.INDEPENDENT_VILLA,
                locality = "Rukmini Vihar",
                city = "Vrindavan",
                fullAddress = "Plot 78, Phase 2, MVDA Colony, Rukmini Vihar, Vrindavan",
                landmark = "Near VIP Road & NH-19 Vrindavan Exit",
                price = 14500000,
                deposit = 0,
                maintenanceCharges = 2000,
                bhk = "3 BHK",
                builtUpAreaSqFt = 2150,
                carpetAreaSqFt = 1850,
                floor = 1,
                totalFloors = 2,
                furnishing = FurnishingStatus.SEMI_FURNISHED,
                facing = "North-East Vaastu Compliant",
                availableFrom = "Ready to Move",
                preferredTenants = "Ownership Sale",
                isPureSattvik = true,
                amenities = listOf("Private Garden", "Dedicated Satsang Room", "Borewell & Sweet Municipal Water", "Covered Car Parking (2 Cars)", "Solar Panels", "Gated Township", "CCTV"),
                templeDistances = TempleDistances(premMandirKm = 2.8, bankeyBihariKm = 4.5, iskconKm = 3.6, janmabhoomiKm = 8.5, yamunaGhatKm = 4.0),
                legalStatus = "MVDA Freehold Registry with Mutation Done",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_ashram_villa,
                loggedCoordinates = "27.5610° N, 77.6520° E",
                loggedLocality = "Rukmini Vihar, Vrindavan",
                ownerName = "Govind Das Sharma",
                ownerPhone = "+91 98223 11223"
            ),
            PropertyListing(
                id = "prop_4",
                title = "2 BHK Devotee Residence near Bankey Bihari Parikrama Marg",
                listingType = ListingType.RENT,
                propertyType = PropertyType.BUILDER_FLOOR,
                locality = "Bankey Bihari Zone",
                city = "Vrindavan",
                fullAddress = "2nd Floor, Bihari Kunj, Parikrama Marg, Vrindavan",
                landmark = "400m walking from Bankey Bihari Mandir VIP Gate",
                price = 13500,
                deposit = 27000,
                maintenanceCharges = 600,
                bhk = "2 BHK",
                builtUpAreaSqFt = 850,
                carpetAreaSqFt = 720,
                floor = 2,
                totalFloors = 3,
                furnishing = FurnishingStatus.SEMI_FURNISHED,
                facing = "Temple Facing",
                availableFrom = "Immediately",
                preferredTenants = "Strict Vegetarian & No Smoking",
                isPureSattvik = true,
                amenities = listOf("Yamuna View Rooftop", "Sweet Water Connection", "Inverter Power Backup", "Geyser in all baths", "Quiet Parikrama Environment"),
                templeDistances = TempleDistances(premMandirKm = 3.5, bankeyBihariKm = 0.4, iskconKm = 2.8, janmabhoomiKm = 12.0, yamunaGhatKm = 0.3),
                legalStatus = "Old Vrindavan Municipal Record Clear",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = false,
                imageDrawableRes = R.drawable.img_brajvaas_hero,
                loggedCoordinates = "27.5847° N, 77.6978° E",
                loggedLocality = "Parikrama Marg, Vrindavan",
                ownerName = "Kishori Das",
                ownerPhone = "+91 98450 12345"
            ),
            PropertyListing(
                id = "prop_5",
                title = "3 BHK Premium Flat in Krishna Nagar, Mathura City",
                listingType = ListingType.RENT,
                propertyType = PropertyType.APARTMENT,
                locality = "Krishna Nagar",
                city = "Mathura",
                fullAddress = "B-501, Braj Dham Heights, Krishna Nagar Main Road, Mathura",
                landmark = "Near LIC Building, 2km from Shri Krishna Janmabhoomi",
                price = 22000,
                deposit = 44000,
                maintenanceCharges = 2200,
                bhk = "3 BHK",
                builtUpAreaSqFt = 1480,
                carpetAreaSqFt = 1260,
                floor = 5,
                totalFloors = 7,
                furnishing = FurnishingStatus.SEMI_FURNISHED,
                facing = "North Facing",
                availableFrom = "15th of this month",
                preferredTenants = "Salaried Families / Bankers",
                isPureSattvik = false,
                amenities = listOf("24/7 Power Backup", "Dedicated Car Parking", "High Speed Lift", "Gym & Club House", "Fire Safety", "Commercial Hub Nearby"),
                templeDistances = TempleDistances(premMandirKm = 8.5, bankeyBihariKm = 10.2, iskconKm = 9.0, janmabhoomiKm = 1.8, yamunaGhatKm = 3.5),
                legalStatus = "MVDA Approved & Bank Loan Approved",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_ashram_villa,
                loggedCoordinates = "27.4924° N, 77.6737° E",
                loggedLocality = "Krishna Nagar, Mathura",
                ownerName = "Radhe Shyam Agarwal",
                ownerPhone = "+91 98111 88776"
            ),
            PropertyListing(
                id = "prop_6",
                title = "Prime MVDA Approved Plot (160 Sq. Yds) on NH-19 Crossing",
                listingType = ListingType.SELL,
                propertyType = PropertyType.PLOT_LAND,
                locality = "NH-19 Mathura-Vrindavan",
                city = "Mathura",
                fullAddress = "Sector 4, Highway City, Vrindavan Cut, NH-19, Mathura",
                landmark = "Near GLA University & Vrindavan Flyover",
                price = 4800000,
                deposit = 0,
                maintenanceCharges = 0,
                bhk = "Plot",
                builtUpAreaSqFt = 1440,
                carpetAreaSqFt = 1440,
                floor = 0,
                totalFloors = 0,
                furnishing = FurnishingStatus.UNFURNISHED,
                facing = "East Facing 40ft Wide Road",
                availableFrom = "Immediate Registry",
                preferredTenants = "Residential Construction",
                isPureSattvik = true,
                amenities = listOf("Gated Gated Boundary", "Street Lights & Sewer Line", "Sweet Water Pipe", "Wide 40ft Road", "Park Facing"),
                templeDistances = TempleDistances(premMandirKm = 5.2, bankeyBihariKm = 7.0, iskconKm = 5.8, janmabhoomiKm = 6.5, yamunaGhatKm = 6.0),
                legalStatus = "143 Land Converted, MVDA Approved",
                approvalStatus = ApprovalStatus.APPROVED,
                isVerified = true,
                imageDrawableRes = R.drawable.img_brajvaas_hero,
                loggedCoordinates = "27.5250° N, 77.6600° E",
                loggedLocality = "NH-19 Mathura-Vrindavan",
                ownerName = "Govind Das Sharma",
                ownerPhone = "+91 98223 11223"
            ),
            PropertyListing(
                id = "prop_pending_1",
                title = "1 BHK Pilgrim Abode on Sunrakh Road",
                listingType = ListingType.RENT,
                propertyType = PropertyType.APARTMENT,
                locality = "Sunrakh Road",
                city = "Vrindavan",
                fullAddress = "Plot 55, Sunrakh Road, Near Prem Mandir, Vrindavan",
                landmark = "Near Vrindavan Heritage Tower site",
                price = 11000,
                deposit = 22000,
                maintenanceCharges = 800,
                bhk = "1 BHK",
                builtUpAreaSqFt = 650,
                carpetAreaSqFt = 520,
                floor = 1,
                totalFloors = 3,
                furnishing = FurnishingStatus.SEMI_FURNISHED,
                facing = "East Facing",
                availableFrom = "Immediately",
                preferredTenants = "Devotee Pilgrims",
                isPureSattvik = true,
                amenities = listOf("Power Backup", "Sweet Water", "Balcony"),
                templeDistances = TempleDistances(premMandirKm = 1.1, bankeyBihariKm = 2.8, iskconKm = 1.5, janmabhoomiKm = 11.0, yamunaGhatKm = 1.2),
                legalStatus = "Submitted for Verification",
                approvalStatus = ApprovalStatus.PENDING_APPROVAL,
                isVerified = false,
                imageDrawableRes = R.drawable.img_ashram_villa,
                loggedCoordinates = "27.5750° N, 77.6710° E",
                loggedLocality = "Sunrakh Road, Vrindavan",
                ownerName = "Narayan Shastri",
                ownerPhone = "+91 98370 55443"
            )
        )
    }

    private fun getInitialConversations(): List<ConversationThread> {
        return listOf(
            ConversationThread(
                id = "conv_prop_1",
                propertyId = "prop_1",
                propertyTitle = "2 BHK Spiritual Ashram Flat with Balcony & Mandir",
                propertyLocality = "Raman Reti, Vrindavan",
                propertyPrice = 16500,
                listingType = ListingType.RENT,
                recipientName = "Raman Reti Landlord (2 BHK)",
                lastMessage = "Radhe Radhe! You are welcome to visit tomorrow at 4 PM for darshan and property walkthrough.",
                lastTimestamp = System.currentTimeMillis() - 3600000,
                unreadCount = 1
            ),
            ConversationThread(
                id = "conv_prop_short_1",
                propertyId = "prop_short_1",
                propertyTitle = "Divine 1 BHK Ashram Homestay (Near Prem Mandir)",
                propertyLocality = "Chhatikara Road, Vrindavan",
                propertyPrice = 1400,
                listingType = ListingType.SHORT_STAY,
                recipientName = "Prem Mandir Homestay Host",
                lastMessage = "Short stay confirmed! Key handover assisted by Brajvaas executive.",
                lastTimestamp = System.currentTimeMillis() - 7200000,
                unreadCount = 0
            )
        )
    }

    private fun getInitialMessages(): Map<String, List<ChatMessage>> {
        val now = System.currentTimeMillis()
        return mapOf(
            "conv_prop_1" to listOf(
                ChatMessage(
                    id = "m1",
                    conversationId = "conv_prop_1",
                    propertyId = "prop_1",
                    senderId = "system",
                    senderName = "Brajvaas Desk",
                    messageText = "Brajvaas Facilitation Connected: We charge 15 days rent as brokerage upon agreement. Complete rent agreement and Mathura police verification handled by Brajvaas.",
                    timestamp = now - 7200000,
                    isFromMe = false
                ),
                ChatMessage(
                    id = "m2",
                    conversationId = "conv_prop_1",
                    propertyId = "prop_1",
                    senderId = "me",
                    senderName = "Seeker (You)",
                    messageText = "Radhe Radhe ji. Is this 2 BHK flat near ISKCON available for possession this weekend?",
                    timestamp = now - 5400000,
                    isFromMe = true
                ),
                ChatMessage(
                    id = "m3",
                    conversationId = "conv_prop_1",
                    propertyId = "prop_1",
                    senderId = "owner",
                    senderName = "Property Owner",
                    messageText = "Radhe Radhe! You are welcome to visit tomorrow at 4 PM for darshan and property walkthrough with Brajvaas team.",
                    timestamp = now - 3600000,
                    isFromMe = false
                )
            ),
            "conv_prop_short_1" to listOf(
                ChatMessage(
                    id = "ms1",
                    conversationId = "conv_prop_short_1",
                    propertyId = "prop_short_1",
                    senderId = "system",
                    senderName = "Brajvaas Stays",
                    messageText = "Vrindavan Short Stay channel connected. Flat Rs 200 platform fee applies upon booking.",
                    timestamp = now - 8000000,
                    isFromMe = false
                ),
                ChatMessage(
                    id = "ms2",
                    conversationId = "conv_prop_short_1",
                    propertyId = "prop_short_1",
                    senderId = "owner",
                    senderName = "Host Govind Das",
                    messageText = "Welcome to sacred Vrindavan! Room is ready with hot water and Prem Mandir view.",
                    timestamp = now - 7200000,
                    isFromMe = false
                )
            )
        )
    }

    private fun getInitialRentPayments(): List<RentPaymentRecord> {
        return listOf(
            RentPaymentRecord(
                id = "rent_oct_2026",
                propertyTitle = "2 BHK Krishna Krupa Flat (A-304)",
                locality = "Raman Reti, Vrindavan",
                landlordName = "Govind Das Sharma",
                tenantName = "Mukesh Garg",
                monthYear = "October 2026",
                baseRent = 16500,
                maintenanceCharges = 1500,
                totalAmount = 18000,
                dueDate = "15 Oct 2026",
                status = PaymentStatus.PENDING,
                paidDate = null,
                transactionId = null,
                paymentMode = "Brajvaas Instant UPI",
                receiptNumber = "BV-REC-2026-1089"
            ),
            RentPaymentRecord(
                id = "rent_sep_2026",
                propertyTitle = "2 BHK Krishna Krupa Flat (A-304)",
                locality = "Raman Reti, Vrindavan",
                landlordName = "Govind Das Sharma",
                tenantName = "Mukesh Garg",
                monthYear = "September 2026",
                baseRent = 16500,
                maintenanceCharges = 1500,
                totalAmount = 18000,
                dueDate = "10 Sep 2026",
                status = PaymentStatus.PAID,
                paidDate = "08 Sep 2026, 11:24 AM",
                transactionId = "BV-UPI-984210982",
                paymentMode = "UPI (Google Pay / PhonePe)",
                receiptNumber = "BV-REC-2026-0942"
            )
        )
    }

    private fun getInitialSupportTickets(): List<SupportTicket> {
        return listOf(
            SupportTicket(
                id = "TKT-4891",
                category = "MVDA Legal Verification",
                subject = "Guidance on checking MVDA approval map for Raman Reti land",
                description = "Wanted to know if Brajvaas assistance can verify the development layout passed by Mathura-Vrindavan Development Authority.",
                status = "Resolved",
                createdAtFormatted = "02 Oct 2026"
            ),
            SupportTicket(
                id = "TKT-5021",
                category = "Rent Agreement & Verification",
                subject = "Mathura Police verification status for A-304 tenant",
                description = "Checking if the UP Police UPCOP submission has been verified by the Brajvaas field officer.",
                status = "In Review",
                createdAtFormatted = "10 Oct 2026"
            )
        )
    }
}
