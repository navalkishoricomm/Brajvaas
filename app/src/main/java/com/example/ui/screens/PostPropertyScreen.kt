package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FurnishingStatus
import com.example.data.model.ListingType
import com.example.data.model.PropertyType
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.BrajEmeraldContainer
import com.example.ui.theme.BrajEmeraldPrimary
import com.example.ui.theme.BrajPendingOrange
import com.example.ui.theme.BrajSaffronGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostPropertyScreen(
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    // Form states
    var listingType by remember { mutableStateOf(ListingType.RENT) }
    var propertyType by remember { mutableStateOf(PropertyType.APARTMENT) }
    var title by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("Vrindavan") }
    var selectedLocality by remember { mutableStateOf("Raman Reti") }
    var fullAddress by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var depositText by remember { mutableStateOf("") }
    var maintenanceText by remember { mutableStateOf("") }
    var bhk by remember { mutableStateOf("2 BHK") }
    var builtUpAreaText by remember { mutableStateOf("1050") }
    var carpetAreaText by remember { mutableStateOf("880") }
    var floorText by remember { mutableStateOf("2") }
    var totalFloorsText by remember { mutableStateOf("4") }
    var furnishing by remember { mutableStateOf(FurnishingStatus.SEMI_FURNISHED) }
    var facing by remember { mutableStateOf("East / Temple Facing") }
    var preferredTenants by remember { mutableStateOf("Devotee Families / Pilgrims") }
    var isPureSattvik by remember { mutableStateOf(true) }

    // Proximities
    var premMandirKmText by remember { mutableStateOf("1.5") }
    var bankeyBihariKmText by remember { mutableStateOf("2.0") }
    var iskconKmText by remember { mutableStateOf("1.0") }
    var janmabhoomiKmText by remember { mutableStateOf("11.0") }
    var yamunaGhatKmText by remember { mutableStateOf("0.8") }

    // Legal status
    var isMvdaApproved by remember { mutableStateOf(true) }
    var isRegistryClear by remember { mutableStateOf(true) }

    // Owner info (Confidential)
    var ownerName by remember { mutableStateOf("Devotee Landlord") }
    var ownerPhone by remember { mutableStateOf("+91 98765 43210") }

    // Amenities
    val availableAmenities = listOf(
        "24/7 Power Backup",
        "Sweet RO Water",
        "Society Mandir",
        "Elevator (Lift)",
        "Covered Car Parking",
        "24/7 Guard & CCTV",
        "Satsang Hall",
        "Balcony",
        "Air Conditioning",
        "Geyser"
    )
    val selectedAmenities = remember { mutableStateListOf("24/7 Power Backup", "Sweet RO Water", "Society Mandir", "24/7 Guard & CCTV") }

    val localities = listOf(
        "Raman Reti",
        "Chhatikara Road",
        "Rukmini Vihar",
        "Bankey Bihari Zone",
        "Sunrakh Road",
        "Krishna Nagar",
        "NH-19 Mathura-Vrindavan",
        "Govardhan Road"
    )

    val cities = listOf("Vrindavan", "Mathura", "Govardhan")
    val bhkOptions = listOf("1 RK", "1 BHK", "2 BHK", "3 BHK", "4+ BHK", "Plot")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Property in Braj", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("post_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Admin Review Notice Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = BrajEmeraldContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrajEmeraldPrimary)
                    Column {
                        Text(
                            text = "Admin Approval Verification Mandatory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrajEmeraldPrimary
                        )
                        Text(
                            text = "To ensure authentic, zero-brokerage sacred listings in Vrindavan & Mathura, our Admin verifies all property details before making them public.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 1: Intent & Type
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1. Listing Purpose & Property Type", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = listingType == ListingType.RENT,
                            onClick = { listingType = ListingType.RENT },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("For Rent", fontSize = 12.sp)
                        }
                        SegmentedButton(
                            selected = listingType == ListingType.SELL,
                            onClick = { listingType = ListingType.SELL },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("For Sale", fontSize = 12.sp)
                        }
                        SegmentedButton(
                            selected = listingType == ListingType.SHORT_STAY,
                            onClick = {
                                listingType = ListingType.SHORT_STAY
                                selectedCity = "Vrindavan"
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text("Short Stay", fontSize = 12.sp)
                        }
                    }

                    Text("Property Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PropertyType.values().forEach { type ->
                            FilterChip(
                                selected = propertyType == type,
                                onClick = { propertyType = type },
                                label = { Text(type.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("BHK Configuration", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        bhkOptions.forEach { b ->
                            FilterChip(
                                selected = bhk == b,
                                onClick = { bhk = b },
                                label = { Text(b, fontSize = 12.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Listing Title (e.g. 2 BHK Flat near ISKCON Vrindavan)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_title_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Section 2: Location in Braj (Vrindavan / Mathura)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("2. Location in Vrindavan / Mathura", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)

                    // City selection
                    Text("City / Zone", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cities.forEach { c ->
                            FilterChip(
                                selected = selectedCity == c,
                                onClick = { selectedCity = c },
                                label = { Text(c) }
                            )
                        }
                    }

                    // Locality selection
                    Text("Locality / Sector", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        localities.forEach { loc ->
                            FilterChip(
                                selected = selectedLocality == loc,
                                onClick = { selectedLocality = loc },
                                label = { Text(loc, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = { fullAddress = it },
                        label = { Text("Exact Address / House No. (Kept Strictly Confidential)") },
                        supportingText = { Text("🔒 Confidential: Full address remains private and is never shown publicly to users.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Nearby Landmark (e.g. 400m from Prem Mandir Gate 3)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Section 3: Sacred Temple Distances
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TempleHindu, contentDescription = null, tint = BrajSaffronGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3. Temple Proximity (in Kilometers)", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = premMandirKmText,
                            onValueChange = { premMandirKmText = it },
                            label = { Text("Prem Mandir") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = bankeyBihariKmText,
                            onValueChange = { bankeyBihariKmText = it },
                            label = { Text("Bankey Bihari") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = iskconKmText,
                            onValueChange = { iskconKmText = it },
                            label = { Text("ISKCON Vrindavan") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = janmabhoomiKmText,
                            onValueChange = { janmabhoomiKmText = it },
                            label = { Text("Janmabhoomi (Mathura)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Section 4: Pricing & Area Dimensions
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (listingType) {
                        ListingType.SHORT_STAY -> {
                            Text("4. Stay Accommodation Details", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                            Surface(
                                color = BrajEmeraldContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Pricing and area calculations are not required for short stays. Daily pilgrim tariffs are managed during verification.",
                                        fontSize = 11.sp,
                                        color = BrajEmeraldPrimary
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = floorText,
                                    onValueChange = { floorText = it },
                                    label = { Text("Floor No.") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = totalFloorsText,
                                    onValueChange = { totalFloorsText = it },
                                    label = { Text("Total Floors") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                        ListingType.RENT -> {
                            Text("4. Area Dimensions (Pricing not required)", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                            Surface(
                                color = BrajEmeraldContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Pricing is not required for rented properties. Only area dimensions are required. We charge 15 days rent as brokerage.",
                                        fontSize = 11.sp,
                                        color = BrajEmeraldPrimary
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = builtUpAreaText,
                                    onValueChange = { builtUpAreaText = it },
                                    label = { Text("Built-up (sq.ft) *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = carpetAreaText,
                                    onValueChange = { carpetAreaText = it },
                                    label = { Text("Carpet Area (sq.ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = floorText,
                                    onValueChange = { floorText = it },
                                    label = { Text("Floor No.") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = totalFloorsText,
                                    onValueChange = { totalFloorsText = it },
                                    label = { Text("Total Floors") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                        ListingType.SELL -> {
                            Text("4. Pricing & Area Dimensions", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = { Text("Total Sale Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_price_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = builtUpAreaText,
                                    onValueChange = { builtUpAreaText = it },
                                    label = { Text("Built-up (sq.ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = carpetAreaText,
                                    onValueChange = { carpetAreaText = it },
                                    label = { Text("Carpet Area (sq.ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = floorText,
                                    onValueChange = { floorText = it },
                                    label = { Text("Floor No.") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = totalFloorsText,
                                    onValueChange = { totalFloorsText = it },
                                    label = { Text("Total Floors") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Furnishing & Community Guidelines
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("5. Furnishing & Community Standards", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)

                    Text("Furnishing Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FurnishingStatus.values().forEach { f ->
                            FilterChip(
                                selected = furnishing == f,
                                onClick = { furnishing = f },
                                label = { Text(f.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pure Sattvik Society / Ashram", fontWeight = FontWeight.Bold)
                            Text(
                                "Strictly vegetarian residents only (Recommended in Vrindavan)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPureSattvik,
                            onCheckedChange = { isPureSattvik = it }
                        )
                    }
                }
            }

            // Section 6: Amenities Checklist
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("6. Amenities Checklist", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableAmenities.forEach { amenity ->
                            val isChecked = selectedAmenities.contains(amenity)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    if (isChecked) selectedAmenities.remove(amenity)
                                    else selectedAmenities.add(amenity)
                                },
                                label = { Text(amenity, fontSize = 11.sp) },
                                leadingIcon = {
                                    if (isChecked) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                }
            }

            // Section 7: Legal Authority in Mathura-Vrindavan
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("7. Legal & MVDA Approvals", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MVDA Approved Map / RERA Registered", fontSize = 13.sp)
                        Switch(checked = isMvdaApproved, onCheckedChange = { isMvdaApproved = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clear Title / Freehold Registry", fontSize = 13.sp)
                        Switch(checked = isRegistryClear, onCheckedChange = { isRegistryClear = it })
                    }
                }
            }

            // Section 8: Confidential Poster Contact Details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = BrajEmeraldPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("8. Poster Contact (STRICTLY CONFIDENTIAL)", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                    }

                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Shielded by Brajvaas Policy: Your contact details are stored for admin verification and will NEVER be shown to seekers. Brajvaas charges 15 days rent upon deal closing, handling all agreement drafting and Mathura police verification.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Your Full Name (Owner / Trust)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        label = { Text("Your Phone Number (For Admin OTP/Call only)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "$bhk $selectedLocality $selectedCity" }
                    val finalPrice = when (listingType) {
                        ListingType.SHORT_STAY -> 0L
                        ListingType.RENT -> 0L
                        ListingType.SELL -> priceText.toLongOrNull() ?: 4500000L
                    }
                    val finalDeposit = if (listingType == ListingType.RENT) 0L else depositText.toLongOrNull() ?: (finalPrice * 2)
                    val finalMaint = if (listingType == ListingType.RENT || listingType == ListingType.SHORT_STAY) 0L else maintenanceText.toLongOrNull() ?: 1200L

                    val finalBuiltUp = if (listingType == ListingType.SHORT_STAY) 0 else (builtUpAreaText.toIntOrNull() ?: 1000)
                    val finalCarpet = if (listingType == ListingType.SHORT_STAY) 0 else (carpetAreaText.toIntOrNull() ?: 850)

                    val legalStatusStr = buildString {
                        if (isMvdaApproved) append("MVDA Approved • ")
                        if (isRegistryClear) append("Freehold Registry")
                    }.ifBlank { "Standard Brajvaas Lease" }

                    viewModel.submitNewProperty(
                        title = finalTitle,
                        listingType = listingType,
                        propertyType = propertyType,
                        locality = selectedLocality,
                        city = selectedCity,
                        fullAddress = fullAddress.ifBlank { "$selectedLocality, $selectedCity" },
                        landmark = landmark,
                        price = finalPrice,
                        deposit = finalDeposit,
                        maintenance = finalMaint,
                        bhk = bhk,
                        builtUpArea = finalBuiltUp,
                        carpetArea = finalCarpet,
                        floor = floorText.toIntOrNull() ?: 1,
                        totalFloors = totalFloorsText.toIntOrNull() ?: 4,
                        furnishing = furnishing,
                        facing = facing,
                        preferredTenants = preferredTenants,
                        isPureSattvik = isPureSattvik,
                        amenities = selectedAmenities.toList(),
                        premMandirKm = premMandirKmText.toDoubleOrNull() ?: 1.5,
                        bankeyBihariKm = bankeyBihariKmText.toDoubleOrNull() ?: 2.0,
                        iskconKm = iskconKmText.toDoubleOrNull() ?: 1.0,
                        janmabhoomiKm = janmabhoomiKmText.toDoubleOrNull() ?: 11.0,
                        yamunaGhatKm = yamunaGhatKmText.toDoubleOrNull() ?: 1.0,
                        legalStatus = legalStatusStr,
                        ownerName = ownerName,
                        ownerPhone = ownerPhone
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_property_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Property for Admin Review", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
