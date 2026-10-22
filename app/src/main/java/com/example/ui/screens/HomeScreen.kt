package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ListingType
import com.example.data.model.PropertyListing
import com.example.ui.AppScreen
import com.example.ui.BrajvaasViewModel
import com.example.ui.components.PropertyCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BrajvaasViewModel,
    modifier: Modifier = Modifier
) {
    val visibleProperties by viewModel.visibleProperties.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val pendingCount by viewModel.pendingListingsCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val shortlistedIds by viewModel.shortlistedPropertyIds.collectAsState()

    val localities = listOf(
        "All",
        "Raman Reti",
        "Chhatikara Road",
        "Rukmini Vihar",
        "Bankey Bihari Zone",
        "Sunrakh Road",
        "Krishna Nagar",
        "NH-19 Mathura-Vrindavan",
        "Govardhan Road"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrajEmeraldPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.brajvaas_logo_icon),
                                contentDescription = "Brajvaas Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = "Brajvaas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = BrajEmeraldPrimary
                            )
                            Text(
                                text = "Vrindavan & Mathura • 15 Days Brokerage",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrajSaffronGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Shortlisted / Visited User Activity button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.USER_ACTIVITY) },
                        modifier = Modifier.testTag("user_activity_top_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (shortlistedIds.isNotEmpty()) {
                                    Badge(containerColor = BrajSaffronGold) {
                                        Text("${shortlistedIds.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "My Shortlisted & Visited",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Direct chat inbox button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MESSAGES) },
                        modifier = Modifier.testTag("messages_shortcut_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = "Inbox",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.POST_PROPERTY) },
                icon = { Icon(Icons.Default.AddHomeWork, contentDescription = null) },
                text = { Text("Post Property", fontWeight = FontWeight.Bold) },
                containerColor = BrajEmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("post_property_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_brajvaas_hero),
                        contentDescription = "Vrindavan Abodes",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33000000),
                                        Color(0xCC082E28)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TempleHindu,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "JAI SHRI KRISHNA",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Find Your Sacred Abode in Braj",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "15-Day Managed Brokerage • Agreement & Police KYC by Us",
                            color = Color(0xFFD4EBE6),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Dedicated Service Action Cards (Short Stays & 11-Month Agreements)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Vrindavan Short Stays Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrajSaffronGoldContainer),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.SHORT_STAYS) }
                            .testTag("home_short_stays_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Luggage, contentDescription = null, tint = BrajOnSaffronContainer, modifier = Modifier.size(16.dp))
                                Text("Short Stays", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrajOnSaffronContainer)
                            }
                            Text("Holy Pilgrim Stays", fontWeight = FontWeight.Black, fontSize = 13.sp, color = BrajOnSaffronContainer)
                            Surface(shape = RoundedCornerShape(4.dp), color = BrajSaffronGold) {
                                Text("Devotee Homestays", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // 11-Month Rental Agreements Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrajEmeraldContainer),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(AppScreen.MANAGED_AGREEMENTS) }
                            .testTag("home_agreements_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(16.dp))
                                Text("Rental Care", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrajEmeraldPrimary)
                            }
                            Text("11-Month Leases", fontWeight = FontWeight.Black, fontSize = 13.sp, color = BrajEmeraldPrimary)
                            Surface(shape = RoundedCornerShape(4.dp), color = BrajEmeraldPrimary) {
                                Text("15 Days Brokerage", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // Rent vs Sell vs Short Stay Segmented Control
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = filter.listingType == ListingType.RENT,
                            onClick = { viewModel.setListingType(ListingType.RENT) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = BrajEmeraldPrimary,
                                activeContentColor = Color.White
                            ),
                            modifier = Modifier.testTag("rent_tab_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(13.dp))
                                Text("Rent", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        SegmentedButton(
                            selected = filter.listingType == ListingType.SELL,
                            onClick = { viewModel.setListingType(ListingType.SELL) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = BrajSaffronGold,
                                activeContentColor = Color.White
                            ),
                            modifier = Modifier.testTag("buy_tab_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(13.dp))
                                Text("Buy / Sell", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        SegmentedButton(
                            selected = filter.listingType == ListingType.SHORT_STAY,
                            onClick = { viewModel.setListingType(ListingType.SHORT_STAY) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Color(0xFF0D9488),
                                activeContentColor = Color.White
                            ),
                            modifier = Modifier.testTag("short_stays_tab_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Luggage, contentDescription = null, modifier = Modifier.size(13.dp))
                                Text("Short Stay", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search Prem Mandir, Raman Reti, 2BHK...",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = BrajEmeraldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrajEmeraldPrimary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    IconButton(
                        onClick = { viewModel.toggleFilterSheet(true) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrajEmeraldContainer)
                            .testTag("filter_icon_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (filter.onlyVerified || filter.onlySattvik || filter.locality != "All" || filter.bhkList.isNotEmpty()) {
                                    Badge(containerColor = BrajSaffronGold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filters",
                                tint = BrajEmeraldPrimary
                            )
                        }
                    }
                }
            }

            // Locality Quick Chips (Horizontal Scroll)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Vrindavan & Mathura Localities",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        localities.forEach { loc ->
                            val isSelected = filter.locality == loc
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BrajEmeraldPrimary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setLocalityFilter(loc) }
                            ) {
                                Text(
                                    text = loc,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Results Counter Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${visibleProperties.size} Verified Properties Available",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "15-Day Brokerage Service",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrajEmeraldPrimary
                    )
                }
            }

            // Empty state or Property Cards List
            if (visibleProperties.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No approved properties found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing filters or search another locality in Mathura-Vrindavan.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setLocalityFilter("All")
                            }
                        ) {
                            Text("Reset Search")
                        }
                    }
                }
            } else {
                items(visibleProperties, key = { it.id }) { property ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        PropertyCard(
                            property = property,
                            onCardClick = { viewModel.selectProperty(property) },
                            onChatClick = { viewModel.openChatWithPropertyOwner(property) },
                            isShortlisted = shortlistedIds.contains(property.id),
                            onShortlistToggle = { viewModel.toggleShortlist(property.id) }
                        )
                    }
                }
            }
        }
    }
}
