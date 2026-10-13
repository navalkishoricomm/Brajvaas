package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.data.model.RentPaymentRecord
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentPayScreen(
    viewModel: BrajvaasViewModel,
    modifier: Modifier = Modifier
) {
    val rentPayments by viewModel.rentPayments.collectAsState()
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    val currentDue = rentPayments.find { it.status == PaymentStatus.PENDING || it.status == PaymentStatus.OVERDUE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Brajvaas Rent Pay", fontWeight = FontWeight.Black)
                        Text(
                            "Rent Payment Tracking & Official Stamped Receipts",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary
                        )
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
            // Current Active Tenancy & Due Card
            if (currentDue != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("current_due_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrajEmeraldContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = BrajPendingOrange,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "RENT DUE: ${currentDue.monthYear.uppercase()}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = "Due By: ${currentDue.dueDate}",
                                    fontSize = 12.sp,
                                    color = BrajOnEmeraldContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "₹${indianFormat.format(currentDue.totalAmount)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrajEmeraldPrimary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = currentDue.propertyTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${currentDue.locality} • Landlord: ${currentDue.landlordName}",
                                    fontSize = 12.sp,
                                    color = BrajOnEmeraldContainer
                                )
                            }

                            HorizontalDivider(color = BrajEmeraldPrimary.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Base Rent: ₹${indianFormat.format(currentDue.baseRent)}", fontSize = 11.sp)
                                Text("Maint & Water: ₹${indianFormat.format(currentDue.maintenanceCharges)}", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.payRent(currentDue.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("pay_rent_now_btn")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pay Rent via UPI / Brajvaas Pay", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrajVerifiedGreenContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrajVerifiedGreen, modifier = Modifier.size(32.dp))
                            Column {
                                Text("All Rent Dues Cleared!", fontWeight = FontWeight.Bold, color = BrajVerifiedGreen)
                                Text("Your current month tenancy is in good standing.", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Features Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Why Pay via Brajvaas?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrajEmeraldPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = BrajSaffronGold, modifier = Modifier.size(16.dp))
                            Text("Instant digital rent receipts with Brajvaas seal for HRA claims", fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(16.dp))
                            Text("Direct landlord account transfer with zero platform fee", fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BrajSaffronGold, modifier = Modifier.size(16.dp))
                            Text("Automatic SMS & WhatsApp payment acknowledgment to landlord", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Payment History Header
            item {
                Text(
                    text = "Payment History & Rent Receipts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Payment records list
            items(rentPayments, key = { it.id }) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rent_record_${record.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(record.monthYear, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(record.propertyTitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (record.status == PaymentStatus.PAID) BrajVerifiedGreenContainer else BrajPendingOrangeContainer
                            ) {
                                Text(
                                    text = record.status.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.status == PaymentStatus.PAID) BrajVerifiedGreen else BrajPendingOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Amount Paid", fontSize = 10.sp, color = Color.Gray)
                                Text("₹${indianFormat.format(record.totalAmount)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }

                            if (record.status == PaymentStatus.PAID) {
                                Button(
                                    onClick = { viewModel.viewReceipt(record) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldContainer, contentColor = BrajEmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("view_receipt_${record.id}")
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.payRent(record.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Pay Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
