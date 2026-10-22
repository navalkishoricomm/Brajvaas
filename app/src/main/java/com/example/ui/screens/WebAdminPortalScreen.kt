package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppScreen
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAdminPortalScreen(
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val webAdminUrl by viewModel.webServerUrl.collectAsState()

    var isWebViewMode by remember { mutableStateOf(true) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    var activeTab by remember { mutableIntStateOf(0) }
    // Tabs: 0: Overview, 1: Users, 2: Property Listings, 3: User Leads (Likes/Interests), 4: Short Stays, 5: Location Logs

    val users by viewModel.usersList.collectAsState()
    val allProperties by viewModel.allProperties.collectAsState()
    val leads by viewModel.userInterestLeads.collectAsState()
    val shortStays by viewModel.shortStayBookings.collectAsState()
    val locationLogs by viewModel.locationAuditLogs.collectAsState()
    val pendingCount by viewModel.pendingListingsCount.collectAsState()
    val newLeadsCount by viewModel.totalLeadsCount.collectAsState()

    var rejectTargetId by remember { mutableStateOf<String?>(null) }
    var rejectReasonText by remember { mutableStateOf("Please provide clearer ownership proof or MVDA layout.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Brajvaas Web Admin Console", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BrajEmeraldPrimary
                            ) {
                                Text(
                                    "HTTP 8080",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "User Management • Property Approval • Leads • Exact Locations",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("web_admin_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { reloadTrigger++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload Web View")
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
            // Web Link Browser Bar (Addresses user requirement: "the admin panel would be web based. please provide link too.")
            Surface(
                color = Color(0xFF1E293B), // Dark Web Console Header
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Text(
                                text = webAdminUrl,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Copy Link Button
                            FilledTonalButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Brajvaas Admin URL", webAdminUrl)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.showNotification("Local Web Admin URL copied: $webAdminUrl")
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF334155),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("copy_admin_url_btn")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy URL", fontSize = 11.sp)
                            }

                            // Copy HTML Source Button
                            FilledTonalButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val html = viewModel.getAdminWebHtml()
                                    val clip = ClipData.newPlainText("Brajvaas Admin HTML", html)
                                    clipboard.setPrimaryClip(clip)
                                    viewModel.showNotification("Complete HTML code copied! Save as admin.html to open on any PC/Mac browser.")
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF334155),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("copy_admin_html_btn")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy HTML", fontSize = 11.sp)
                            }

                            // Open in Browser Button
                            Button(
                                onClick = {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webAdminUrl))
                                    try {
                                        context.startActivity(browserIntent)
                                    } catch (e: Exception) {
                                        viewModel.showNotification("Opening in internal Web View tab below.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("open_admin_browser_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Chrome", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Explanatory guidance text for the user
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                            Text(
                                text = "Embedded HTTP server is active on $webAdminUrl. Use the Interactive Web View below, open in Chrome on this device, or copy the HTML for any desktop browser.",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // View Mode Selector (Interactive Web View vs Native Android Console)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = isWebViewMode,
                        onClick = { isWebViewMode = true },
                        label = { Text("Interactive Web Browser View", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrajEmeraldPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = !isWebViewMode,
                        onClick = { isWebViewMode = false },
                        label = { Text("Native Android View", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrajEmeraldPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isWebViewMode) {
                // Interactive Web View embedding the HTML5/JS Web Admin Panel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    key(reloadTrigger) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.allowFileAccess = true
                                    settings.databaseEnabled = true
                                    settings.useWideViewPort = true
                                    settings.loadWithOverviewMode = true
                                    webViewClient = WebViewClient()
                                    loadDataWithBaseURL(
                                        webAdminUrl,
                                        viewModel.getAdminWebHtml(),
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            update = { webView ->
                                if (reloadTrigger > 0) {
                                    webView.loadDataWithBaseURL(
                                        webAdminUrl,
                                        viewModel.getAdminWebHtml(),
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                // Scrollable Native Android Tabs
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Overview", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Users (${users.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Properties", fontWeight = FontWeight.Bold)
                                if (pendingCount > 0) {
                                    Badge(containerColor = BrajPendingOrange) { Text("$pendingCount") }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("User Likes & Leads", fontWeight = FontWeight.Bold)
                                if (newLeadsCount > 0) {
                                    Badge(containerColor = BrajSaffronGold) { Text("$newLeadsCount") }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        text = { Text("Short Stays (₹200)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 5,
                        onClick = { activeTab = 5 },
                        text = { Text("Exact Location Logs", fontWeight = FontWeight.Bold) }
                    )
                }

            // Tab Content
            when (activeTab) {
                0 -> AdminOverviewTab(
                    usersCount = users.size,
                    pendingListings = pendingCount,
                    approvedListings = allProperties.count { it.approvalStatus == ApprovalStatus.APPROVED },
                    leadsCount = leads.size,
                    shortStaysCount = shortStays.size,
                    onSwitchTab = { activeTab = it }
                )
                1 -> AdminUserManagementTab(
                    users = users,
                    onDeleteUser = { viewModel.deleteUser(it) },
                    onToggleKyc = { viewModel.toggleUserKyc(it) }
                )
                2 -> AdminPropertyManagementTab(
                    allProperties = allProperties,
                    onApprove = { id, verified -> viewModel.adminApproveProperty(id, verified) },
                    onReject = { rejectTargetId = it },
                    onDelete = { viewModel.deleteProperty(it) },
                    onToggleVerify = { viewModel.adminToggleVerification(it) }
                )
                3 -> AdminUserLeadsTab(
                    leads = leads,
                    onUpdateStatus = { id, status -> viewModel.updateLeadStatus(id, status) },
                    onContactUser = { phone, name ->
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        try {
                            context.startActivity(callIntent)
                        } catch (e: Exception) {
                            viewModel.showNotification("Contacting $name at $phone...")
                        }
                    }
                )
                4 -> AdminShortStaysTab(bookings = shortStays)
                5 -> AdminLocationAuditTab(logs = locationLogs)
            }
        }
    }
}

    // Rejection Dialog
    if (rejectTargetId != null) {
        AlertDialog(
            onDismissRequest = { rejectTargetId = null },
            title = { Text("Reject Listing", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide feedback for the user in Mathura-Vrindavan:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = rejectTargetId ?: return@Button
                        viewModel.adminRejectProperty(id, rejectReasonText)
                        rejectTargetId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrajRejectedRed)
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ---------------- TAB 0: OVERVIEW ----------------
@Composable
private fun AdminOverviewTab(
    usersCount: Int,
    pendingListings: Int,
    approvedListings: Int,
    leadsCount: Int,
    shortStaysCount: Int,
    onSwitchTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Vrindavan-Mathura Admin Cell Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminMetricCard(
                        title = "Pending Approvals",
                        count = "$pendingListings",
                        badge = if (pendingListings > 0) "Action Required" else "Clear",
                        badgeColor = if (pendingListings > 0) BrajPendingOrange else BrajVerifiedGreen,
                        modifier = Modifier.weight(1f).clickable { onSwitchTab(2) }
                    )
                    AdminMetricCard(
                        title = "User Leads / Likes",
                        count = "$leadsCount",
                        badge = "Admin Contact",
                        badgeColor = BrajSaffronGold,
                        modifier = Modifier.weight(1f).clickable { onSwitchTab(3) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminMetricCard(
                        title = "Registered Users",
                        count = "$usersCount",
                        badge = "Devotees & Landlords",
                        badgeColor = BrajEmeraldPrimary,
                        modifier = Modifier.weight(1f).clickable { onSwitchTab(1) }
                    )
                    AdminMetricCard(
                        title = "Short Stays",
                        count = "$shortStaysCount",
                        badge = "₹200 Fee per Stay",
                        badgeColor = BrajEmeraldPrimary,
                        modifier = Modifier.weight(1f).clickable { onSwitchTab(4) }
                    )
                }
            }
        }

        // Brokerage & Verification Model Summary Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Brajvaas Revenue & Operational Model", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Rental Properties:", fontSize = 12.sp, color = Color.Gray)
                            Text("15 Days Rent from Landlord & 15 Days from Tenant", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrajEmeraldPrimary)
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Vrindavan Short Stays:", fontSize = 12.sp, color = Color.Gray)
                            Text("Flat ₹200 from Tenant & ₹200 from Landlord per stay", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrajSaffronGold)
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Legal & Police Verification:", fontSize = 12.sp, color = Color.Gray)
                            Text("Managed end-to-end by Brajvaas Admin Cell (Mathura District)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 1: USERS ----------------
@Composable
private fun AdminUserManagementTab(
    users: List<UserProfile>,
    onDeleteUser: (String) -> Unit,
    onToggleKyc: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users, key = { it.id }) { user ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("user_item_${user.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = BrajEmeraldContainer, modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(user.role.label, fontSize = 11.sp, color = BrajEmeraldPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (user.isKycVerified) BrajVerifiedGreenContainer else BrajPendingOrangeContainer
                        ) {
                            Text(
                                if (user.isKycVerified) "KYC Verified ✓" else "KYC Pending",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user.isKycVerified) BrajVerifiedGreen else BrajPendingOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text("Contact: ${user.phone} • ${user.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Exact Logged Location
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Logged GPS: ${user.lastKnownCoordinates} (${user.lastKnownLocality})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onToggleKyc(user.id) }) {
                            Text(if (user.isKycVerified) "Revoke KYC" else "Grant KYC", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedButton(
                            onClick = { onDeleteUser(user.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrajRejectedRed),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Delete User", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 2: PROPERTY MODERATION ----------------
@Composable
private fun AdminPropertyManagementTab(
    allProperties: List<PropertyListing>,
    onApprove: (id: String, grantVerified: Boolean) -> Unit,
    onReject: (id: String) -> Unit,
    onDelete: (id: String) -> Unit,
    onToggleVerify: (id: String) -> Unit
) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(allProperties, key = { it.id }) { prop ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (prop.approvalStatus) {
                                ApprovalStatus.PENDING_APPROVAL -> BrajPendingOrangeContainer
                                ApprovalStatus.APPROVED -> BrajVerifiedGreenContainer
                                ApprovalStatus.REJECTED -> BrajRejectedRedContainer
                            }
                        ) {
                            Text(
                                prop.approvalStatus.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (prop.approvalStatus) {
                                    ApprovalStatus.PENDING_APPROVAL -> BrajPendingOrange
                                    ApprovalStatus.APPROVED -> BrajVerifiedGreen
                                    ApprovalStatus.REJECTED -> BrajRejectedRed
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text("₹${indianFormat.format(prop.price)}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = BrajEmeraldPrimary)
                    }

                    Text(prop.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${prop.locality}, ${prop.city} • ${prop.bhk}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Confidential Poster details shown to admin
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrajEmeraldContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Poster: ${prop.ownerName} (${prop.ownerPhone})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Exact GPS: ${prop.loggedCoordinates} • ${prop.loggedLocality}", fontSize = 10.sp)
                        }
                    }

                    // Action buttons
                    if (prop.approvalStatus == ApprovalStatus.PENDING_APPROVAL) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onReject(prop.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrajRejectedRed),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Reject", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onApprove(prop.id, false) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Approve", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onApprove(prop.id, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrajSaffronGold),
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Approve & Verify", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onToggleVerify(prop.id) }) {
                                Text(if (prop.isVerified) "Remove Verified" else "Make Verified", fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = { onDelete(prop.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = BrajRejectedRed)
                            ) {
                                Text("Delete Listing", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 3: USER INTERESTS & LEADS ----------------
// Specifically addresses: "if a user like a property, that interest would be visible to admin and he will contact the user for further updates."
@Composable
private fun AdminUserLeadsTab(
    leads: List<UserInterestLead>,
    onUpdateStatus: (id: String, status: String) -> Unit,
    onContactUser: (phone: String, name: String) -> Unit
) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrajSaffronGoldContainer.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = BrajOnSaffronContainer)
                    Text(
                        text = "User Interest Pipeline: When users like, shortlist, or book properties, their contact info and exact location are logged here so admin can assist them.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrajOnSaffronContainer
                    )
                }
            }
        }

        items(leads, key = { it.id }) { lead ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("lead_card_${lead.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                lead.status.contains("New") -> BrajPendingOrangeContainer
                                lead.status.contains("Contacted") -> BrajEmeraldContainer
                                else -> BrajVerifiedGreenContainer
                            }
                        ) {
                            Text(
                                lead.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    lead.status.contains("New") -> BrajPendingOrange
                                    lead.status.contains("Contacted") -> BrajEmeraldPrimary
                                    else -> BrajVerifiedGreen
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(lead.leadType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajSaffronGold)
                    }

                    // Seeker info
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(18.dp))
                        Column {
                            Text(lead.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Mobile: ${lead.userPhone} • ${lead.userEmail}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Property of Interest
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Interested Property:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(lead.propertyTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Locality: ${lead.propertyLocality} • ₹${indianFormat.format(lead.propertyPrice)}", fontSize = 11.sp, color = BrajEmeraldPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Exact Logged Location
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Exact Logged Location: ${lead.userExactLocation}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }

                    HorizontalDivider()

                    // Admin Actions to Contact User
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onContactUser(lead.userPhone, lead.userName) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("contact_user_btn_${lead.id}")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Seeker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(
                                onClick = { onUpdateStatus(lead.id, "Contacted - Discussion Ongoing") },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Mark Contacted", fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = { onUpdateStatus(lead.id, "Visit Scheduled in Vrindavan") },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Schedule Visit", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 4: SHORT STAYS ----------------
@Composable
private fun AdminShortStaysTab(bookings: List<ShortStayBooking>) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrajEmeraldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Vrindavan Short Stays & Homestays (Flat ₹200 Platform Fee)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrajEmeraldPrimary)
                    Text(
                        "We charge a flat ₹200 platform fee upon booking irrespective of stay duration.",
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(bookings, key = { it.id }) { b ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(b.propertyTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(shape = RoundedCornerShape(4.dp), color = BrajVerifiedGreenContainer) {
                            Text(b.status, color = BrajVerifiedGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Text("Guest: ${b.guestName} (${b.guestPhone}) • Host: ${b.hostName} (${b.hostPhone})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Dates: ${b.checkInDate} to ${b.checkOutDate} (${b.numberOfNights} nights, ${b.numberOfGuests} guests)", fontSize = 11.sp)

                    // Fee breakdown
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tenant Fee: ₹200 (Paid)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                                Text("Landlord Fee: ₹200 (Deducted)", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Platform Revenue", fontSize = 10.sp, color = Color.Gray)
                                Text("₹400", fontWeight = FontWeight.Black, fontSize = 14.sp, color = BrajEmeraldPrimary)
                            }
                        }
                    }

                    // Exact Logged Location
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(14.dp))
                        Text("Logged GPS: ${b.guestExactCoordinates}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ---------------- TAB 5: EXACT LOCATION AUDIT LOG ----------------
// Specifically addresses: "also log location of user whosoever posting, taking on rent, buying or shorts stays and that exect locatoin should be logged and should be visible to admin."
@Composable
private fun AdminLocationAuditTab(logs: List<UserLocationAuditLog>) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrajEmeraldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = BrajEmeraldPrimary)
                    Column {
                        Text("Live User Location Tracking & Audit Log", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrajEmeraldPrimary)
                        Text("Exact GPS coordinates logged for every post, rent inquiry, purchase, and short stay.", fontSize = 11.sp)
                    }
                }
            }
        }

        items(logs, key = { it.id }) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when (log.actionType) {
                            "POSTED_PROPERTY" -> BrajEmeraldContainer
                            "SHORT_STAY_BOOKING" -> BrajSaffronGoldContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (log.actionType) {
                                    "POSTED_PROPERTY" -> Icons.Default.AddHomeWork
                                    "SHORT_STAY_BOOKING" -> Icons.Default.Luggage
                                    "PROPERTY_LIKED" -> Icons.Default.Favorite
                                    else -> Icons.Default.Key
                                },
                                contentDescription = null,
                                tint = BrajEmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(sdf.format(Date(log.timestamp)), fontSize = 10.sp, color = Color.Gray)
                        }

                        Text(log.actionDescription, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrajEmeraldPrimary)

                        if (log.propertyTitle != null) {
                            Text("Property: ${log.propertyTitle}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "Coordinates: ${log.exactCoordinates} (${log.geoLocality})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    count: String,
    badge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(count, fontSize = 24.sp, fontWeight = FontWeight.Black, color = BrajEmeraldPrimary)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
