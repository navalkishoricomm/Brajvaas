package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.AgreementStepStatus
import com.example.data.model.PoliceVerificationStatus
import com.example.data.model.RentalAgreementTracker
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedAgreementsScreen(
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val agreements by viewModel.agreements.collectAsState()
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    var viewAgreementDialogRecord by remember { mutableStateOf<RentalAgreementTracker?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Agreements & Police Verification", fontWeight = FontWeight.Black)
                        Text(
                            "Done by Brajvaas • 15 Days Brokerage",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("agreements_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brokerage Model Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrajEmeraldContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Balance, contentDescription = null, tint = BrajEmeraldPrimary)
                            Text(
                                text = "Brajvaas Transparent Brokerage Policy",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = BrajEmeraldPrimary
                            )
                        }

                        Text(
                            text = "We charge 15 days rent as brokerage upon successful deal finalization. In return, all legal and administrative work is handled by us:",
                            fontSize = 12.sp,
                            color = BrajOnEmeraldContainer
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(16.dp))
                            Text("11-Month Legal E-Stamped Rent Agreement prepared & notarized by us", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(16.dp))
                            Text("Mathura District Police Tenant Verification filed & cleared by us", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(16.dp))
                            Text("Physical visit accompaniment & inventory handover by Brajvaas Mitra", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Active Rent Agreements (${agreements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(agreements, key = { it.id }) { agreement ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(agreement.agreementNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrajEmeraldPrimary)
                                Text("Initiated: ${agreement.initiatedDate}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrajSaffronGoldContainer
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Brokerage (15 Days)", fontSize = 9.sp, color = BrajOnSaffronContainer, fontWeight = FontWeight.Bold)
                                    Text("₹${indianFormat.format(agreement.brokerage15DaysFee)}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BrajOnSaffronContainer)
                                }
                            }
                        }

                        // Property Details
                        Text(
                            text = agreement.propertyTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${agreement.locality} • Rent: ₹${indianFormat.format(agreement.monthlyRent)}/mo",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // STEP 1: Rent Agreement
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Article, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Text("1. Rent Agreement (Done by Us)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (agreement.agreementStatus == AgreementStepStatus.COMPLETED) BrajVerifiedGreenContainer else BrajEmeraldContainer
                                ) {
                                    Text(
                                        text = agreement.agreementStatus.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (agreement.agreementStatus == AgreementStepStatus.COMPLETED) BrajVerifiedGreen else BrajEmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Drafted with standard Braj 11-month pilgrimage/devotee terms with ₹100 e-stamp.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // STEP 2: Police Verification
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.LocalPolice, contentDescription = null, tint = BrajSaffronGold, modifier = Modifier.size(16.dp))
                                    Text("2. Mathura Police Verification (Done by Us)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (agreement.policeVerificationStatus == PoliceVerificationStatus.VERIFIED_CLEARED) BrajVerifiedGreenContainer else BrajEmeraldContainer
                                ) {
                                    Text(
                                        text = agreement.policeVerificationStatus.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (agreement.policeVerificationStatus == PoliceVerificationStatus.VERIFIED_CLEARED) BrajVerifiedGreen else BrajEmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Ack No: ${agreement.policeAckNumber} filed under Mathura Police directives.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Actions: View details & Simulate steps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewAgreementDialogRecord = agreement },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("View Document", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.advanceAgreementStep(agreement.id)
                                    viewModel.advancePoliceVerification(agreement.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Advance Clearance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Agreement Details View Dialog
    viewAgreementDialogRecord?.let { agr ->
        AlertDialog(
            onDismissRequest = { viewAgreementDialogRecord = null },
            title = { Text("Brajvaas Rental Agreement", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Agreement ID: ${agr.agreementNumber}", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary, fontSize = 13.sp)
                    Text("Tenant: ${agr.tenantName}")
                    Text("Landlord: ${agr.landlordName}")
                    Text("Premises: ${agr.propertyTitle}, ${agr.locality}")
                    Text("Monthly Rent: ₹${indianFormat.format(agr.monthlyRent)}")
                    Text("Brajvaas Brokerage (15 Days): ₹${indianFormat.format(agr.brokerage15DaysFee)}")
                    HorizontalDivider()
                    Text("Status: ${agr.agreementStatus.label}", fontWeight = FontWeight.Bold)
                    Text("Police Clearance: ${agr.policeVerificationStatus.label}", fontWeight = FontWeight.Bold, color = BrajVerifiedGreen)
                    Text("Police Ack Number: ${agr.policeAckNumber}", fontSize = 11.sp, color = Color.DarkGray)
                }
            },
            confirmButton = {
                Button(onClick = { viewAgreementDialogRecord = null }) {
                    Text("Close")
                }
            }
        )
    }
}
