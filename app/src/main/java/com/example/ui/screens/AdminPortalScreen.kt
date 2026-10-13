package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.ApprovalStatus
import com.example.data.model.ListingType
import com.example.data.model.PropertyListing
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

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

    val pendingProperties = allProperties.filter { it.approvalStatus == ApprovalStatus.PENDING_APPROVAL }
    val approvedProperties = allProperties.filter { it.approvalStatus == ApprovalStatus.APPROVED }
    val rejectedProperties = allProperties.filter { it.approvalStatus == ApprovalStatus.REJECTED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Brajvaas Admin Portal", fontWeight = FontWeight.Black)
                        Text(
                            "Property Moderation & Verification Cell",
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
                modifier = Modifier.fillMaxWidth().clickable { viewModel.navigateTo(com.example.ui.AppScreen.WEB_ADMIN_PANEL) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Web Admin: View Users, Likes & Location Audit",
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

            // Explanation Banner
            Surface(
                color = BrajEmeraldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Workflow Enforcement: User submissions stay hidden until approved here.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrajOnEmeraldContainer
                    )
                }
            }

            // Tab Row
            PrimaryTabRow(selectedTabIndex = adminTab) {
                Tab(
                    selected = adminTab == 0,
                    onClick = { viewModel.setAdminTab(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    text = { Text("Live Approved (${approvedProperties.size})") }
                )
                Tab(
                    selected = adminTab == 2,
                    onClick = { viewModel.setAdminTab(2) },
                    text = { Text("Rejected (${rejectedProperties.size})") }
                )
            }

            val currentList = when (adminTab) {
                0 -> pendingProperties
                1 -> approvedProperties
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
                            imageVector = if (adminTab == 0) Icons.Default.CheckCircle else Icons.Default.Inbox,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (adminTab == 0) "All caught up! No pending properties." else "No properties in this section.",
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
                            }
                        )
                    }
                }
            }
        }
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
}

@Composable
private fun AdminPropertyReviewCard(
    property: PropertyListing,
    onApprove: (grantVerified: Boolean) -> Unit,
    onRejectClick: () -> Unit,
    onToggleVerify: () -> Unit
) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Status badge & ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                text = "${property.locality}, ${property.city} • ${property.fullAddress}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Price & Specs
            val priceStr = if (property.listingType == ListingType.RENT) {
                "₹${indianFormat.format(property.price)} / mo"
            } else {
                "₹${indianFormat.format(property.price)}"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(priceStr, fontWeight = FontWeight.ExtraBold, color = BrajEmeraldPrimary, fontSize = 16.sp)
                Text("${property.bhk} • ${property.builtUpAreaSqFt} sq.ft • ${property.propertyType.displayName}", fontSize = 12.sp, color = Color.DarkGray)
            }

            // Admin Inspection Details (Poster name and phone shown ONLY here to the admin!)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Admin Inspection Data:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                    Text("• Poster: ${property.ownerName} (${property.ownerPhone})", fontSize = 11.sp)
                    Text("• Legal Claim: ${property.legalStatus}", fontSize = 11.sp)
                    Text("• Prem Mandir: ${property.templeDistances.premMandirKm} km | Bankey Bihari: ${property.templeDistances.bankeyBihariKm} km", fontSize = 11.sp)
                    if (property.adminFeedback != null) {
                        Text("• Note: ${property.adminFeedback}", fontSize = 11.sp, color = BrajPendingOrange)
                    }
                }
            }

            HorizontalDivider()

            // Actions for Admin
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
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Verified Badge: ", fontSize = 12.sp)
                        Text(
                            if (property.isVerified) "Granted" else "None",
                            fontWeight = FontWeight.Bold,
                            color = if (property.isVerified) BrajVerifiedGreen else Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = onToggleVerify,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (property.isVerified) "Remove Badge" else "Grant Badge", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
