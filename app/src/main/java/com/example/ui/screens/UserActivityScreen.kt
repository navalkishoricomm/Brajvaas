package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PropertyListing
import com.example.ui.AppScreen
import com.example.ui.BrajvaasViewModel
import com.example.ui.components.PropertyCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserActivityScreen(
    viewModel: BrajvaasViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val shortlistedProps by viewModel.shortlistedProperties.collectAsState()
    val visitedPropsWithTime by viewModel.visitedPropertiesWithListing.collectAsState()
    val agreements by viewModel.agreements.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Shortlisted, 1: Visited, 2: Agreements

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Activity & Profile", fontWeight = FontWeight.Black)
                        Text(
                            "Shortlisted • Visited • Agreement Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary
                        )
                    }
                },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Gray)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.AUTH) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BrajEmeraldPrimary,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.fullName ?: "Guest Seeker",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${currentUser?.phone ?: "Not registered"} • ${currentUser?.city ?: "Vrindavan"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BrajEmeraldContainer
                                    ) {
                                        Text(
                                            text = currentUser?.role?.label ?: "Devotee / Tenant",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrajEmeraldPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BrajVerifiedGreenContainer
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("KYC Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrajVerifiedGreen)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Brokerage & Verification Guarantee Note
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrajSaffronGoldContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = BrajSaffronGold, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        text = "Brajvaas Managed Tenancy Service",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = BrajOnSaffronContainer
                                    )
                                    Text(
                                        text = "15 days rent brokerage from landlord & tenant. Rent agreements & Mathura police verification done by our team.",
                                        fontSize = 10.sp,
                                        color = BrajOnSaffronContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Metric Pills / Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("Shortlisted (${shortlistedProps.size}) ★", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = BrajSaffronGold, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrajSaffronGoldContainer,
                            selectedLabelColor = BrajOnSaffronContainer
                        ),
                        modifier = Modifier.testTag("tab_shortlisted")
                    )

                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("Visited (${visitedPropsWithTime.size})", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_visited")
                    )

                    FilterChip(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("Agreements (${agreements.size})", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_agreements")
                    )
                }
            }

            // Tab Content
            when (activeTab) {
                0 -> {
                    // SHORTLISTED PROPERTIES (Distinct golden styling so they show visually different!)
                    if (shortlistedProps.isEmpty()) {
                        item {
                            EmptyActivityState("No properties shortlisted yet", "Tap the bookmark star on any property to shortlist it.")
                        }
                    } else {
                        item {
                            Text(
                                text = "Your Shortlisted Properties (Distinct View)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrajSaffronGold
                            )
                        }
                        items(shortlistedProps, key = { it.id }) { prop ->
                            ShortlistedPropertyCard(
                                property = prop,
                                onCardClick = { viewModel.selectProperty(prop) },
                                onChatClick = { viewModel.openChatWithPropertyOwner(prop) },
                                onRequestAgreement = { viewModel.requestRentAgreementAndVerification(prop) },
                                onRemoveShortlist = { viewModel.toggleShortlist(prop.id) }
                            )
                        }
                    }
                }

                1 -> {
                    // VISITED PROPERTIES
                    if (visitedPropsWithTime.isEmpty()) {
                        item {
                            EmptyActivityState("No visited properties history", "Explore Vrindavan & Mathura properties to automatically build your visited history.")
                        }
                    } else {
                        item {
                            Text(
                                text = "Visited & Inspected Properties (${visitedPropsWithTime.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(visitedPropsWithTime, key = { it.first.id + it.second }) { (prop, time) ->
                            VisitedPropertyCard(
                                property = prop,
                                visitedTimestamp = time,
                                onCardClick = { viewModel.selectProperty(prop) },
                                onChatClick = { viewModel.openChatWithPropertyOwner(prop) }
                            )
                        }
                    }
                }

                2 -> {
                    // MANAGED AGREEMENTS & POLICE VERIFICATION
                    item {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.MANAGED_AGREEMENTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Complete Agreements & Verification Center", fontWeight = FontWeight.Bold)
                        }
                    }

                    items(agreements, key = { it.id }) { agr ->
                        AgreementTrackerCard(
                            agreement = agr,
                            onAdvanceAgreement = { viewModel.advanceAgreementStep(agr.id) },
                            onAdvancePv = { viewModel.advancePoliceVerification(agr.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShortlistedPropertyCard(
    property: PropertyListing,
    onCardClick: () -> Unit,
    onChatClick: () -> Unit,
    onRequestAgreement: () -> Unit,
    onRemoveShortlist: () -> Unit
) {
    // Distinct visual presentation for shortlisted property
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(2.dp, BrajSaffronGold, RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick)
            .testTag("shortlisted_card_${property.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Golden Shortlisted Ribbon
            Surface(
                color = BrajSaffronGold,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Text(
                            text = "SHORTLISTED PROPERTY",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onRemoveShortlist,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.BookmarkRemove, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Normal Property Card body inside distinct frame
            PropertyCard(
                property = property,
                onCardClick = onCardClick,
                onChatClick = onChatClick
            )

            // Direct CTA for Shortlisted Item: Initiate Agreement
            Surface(
                color = BrajEmeraldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Brajvaas 15-Day Brokerage Service",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrajEmeraldPrimary
                        )
                        Text(
                            text = "Agreement drafting & police verification by us",
                            fontSize = 10.sp,
                            color = BrajOnEmeraldContainer
                        )
                    }

                    Button(
                        onClick = onRequestAgreement,
                        colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Draft Agreement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VisitedPropertyCard(
    property: PropertyListing,
    visitedTimestamp: Long,
    onCardClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)
    val visitedStr = timeFormat.format(Date(visitedTimestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
            .testTag("visited_card_${property.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Visited Timestamp Tag Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.History, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Visited / Inspected by You: $visitedStr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BrajEmeraldPrimary
                    ) {
                        Text(
                            text = "Inspected",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            PropertyCard(
                property = property,
                onCardClick = onCardClick,
                onChatClick = onChatClick
            )
        }
    }
}

@Composable
fun AgreementTrackerCard(
    agreement: com.example.data.model.RentalAgreementTracker,
    onAdvanceAgreement: () -> Unit,
    onAdvancePv: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = agreement.agreementNumber,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = BrajEmeraldPrimary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BrajSaffronGoldContainer
                ) {
                    Text(
                        text = "15 Days Fee: ₹${agreement.brokerage15DaysFee}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BrajOnSaffronContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "${agreement.propertyTitle} • ${agreement.locality}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            // Step 1: Agreement
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("1. Rent Agreement (By Brajvaas):", fontSize = 11.sp, color = Color.Gray)
                        Text(agreement.agreementStatus.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                    }
                    TextButton(onClick = onAdvanceAgreement) {
                        Text("Advance Step", fontSize = 11.sp)
                    }
                }
            }

            // Step 2: Police Verification
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrajEmeraldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("2. Mathura Police Verification (By Brajvaas):", fontSize = 11.sp, color = Color.DarkGray)
                        Text(agreement.policeVerificationStatus.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrajVerifiedGreen)
                    }
                    TextButton(onClick = onAdvancePv) {
                        Text("Advance Step", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityState(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Bookmarks, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
