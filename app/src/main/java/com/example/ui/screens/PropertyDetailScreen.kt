package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ListingType
import com.example.data.model.PropertyListing
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    property: PropertyListing?,
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    if (property == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Property details not found")
        }
        return
    }

    // Automatically record visit in user history
    LaunchedEffect(property.id) {
        viewModel.recordPropertyVisit(property.id)
    }

    val shortlistedIds by viewModel.shortlistedPropertyIds.collectAsState()
    val isShortlisted = shortlistedIds.contains(property.id)
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property.bhk, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Shortlist star toggle
                    IconButton(
                        onClick = { viewModel.toggleShortlist(property.id) },
                        modifier = Modifier.testTag("detail_shortlist_btn")
                    ) {
                        Icon(
                            imageVector = if (isShortlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Shortlist",
                            tint = if (isShortlisted) BrajSaffronGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { viewModel.showNotification("Listing link copied for sharing.") }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.openChatWithPropertyOwner(property) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_owner_detail_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Owner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (property.listingType == ListingType.SHORT_STAY) {
                        Button(
                            onClick = { viewModel.navigateTo(com.example.ui.AppScreen.SHORT_STAYS) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("reserve_short_stay_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Luggage, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reserve Short Stay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.requestRentAgreementAndVerification(property) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("request_agreement_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Draft Agreement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                val imageRes = property.imageDrawableRes ?: R.drawable.img_brajvaas_hero
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Badges overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (property.listingType == ListingType.RENT) BrajEmeraldPrimary else BrajSaffronGold
                    ) {
                        Text(
                            text = if (property.listingType == ListingType.RENT) "FOR RENT" else "FOR SALE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (property.isVerified) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrajVerifiedGreen
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Brajvaas Verified", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Shortlisted floating tag if active
                if (isShortlisted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrajSaffronGold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shortlisted", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Price and Title Block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val formattedPrice = if (property.listingType == ListingType.RENT) {
                            "₹${indianFormat.format(property.price)} / month"
                        } else {
                            if (property.price >= 10000000) {
                                String.format(Locale.ENGLISH, "₹%.2f Crores", property.price / 10000000.0)
                            } else {
                                String.format(Locale.ENGLISH, "₹%.2f Lakhs", property.price / 100000.0)
                            }
                        }

                        Text(
                            text = formattedPrice,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = BrajEmeraldPrimary
                        )

                        Text(
                            text = property.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = BrajSaffronGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${property.locality}, ${property.city}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrajEmeraldContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Full exact address is confidential to protect owner privacy. Complete location details & directions are provided upon booking/visit confirmation.",
                                    fontSize = 11.sp,
                                    color = BrajEmeraldPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        if (property.landmark.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.NearMe, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Landmark: ${property.landmark}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Financial breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (property.listingType == ListingType.RENT) {
                                Column {
                                    Text("Security Deposit", fontSize = 11.sp, color = Color.Gray)
                                    Text("₹${indianFormat.format(property.deposit)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Monthly Maintenance", fontSize = 11.sp, color = Color.Gray)
                                    Text("₹${indianFormat.format(property.maintenanceCharges)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Column {
                                Text("Available From", fontSize = 11.sp, color = Color.Gray)
                                Text(property.availableFrom, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // BRAJVAAS MANAGED BROKERAGE & POLICE VERIFICATION CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrajEmeraldContainer)
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = BrajEmeraldPrimary)
                                Text(
                                    text = "Brajvaas Managed Tenancy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BrajEmeraldPrimary
                                )
                            }

                            if (property.listingType == ListingType.RENT) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BrajSaffronGold
                                ) {
                                    Text(
                                        text = "15 Days Brokerage",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (property.listingType == ListingType.RENT) {
                            val fee = property.price / 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Brokerage Policy:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (fee > 0L) {
                                    Text("₹${indianFormat.format(fee)} (15 Days Rent)", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = BrajEmeraldPrimary)
                                } else {
                                    Text("15 Days Rent as Brokerage", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                                }
                            }
                        }

                        HorizontalDivider(color = BrajEmeraldPrimary.copy(alpha = 0.2f))

                        Text(
                            text = "All legal & compliance work done directly by Brajvaas:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrajOnEmeraldContainer
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(15.dp))
                            Text("11-Month Legal E-Stamped Rent Agreement prepared & notarized by us", fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(15.dp))
                            Text("Mathura District Police Tenant Verification submitted & cleared by us", fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(15.dp))
                            Text("Accompanied Darshan & Physical Walkthrough with Brajvaas Mitra", fontSize = 11.sp)
                        }
                    }
                }

                // Sacred Temple Proximity Radar Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.TempleHindu, contentDescription = null, tint = BrajEmeraldPrimary)
                            Text(
                                text = "Sacred Braj Temple Proximities",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrajEmeraldPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TempleProximityItem("Prem Mandir", "${property.templeDistances.premMandirKm} km")
                            TempleProximityItem("Bankey Bihari", "${property.templeDistances.bankeyBihariKm} km")
                            TempleProximityItem("ISKCON Vrindavan", "${property.templeDistances.iskconKm} km")
                        }

                        HorizontalDivider(color = BrajEmeraldPrimary.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TempleProximityItem("Shri Krishna Janmabhoomi", "${property.templeDistances.janmabhoomiKm} km")
                            TempleProximityItem("Yamuna Ghat", "${property.templeDistances.yamunaGhatKm} km")
                            TempleProximityItem("Parikrama Access", "Walking")
                        }
                    }
                }

                // Property Specifications & Architecture
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Specifications & Building Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        SpecificationRow("Property Type", property.propertyType.displayName)
                        SpecificationRow("Configuration", property.bhk)
                        SpecificationRow("Super Built-up Area", "${property.builtUpAreaSqFt} sq.ft")
                        SpecificationRow("Carpet Area", "${property.carpetAreaSqFt} sq.ft")
                        SpecificationRow("Floor Level", "Floor ${property.floor} of ${property.totalFloors}")
                        SpecificationRow("Furnishing", property.furnishing.displayName)
                        SpecificationRow("Facing & Vaastu", property.facing)
                        SpecificationRow("Preferred Residents", property.preferredTenants)
                        SpecificationRow("Sattvik Community Code", if (property.isPureSattvik) "Mandatory Pure Vegetarian" else "General")
                        SpecificationRow("Legal Status", property.legalStatus)
                    }
                }

                // Amenities Checklist
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Amenities & Facilities",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            property.amenities.forEach { amenity ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BrajEmeraldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = amenity, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Strict Privacy Shield Box (Owner Phone/Email Masked as required!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FBF7))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrajEmeraldPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Brajvaas Privacy Shield",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrajEmeraldPrimary
                            )
                            Text(
                                text = "All property details are shown above. The owner's personal phone and email are kept confidential to prevent spam. Use in-app chat for communications, and Brajvaas will execute the rent agreement and police verification.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TempleProximityItem(name: String, distance: String) {
    Column {
        Text(text = name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = distance, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
    }
}

@Composable
private fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
