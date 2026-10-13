package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.PropertyListing
import com.example.ui.AppScreen
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortStaysScreen(
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val shortStays by viewModel.vrindavanShortStays.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val existingBookings by viewModel.shortStayBookings.collectAsState()

    var bookingTargetProperty by remember { mutableStateOf<PropertyListing?>(null) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Browse Stays, 1: My Bookings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Vrindavan Short Stays", fontWeight = FontWeight.Black)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BrajSaffronGold
                            ) {
                                Text(
                                    "AirBnb Style",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Exclusively Vrindavan • ₹200 Flat Fee per Stay",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("short_stays_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.WEB_ADMIN_PANEL) }) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin View", tint = BrajEmeraldPrimary)
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
            // Pricing policy card
            Surface(
                color = BrajSaffronGoldContainer.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        tint = BrajOnSaffronContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Transparent Pricing: Flat ₹200 from Tenant & ₹200 from Landlord",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BrajOnSaffronContainer
                        )
                        Text(
                            text = "Irrespective of stay duration! No hidden percentage commission.",
                            fontSize = 11.sp,
                            color = BrajOnSaffronContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Tab Row
            PrimaryTabRow(selectedTabIndex = activeSubTab) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Available Homestays (${shortStays.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("My Bookings (${existingBookings.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (activeSubTab == 0) {
                // Browse stays
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(shortStays, key = { it.id }) { stay ->
                        ShortStayCard(
                            stay = stay,
                            onBookClick = { bookingTargetProperty = stay },
                            onDetailClick = { viewModel.selectProperty(stay) }
                        )
                    }
                }
            } else {
                // Bookings list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (existingBookings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No short stays booked yet.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(existingBookings, key = { it.id }) { booking ->
                            BookingSummaryCard(booking = booking)
                        }
                    }
                }
            }
        }
    }

    // Booking Dialog
    bookingTargetProperty?.let { property ->
        ShortStayBookingDialog(
            property = property,
            currentUser = currentUser,
            onDismiss = { bookingTargetProperty = null },
            onConfirmBooking = { guestName, guestPhone, checkIn, checkOut, guests, nights ->
                viewModel.bookShortStay(
                    property = property,
                    guestName = guestName,
                    guestPhone = guestPhone,
                    checkInDate = checkIn,
                    checkOutDate = checkOut,
                    guestsCount = guests,
                    nightsCount = nights
                )
                bookingTargetProperty = null
            }
        )
    }
}

@Composable
private fun ShortStayCard(
    stay: PropertyListing,
    onBookClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() }
            .testTag("short_stay_card_${stay.id}")
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = stay.imageDrawableRes ?: R.drawable.img_ashram_villa),
                    contentDescription = stay.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Locality badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = BrajSaffronGold, modifier = Modifier.size(12.dp))
                        Text(stay.locality, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // AirBnb Vrindavan Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrajEmeraldPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        "Vrindavan Only",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Info Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stay.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${stay.bhk} • ${stay.furnishing.displayName} • ${stay.propertyType.displayName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Temple proximity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🛕 Prem Mandir: ${stay.templeDistances.premMandirKm} km",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrajEmeraldPrimary
                    )
                    Text(
                        text = "🪷 Bankey Bihari: ${stay.templeDistances.bankeyBihariKm} km",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrajSaffronGold
                    )
                }

                HorizontalDivider()

                // Price and Book button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${indianFormat.format(stay.price)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = BrajEmeraldPrimary
                            )
                            Text(
                                text = " / night",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "+ ₹200 flat Brajvaas service fee",
                            fontSize = 10.sp,
                            color = BrajSaffronGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onBookClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("book_stay_${stay.id}")
                    ) {
                        Icon(Icons.Default.Luggage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reserve Stay", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingSummaryCard(booking: com.example.data.model.ShortStayBooking) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BrajVerifiedGreenContainer
                ) {
                    Text(
                        booking.status,
                        color = BrajVerifiedGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "ID: ${booking.id.takeLast(8)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = booking.propertyTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "Locality: ${booking.locality} • ${booking.numberOfGuests} Guests • ${booking.numberOfNights} Nights",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Check-In: ${booking.checkInDate}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Check-Out: ${booking.checkOutDate}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Stay Charge (${booking.numberOfNights} nights): ₹${indianFormat.format(booking.staySubtotal)}", fontSize = 11.sp)
                    Text("Brajvaas Tenant Fee: ₹200 (Flat)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                    Text("Brajvaas Landlord Fee: ₹200 (Paid by Host)", fontSize = 11.sp, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Paid by Guest:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("₹${indianFormat.format(booking.totalGuestPayable)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = BrajEmeraldPrimary)
                    }
                }
            }

            // Logged Location Confirmation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(14.dp))
                Text(
                    text = "Logged GPS: ${booking.guestExactCoordinates}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ShortStayBookingDialog(
    property: PropertyListing,
    currentUser: com.example.data.model.UserProfile?,
    onDismiss: () -> Unit,
    onConfirmBooking: (guestName: String, guestPhone: String, checkIn: String, checkOut: String, guests: Int, nights: Int) -> Unit
) {
    var guestName by remember { mutableStateOf(currentUser?.fullName ?: "Mukesh Garg") }
    var guestPhone by remember { mutableStateOf(currentUser?.phone ?: "+91 98765 43210") }
    var checkInDate by remember { mutableStateOf("18 Oct 2026") }
    var checkOutDate by remember { mutableStateOf("21 Oct 2026") }
    var numberOfGuests by remember { mutableIntStateOf(2) }
    var numberOfNights by remember { mutableIntStateOf(3) }

    val rate = property.price
    val subtotal = rate * numberOfNights
    val tenantFee = 200L
    val landlordFee = 200L
    val totalPayable = subtotal + tenantFee

    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Book Vrindavan Short Stay", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Brajvaas Flat ₹200 Fee Policy", fontSize = 11.sp, color = BrajEmeraldPrimary, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = property.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Guest Name (Aadhaar)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = guestPhone,
                    onValueChange = { guestPhone = it },
                    label = { Text("Mobile (+91)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = checkInDate,
                        onValueChange = { checkInDate = it },
                        label = { Text("Check-In") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = checkOutDate,
                        onValueChange = { checkOutDate = it },
                        label = { Text("Check-Out") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nights: $numberOfNights", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { if (numberOfNights > 1) numberOfNights-- }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null)
                        }
                        IconButton(onClick = { numberOfNights++ }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                        }
                    }
                }

                // Price Calculation Breakdown
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrajEmeraldContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("₹$rate × $numberOfNights nights", fontSize = 11.sp)
                            Text("₹${indianFormat.format(subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Brajvaas Tenant Fee (Flat)", fontSize = 11.sp, color = BrajEmeraldPrimary, fontWeight = FontWeight.Bold)
                            Text("₹200", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Landlord Fee (Flat, paid by host)", fontSize = 10.sp, color = Color.Gray)
                            Text("₹200", fontSize = 10.sp, color = Color.Gray)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Guest Payable:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("₹${indianFormat.format(totalPayable)}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = BrajEmeraldPrimary)
                        }
                    }
                }

                // Location log notice
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(12.dp))
                    Text(
                        text = "Booking location (27.5794° N, 77.6881° E) will be logged to admin.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmBooking(guestName, guestPhone, checkInDate, checkOutDate, numberOfGuests, numberOfNights)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm & Pay ₹$totalPayable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
