package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PropertyCard(
    property: PropertyListing,
    onCardClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
    isShortlisted: Boolean = false,
    onShortlistToggle: (() -> Unit)? = null
) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    val cardBorder = if (isShortlisted) {
        androidx.compose.foundation.BorderStroke(2.dp, BrajSaffronGold)
    } else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
            .testTag("property_card_${property.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isShortlisted) 4.dp else 2.dp)
    ) {
        Column {
            // Image with Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val imageRes = property.imageDrawableRes ?: R.drawable.img_brajvaas_hero
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Left Badges: Listing Type & Verified Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rent vs Sale Tag
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (property.listingType == ListingType.RENT) BrajEmeraldPrimary else BrajSaffronGold,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = if (property.listingType == ListingType.RENT) "FOR RENT" else "FOR SALE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Verified Badge
                    if (property.isVerified) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrajVerifiedGreen,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Brajvaas Verified",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Top Right: Shortlist Star Button & Sattvik tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (property.isPureSattvik) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xCC0F4C42)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = "Sattvik",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Sattvik",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Interactive Shortlist Star
                    if (onShortlistToggle != null) {
                        Surface(
                            shape = CircleShape,
                            color = if (isShortlisted) BrajSaffronGold else Color(0xCC000000),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { onShortlistToggle() }
                                .testTag("shortlist_toggle_${property.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isShortlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Shortlist",
                                    tint = if (isShortlisted) Color.White else Color(0xFFFFD54F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Left: 15-Day Brokerage & Services badge (Updated business model!)
                Surface(
                    shape = RoundedCornerShape(topEnd = 8.dp),
                    color = Color(0xEE082E28),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "15-DAY BROKERAGE • AGREEMENT & POLICE KYC BY US",
                            color = Color(0xFFFFD54F),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // If Shortlisted: Distinct floating label
                if (isShortlisted) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = BrajSaffronGold,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "★ SHORTLISTED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Card Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Price & Area Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val formattedPrice = if (property.listingType == ListingType.RENT) {
                            "₹${indianFormat.format(property.price)} / mo"
                        } else {
                            if (property.price >= 10000000) {
                                String.format(Locale.ENGLISH, "₹%.2f Cr", property.price / 10000000.0)
                            } else {
                                String.format(Locale.ENGLISH, "₹%.2f Lakh", property.price / 100000.0)
                            }
                        }
                        Text(
                            text = formattedPrice,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrajEmeraldPrimary
                        )

                        if (property.listingType == ListingType.RENT) {
                            val fee15Days = property.price / 2
                            Text(
                                text = "Brokerage: ₹${indianFormat.format(fee15Days)} (15 Days Rent)",
                                fontSize = 11.sp,
                                color = BrajSaffronGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // BHK & Area Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = property.bhk,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${property.builtUpAreaSqFt} sq.ft",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Property Title
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Locality & Landmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        tint = BrajSaffronGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${property.locality}, ${property.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sacred Temple Proximity Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (property.templeDistances.premMandirKm > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrajEmeraldContainer
                        ) {
                            Text(
                                text = "Prem Mandir: ${property.templeDistances.premMandirKm} km",
                                fontSize = 11.sp,
                                color = BrajOnEmeraldContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (property.templeDistances.bankeyBihariKm > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrajSaffronGoldContainer
                        ) {
                            Text(
                                text = "Bankey Bihari: ${property.templeDistances.bankeyBihariKm} km",
                                fontSize = 11.sp,
                                color = BrajOnSaffronContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom Action Bar: Secure Chat & View Details
                // Contact info is STRICTLY HIDDEN as required!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacy Shield",
                            tint = BrajEmeraldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Direct Secure Chat",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Chat Button
                    FilledTonalButton(
                        onClick = onChatClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = BrajEmeraldContainer,
                            contentColor = BrajEmeraldPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("chat_owner_btn_${property.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chat Owner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
