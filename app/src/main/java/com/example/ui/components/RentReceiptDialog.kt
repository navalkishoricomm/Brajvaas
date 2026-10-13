package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RentPaymentRecord
import com.example.ui.theme.BrajEmeraldPrimary
import com.example.ui.theme.BrajSaffronGold
import com.example.ui.theme.BrajVerifiedGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RentReceiptDialog(
    record: RentPaymentRecord,
    onDismiss: () -> Unit,
    onDownloadOrShare: () -> Unit
) {
    val indianFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("rent_receipt_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BRAJVAAS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = BrajEmeraldPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "OFFICIAL RENT RECEIPT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrajSaffronGold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Receipt Outer Box styled like formal paper receipt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Top Meta Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Receipt No.", fontSize = 10.sp, color = Color.Gray)
                                Text(record.receiptNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rental Month", fontSize = 10.sp, color = Color.Gray)
                                Text(record.monthYear, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        // Tenant & Landlord
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Received From (Tenant):", fontSize = 10.sp, color = Color.Gray)
                                Text(record.tenantName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Property Landlord:", fontSize = 10.sp, color = Color.Gray)
                                Text(record.landlordName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Property Details
                        Column {
                            Text("Premises Rented:", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "${record.propertyTitle}, ${record.locality}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        // Financial breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Base Rent:", fontSize = 11.sp, color = Color.DarkGray)
                                Text("₹${indianFormat.format(record.baseRent)}", fontSize = 11.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Society Maintenance & Sweet Water:", fontSize = 11.sp, color = Color.DarkGray)
                                Text("₹${indianFormat.format(record.maintenanceCharges)}", fontSize = 11.sp)
                            }
                            HorizontalDivider(color = Color(0xFFDDDDDD))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Paid:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "₹${indianFormat.format(record.totalAmount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrajEmeraldPrimary
                                )
                            }
                        }

                        // Payment Details
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = BrajVerifiedGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Payment Status: PAID & VERIFIED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrajVerifiedGreen
                                    )
                                    val txn = record.transactionId ?: "BV-TXN-OCT26-001"
                                    val dateStr = record.paidDate ?: "08 Oct 2026, 10:30 AM"
                                    Text(
                                        text = "Txn ID: $txn | $dateStr",
                                        fontSize = 9.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        // Tax note
                        Text(
                            text = "✓ Valid for HRA Tax Exemption under Sec 10(13A) of Income Tax Act 1961. Digitally certified by Brajvaas Realty Portal.",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDownloadOrShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDownloadOrShare,
                        colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_receipt_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
