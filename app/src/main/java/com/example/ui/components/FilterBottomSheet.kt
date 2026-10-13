package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ListingType
import com.example.data.model.PropertyType
import com.example.data.model.SearchFilter
import com.example.ui.theme.BrajEmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: SearchFilter,
    onApplyFilter: (SearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
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

    val bhkOptions = listOf("1 RK", "1 BHK", "2 BHK", "3 BHK", "4+ BHK", "Plot")

    var selectedListingType by remember { mutableStateOf(currentFilter.listingType) }
    var selectedLocality by remember { mutableStateOf(currentFilter.locality) }
    var selectedBhks by remember { mutableStateOf(currentFilter.bhkList.toMutableSet()) }
    var selectedTypes by remember { mutableStateOf(currentFilter.propertyTypes.toMutableSet()) }
    var onlyVerified by remember { mutableStateOf(currentFilter.onlyVerified) }
    var onlySattvik by remember { mutableStateOf(currentFilter.onlySattvik) }
    var maxPremMandirKm by remember { mutableStateOf(currentFilter.maxPremMandirDistanceKm.toFloat()) }
    var maxBankeyBihariKm by remember { mutableStateOf(currentFilter.maxBankeyBihariDistanceKm.toFloat()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = BrajEmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Advanced Search Filters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Rent or Buy Segments
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Looking To",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedListingType == ListingType.RENT,
                        onClick = { selectedListingType = ListingType.RENT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Rent Property")
                    }
                    SegmentedButton(
                        selected = selectedListingType == ListingType.SELL,
                        onClick = { selectedListingType = ListingType.SELL },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Buy / Sell")
                    }
                }
            }

            // Locality in Braj / Vrindavan / Mathura
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Vrindavan & Mathura Locality",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    localities.forEach { loc ->
                        FilterChip(
                            selected = selectedLocality == loc,
                            onClick = { selectedLocality = loc },
                            label = { Text(loc, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // BHK Configuration
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Configuration (BHK)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    bhkOptions.forEach { bhk ->
                        val isSelected = selectedBhks.contains(bhk)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedBhks.remove(bhk)
                                else selectedBhks.add(bhk)
                            },
                            label = { Text(bhk, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Property Type
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Property Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PropertyType.values().forEach { type ->
                        val isSelected = selectedTypes.contains(type)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedTypes.remove(type)
                                else selectedTypes.add(type)
                            },
                            label = { Text(type.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Proximity to Holy Shrines
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Temple Proximity Filters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrajEmeraldPrimary
                    )

                    // Prem Mandir distance
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Distance to Prem Mandir", fontSize = 12.sp)
                            Text(
                                if (maxPremMandirKm >= 15f) "Any distance" else "Within ${maxPremMandirKm.toInt()} km",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = maxPremMandirKm,
                            onValueChange = { maxPremMandirKm = it },
                            valueRange = 1f..15f,
                            steps = 13
                        )
                    }

                    // Bankey Bihari distance
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Distance to Bankey Bihari Mandir", fontSize = 12.sp)
                            Text(
                                if (maxBankeyBihariKm >= 15f) "Any distance" else "Within ${maxBankeyBihariKm.toInt()} km",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = maxBankeyBihariKm,
                            onValueChange = { maxBankeyBihariKm = it },
                            valueRange = 1f..15f,
                            steps = 13
                        )
                    }
                }
            }

            // Verification & Sattvik Toggles
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Brajvaas Verified Only", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Show properties vetted by Brajvaas Admin",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = onlyVerified,
                        onCheckedChange = { onlyVerified = it },
                        modifier = Modifier.testTag("verified_only_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strictly Pure Sattvik Society", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Mandatory vegetarian & devotee-friendly community",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = onlySattvik,
                        onCheckedChange = { onlySattvik = it },
                        modifier = Modifier.testTag("sattvik_only_switch")
                    )
                }
            }

            // Actions: Reset & Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedListingType = ListingType.RENT
                        selectedLocality = "All"
                        selectedBhks = mutableSetOf()
                        selectedTypes = mutableSetOf()
                        onlyVerified = false
                        onlySattvik = false
                        maxPremMandirKm = 20f
                        maxBankeyBihariKm = 20f
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        val updated = currentFilter.copy(
                            listingType = selectedListingType,
                            locality = selectedLocality,
                            bhkList = selectedBhks,
                            propertyTypes = selectedTypes,
                            onlyVerified = onlyVerified,
                            onlySattvik = onlySattvik,
                            maxPremMandirDistanceKm = if (maxPremMandirKm >= 15f) 50.0 else maxPremMandirKm.toDouble(),
                            maxBankeyBihariDistanceKm = if (maxBankeyBihariKm >= 15f) 50.0 else maxBankeyBihariKm.toDouble()
                        )
                        onApplyFilter(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_filter_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
