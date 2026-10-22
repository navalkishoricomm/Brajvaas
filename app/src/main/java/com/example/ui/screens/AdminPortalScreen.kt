package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val allProperties by viewModel.allProperties.collectAsState()
    val adminTab by viewModel.adminTab.collectAsState()

    var rejectTargetPropertyId by remember { mutableStateOf<String?>(null) }
    var rejectReason by remember { mutableStateOf("Please provide MVDA approved layout map copy.") }
    var editingProperty by remember { mutableStateOf<PropertyListing?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val pendingProperties = allProperties.filter { it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }
    val liveProperties = allProperties.filter { it.approvalStatus == ApprovalStatus.APPROVED && it.availabilityStatus == AvailabilityStatus.AVAILABLE }
    val archivedProperties = allProperties.filter { it.availabilityStatus != AvailabilityStatus.AVAILABLE }
    val rejectedProperties = allProperties.filter { it.approvalStatus == ApprovalStatus.REJECTED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Brajvaas Admin Portal", fontWeight = FontWeight.Black)
                        Text(
                            "Verification Cell • Moderation & Archive",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Clear all postings button
                    IconButton(
                        onClick = { showClearAllConfirm = true },
                        modifier = Modifier.testTag("admin_clear_all_btn")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All Postings", tint = BrajRejectedRed)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.navigateTo(com.example.ui.AppScreen.WEB_ADMIN_PANEL) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = BrajEmeraldPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("admin_open_web_console_btn")
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Web Console", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Web Console Notice Banner
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(com.example.ui.AppScreen.WEB_ADMIN_PANEL) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Web Admin: View Users, Leads & Exact GPS Logs",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Open Web View ›",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tab Row (4 Tabs: Pending, Live, Archived/Sold, Rejected)
            ScrollableTabRow(
                selectedTabIndex = adminTab.coerceIn(0, 3),
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = adminTab == 0,
                    onClick = { viewModel.setAdminTab(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pending Review", fontWeight = FontWeight.Bold)
                            if (pendingProperties.isNotEmpty()) {
                                Badge(containerColor = BrajPendingOrange) {
                                    Text("${pendingProperties.size}")
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = adminTab == 1,
                    onClick = { viewModel.setAdminTab(1) },
                    text = { Text("Live Active (${liveProperties.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 2,
                    onClick = { viewModel.setAdminTab(2) },
                    text = { Text("Sold / Rented (${archivedProperties.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == 3,
                    onClick = { viewModel.setAdminTab(3) },
                    text = { Text("Rejected (${rejectedProperties.size})", fontWeight = FontWeight.Bold) }
                )
            }

            val currentList = when (adminTab) {
                0 -> pendingProperties
                1 -> liveProperties
                2 -> archivedProperties
                else -> rejectedProperties
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (adminTab) {
                                0 -> Icons.Default.CheckCircle
                                1 -> Icons.Default.HomeWork
                                2 -> Icons.Default.Archive
                                else -> Icons.Default.Inbox
                            },
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = when (adminTab) {
                                0 -> "All caught up! No pending properties."
                                1 -> "No live active properties currently."
                                2 -> "No sold or rented-out properties yet."
                                else -> "No rejected properties."
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currentList, key = { it.id }) { item ->
                        AdminPropertyReviewCard(
                            property = item,
                            onApprove = { grantVerified ->
                                viewModel.adminApproveProperty(item.id, grantVerified)
                            },
                            onRejectClick = {
                                rejectTargetPropertyId = item.id
                            },
                            onToggleVerify = {
                                viewModel.adminToggleVerification(item.id)
                            },
                            onEditClick = {
                                editingProperty = item
                            },
                            onMarkAvailability = { newStatus ->
                                viewModel.markPropertyAvailability(item.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }

    // Edit Property Dialog (Admin can edit while verifying or when already live)
    if (editingProperty != null) {
        val prop = editingProperty!!
        AdminEditPropertyDialog(
            property = prop,
            onDismiss = { editingProperty = null },
            onSave = { updated ->
                viewModel.updatePropertyDetails(updated)
                editingProperty = null
            }
        )
    }

    // Rejection Reason Dialog
    if (rejectTargetPropertyId != null) {
        AlertDialog(
            onDismissRequest = { rejectTargetPropertyId = null },
            title = { Text("Reject Property Submission", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide feedback for the property owner in Mathura-Vrindavan:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = rejectTargetPropertyId ?: return@Button
                        viewModel.adminRejectProperty(id, rejectReason)
                        rejectTargetPropertyId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrajRejectedRed)
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetPropertyId = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirm Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = BrajRejectedRed) },
            title = { Text("Clear All Property Postings?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will wipe all properties, shortlists, and visit histories from the app so you can start posting fresh from the beginning.", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllPostings()
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrajRejectedRed)
                ) {
                    Text("Yes, Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminPropertyReviewCard(
    property: PropertyListing,
    onApprove: (grantVerified: Boolean) -> Unit,
    onRejectClick: () -> Unit,
    onToggleVerify: () -> Unit,
    onEditClick: () -> Unit,
    onMarkAvailability: (AvailabilityStatus) -> Unit
) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val isAvailable = property.availabilityStatus == AvailabilityStatus.AVAILABLE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_card_${property.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Status badge & ID & Availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (property.approvalStatus) {
                            ApprovalStatus.PENDING_APPROVAL -> BrajPendingOrangeContainer
                            ApprovalStatus.APPROVED -> BrajVerifiedGreenContainer
                            ApprovalStatus.REJECTED -> BrajRejectedRedContainer
                        }
                    ) {
                        Text(
                            text = property.approvalStatus.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (property.approvalStatus) {
                                ApprovalStatus.PENDING_APPROVAL -> BrajPendingOrange
                                ApprovalStatus.APPROVED -> BrajVerifiedGreen
                                ApprovalStatus.REJECTED -> BrajRejectedRed
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (property.availabilityStatus != AvailabilityStatus.AVAILABLE) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF64748B)
                        ) {
                            Text(
                                text = property.availabilityStatus.displayName.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "ID: ${property.id.takeLast(8)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Title & Locality
            Text(
                text = property.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${property.bhk} • ${property.locality}, ${property.city} • ${property.fullAddress}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Price & Specs
            val priceStr = if (property.listingType == ListingType.RENT) {
                "₹${indianFormat.format(property.price)} / mo"
            } else if (property.listingType == ListingType.SHORT_STAY) {
                "₹${indianFormat.format(property.price)} / night"
            } else {
                "₹${indianFormat.format(property.price)}"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(priceStr, fontWeight = FontWeight.ExtraBold, color = BrajEmeraldPrimary, fontSize = 16.sp)
                Text("${property.builtUpAreaSqFt} sq.ft • ${property.propertyType.displayName}", fontSize = 12.sp, color = Color.DarkGray)
            }

            // Confidential Owner Contact (Visible ONLY to Admin!)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔒 Confidential Owner Contact (Admin Protected):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                    Text("• Name: ${property.ownerName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Phone: ${property.ownerPhone}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("• Photos Attached: ${property.photos.size} image(s)", fontSize = 11.sp)
                    if (property.closedAtTimestamp != null) {
                        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
                        Text("• Deal Marked: ${dateFormat.format(Date(property.closedAtTimestamp))}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }

            HorizontalDivider()

            // Row 1: Edit & Verification controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Details", fontSize = 11.sp)
                }

                if (property.approvalStatus == ApprovalStatus.APPROVED) {
                    FilledTonalButton(
                        onClick = onToggleVerify,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (property.isVerified) "Remove Badge" else "Grant Badge", fontSize = 11.sp)
                    }
                }
            }

            // Row 2: Mark Sold / Rented / Full Controls
            if (property.approvalStatus == ApprovalStatus.APPROVED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (property.listingType) {
                        ListingType.RENT -> {
                            if (isAvailable) {
                                Button(
                                    onClick = { onMarkAvailability(AvailabilityStatus.RENTED_OUT) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark as Rented Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onMarkAvailability(AvailabilityStatus.AVAILABLE) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Re-list as Available", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        ListingType.SELL -> {
                            if (isAvailable) {
                                Button(
                                    onClick = { onMarkAvailability(AvailabilityStatus.SOLD) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark as Sold", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onMarkAvailability(AvailabilityStatus.AVAILABLE) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Re-list as Available", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        ListingType.SHORT_STAY -> {
                            if (isAvailable) {
                                Button(
                                    onClick = { onMarkAvailability(AvailabilityStatus.FULLY_BOOKED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark as Full / Booked", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onMarkAvailability(AvailabilityStatus.AVAILABLE) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Re-open for Pilgrims", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Row 3: Pending Review Approve / Reject Buttons
            if (property.approvalStatus == ApprovalStatus.PENDING_APPROVAL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrajRejectedRed),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onApprove(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("admin_approve_${property.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onApprove(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrajSaffronGold),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("admin_approve_verify_${property.id}"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Verify", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminEditPropertyDialog(
    property: PropertyListing,
    onDismiss: () -> Unit,
    onSave: (PropertyListing) -> Unit
) {
    var title by remember { mutableStateOf(property.title) }
    var priceText by remember { mutableStateOf(property.price.toString()) }
    var bhk by remember { mutableStateOf(property.bhk) }
    var locality by remember { mutableStateOf(property.locality) }
    var fullAddress by remember { mutableStateOf(property.fullAddress) }
    var landmark by remember { mutableStateOf(property.landmark) }
    var builtUpAreaText by remember { mutableStateOf(property.builtUpAreaSqFt.toString()) }
    var ownerName by remember { mutableStateOf(property.ownerName) }
    var ownerPhone by remember { mutableStateOf(property.ownerPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Property Posting Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Listing Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price / Rent (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bhk,
                        onValueChange = { bhk = it },
                        label = { Text("BHK") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        label = { Text("Locality") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = builtUpAreaText,
                        onValueChange = { builtUpAreaText = it },
                        label = { Text("Area (sq.ft)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("Full Address (Confidential)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Nearby Landmark") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Confidential Landlord Contact:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        label = { Text("Owner Phone") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = property.copy(
                        title = title.ifBlank { property.title },
                        price = priceText.toLongOrNull() ?: property.price,
                        pricePerNight = if (property.listingType == ListingType.SHORT_STAY) (priceText.toLongOrNull() ?: property.price) else 0L,
                        bhk = bhk,
                        locality = locality,
                        fullAddress = fullAddress,
                        landmark = landmark,
                        builtUpAreaSqFt = builtUpAreaText.toIntOrNull() ?: property.builtUpAreaSqFt,
                        ownerName = ownerName,
                        ownerPhone = ownerPhone
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
