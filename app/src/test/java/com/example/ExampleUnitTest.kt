package com.example

import com.example.data.model.*
import com.example.data.repository.BrajvaasRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPendingPropertiesHiddenFromSeekersUntilAdminApproval() {
        val repository = BrajvaasRepository()
        val allProps = repository.properties.value

        val pendingProps = allProps.filter { it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }
        val approvedProps = allProps.filter { it.approvalStatus == ApprovalStatus.APPROVED }

        assertTrue("Should have initial pending properties awaiting review", pendingProps.isNotEmpty())
        assertTrue("Should have initial approved properties visible to seekers", approvedProps.isNotEmpty())

        val targetPending = pendingProps.first()

        repository.adminApprove(targetPending.id, verifyBadge = true)

        val updatedProps = repository.properties.value
        val updatedTarget = updatedProps.find { it.id == targetPending.id }

        assertNotNull(updatedTarget)
        assertEquals(ApprovalStatus.APPROVED, updatedTarget?.approvalStatus)
        assertTrue(updatedTarget?.isVerified == true)
    }

    @Test
    fun testAuthAndUserRegistration() {
        val repository = BrajvaasRepository()

        // Test sign up
        repository.signUp(
            name = "Radhe Shyam",
            phone = "+91 99887 76655",
            email = "radhe@vrindavan.org",
            role = UserRole.SEEKER,
            city = "Vrindavan"
        )

        assertTrue(repository.isLoggedIn.value)
        val user = repository.currentUser.value
        assertNotNull(user)
        assertEquals("Radhe Shyam", user?.fullName)
        assertEquals(UserRole.SEEKER, user?.role)

        // Test logout
        repository.logout()
        assertFalse(repository.isLoggedIn.value)
        assertNull(repository.currentUser.value)

        // Test login
        repository.login("+91 98765 43210", UserRole.LANDLORD)
        assertTrue(repository.isLoggedIn.value)
        assertEquals(UserRole.LANDLORD, repository.currentUser.value?.role)
    }

    @Test
    fun testVisitedAndShortlistedTracking() {
        val repository = BrajvaasRepository()
        val targetId = "prop_1"

        // Toggle shortlist
        val initialShortlisted = repository.shortlistedPropertyIds.value.contains(targetId)
        repository.toggleShortlist(targetId)
        val updatedShortlist = repository.shortlistedPropertyIds.value.contains(targetId)
        assertNotEquals(initialShortlisted, updatedShortlist)

        // Record visited
        repository.recordPropertyVisit("prop_4")
        val visits = repository.visitedProperties.value
        assertTrue(visits.any { it.propertyId == "prop_4" })
    }

    @Test
    fun testFifteenDaysBrokerageAndAgreementCreation() {
        val repository = BrajvaasRepository()
        val prop = repository.properties.value.first { it.listingType == ListingType.RENT }

        repository.requestRentAgreementAndVerification(prop, "Mukesh Garg")

        val agreements = repository.agreements.value
        val created = agreements.find { it.propertyId == prop.id }

        assertNotNull("Agreement should be created", created)
        // 15 days rent brokerage = monthly rent / 2
        assertEquals(prop.price / 2, created?.brokerage15DaysFee)
        assertEquals(AgreementStepStatus.DRAFTING, created?.agreementStatus)
        assertEquals(PoliceVerificationStatus.DOCUMENTS_COLLECTED, created?.policeVerificationStatus)

        // Test step progression
        repository.advanceAgreementStep(created!!.id)
        repository.advancePoliceVerification(created.id)

        val updatedAgr = repository.agreements.value.find { it.id == created.id }
        assertEquals(AgreementStepStatus.E_STAMPED, updatedAgr?.agreementStatus)
        assertEquals(PoliceVerificationStatus.BRAJVAAS_INSPECTION, updatedAgr?.policeVerificationStatus)
    }

    @Test
    fun testRentPaymentWorkflow() {
        val repository = BrajvaasRepository()
        val payments = repository.rentPayments.value
        val pendingRent = payments.find { it.status == PaymentStatus.PENDING }

        assertNotNull("Should have a pending rent due", pendingRent)
        val paymentId = pendingRent!!.id

        repository.payRent(paymentId, "UPI / Brajvaas Pay")

        val updatedPayments = repository.rentPayments.value
        val paidRecord = updatedPayments.find { it.id == paymentId }

        assertNotNull(paidRecord)
        assertEquals(PaymentStatus.PAID, paidRecord?.status)
        assertNotNull(paidRecord?.transactionId)
        assertTrue(paidRecord?.transactionId!!.startsWith("BV-TXN-"))
        assertNotNull(paidRecord?.paidDate)
    }

    @Test
    fun testMessagingChannel() {
        val repository = BrajvaasRepository()
        val prop = repository.properties.value.first()

        val convId = repository.getOrCreateConversation(prop)
        assertNotNull(convId)

        repository.sendMessage(convId, prop.id, "Is this property available for rent?")

        val messages = repository.messages.value[convId]
        assertNotNull(messages)
        assertTrue(messages!!.size >= 2)
    }

    @Test
    fun testShortStayBookingWithFlatTwoHundredFee() {
        val repository = BrajvaasRepository()
        val shortStayProp = repository.properties.value.first { it.listingType == ListingType.SHORT_STAY }

        val initialBookingsCount = repository.shortStayBookings.value.size

        repository.bookShortStay(
            property = shortStayProp,
            guestName = "Mukesh Garg",
            guestPhone = "+91 98765 43210",
            checkInDate = "20 Oct 2026",
            checkOutDate = "23 Oct 2026",
            guestsCount = 2,
            nightsCount = 3
        )

        val updatedBookings = repository.shortStayBookings.value
        assertEquals(initialBookingsCount + 1, updatedBookings.size)

        val latest = updatedBookings.first()
        assertEquals(200L, latest.tenantFee)
        assertEquals(200L, latest.landlordFee)
        assertEquals(shortStayProp.price * 3, latest.staySubtotal)
        assertEquals((shortStayProp.price * 3) + 200L, latest.totalGuestPayable)
        assertTrue(latest.guestExactCoordinates.contains("27."))

        // Verify location audit log was appended
        val logs = repository.locationAuditLogs.value
        assertTrue(logs.any { it.actionType == "SHORT_STAY_BOOKING" })
    }

    @Test
    fun testUserInterestLeadsAndLocationAudit() {
        val repository = BrajvaasRepository()
        val initialLeadsCount = repository.userInterestLeads.value.size

        // Shortlisting should create an admin interest lead
        repository.toggleShortlist("prop_2")

        val leads = repository.userInterestLeads.value
        assertTrue("Leads should be recorded for admin contact", leads.size >= initialLeadsCount)

        val newLead = leads.first()
        assertNotNull(newLead.userPhone)
        assertNotNull(newLead.userExactLocation)

        // Test admin status update
        repository.updateLeadStatus(newLead.id, "Contacted - Discussion Ongoing", "Called Mukesh on phone")
        val updatedLead = repository.userInterestLeads.value.find { it.id == newLead.id }
        assertEquals("Contacted - Discussion Ongoing", updatedLead?.status)
        assertEquals("Called Mukesh on phone", updatedLead?.adminNotes)
    }

    @Test
    fun testAdminWebServerAndHtmlGeneration() {
        val repository = BrajvaasRepository()
        val server = com.example.data.server.AdminWebServer(repository)

        val html = server.generateWebAdminHtml()
        assertNotNull(html)
        assertTrue(html.contains("Brajvaas Web Admin Console"))
        assertTrue(html.contains("Vrindavan Short Stays"))
        assertTrue(html.contains("15 days rent"))
        assertTrue(html.contains("₹200 from tenant and ₹200 from landlord"))

        val json = server.generateJsonSnapshot()
        assertNotNull(json)
        assertTrue(json.contains("\"users\":["))
        assertTrue(json.contains("\"properties\":["))
        assertTrue(json.contains("\"shortStays\":["))
        assertTrue(json.contains("\"locationLogs\":["))

        val port = server.start(8090)
        assertTrue(port > 0)
        assertTrue(server.isRunning)
        server.stop()
        assertFalse(server.isRunning)
    }
}

