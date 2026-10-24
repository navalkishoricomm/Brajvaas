@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*

@Composable
fun ContactUsScreen(
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Contact Us", fontWeight = FontWeight.Black)
                        Text(
                            "Brajvaas Hospitalities • Sri Vrindavan Dham",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("contact_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        ContactUsContent(
            viewModel = viewModel,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun ContactUsContent(
    viewModel: BrajvaasViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var devoteeName by remember { mutableStateOf("") }
    var devoteePhone by remember { mutableStateOf("") }
    var inquiryNote by remember { mutableStateOf("") }
    var submittedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Office Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contact_office_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrajEmeraldPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛕", fontSize = 24.sp)
                        }
                        Column {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = BrajSaffronGold
                            ) {
                                Text(
                                    text = "OFFICIAL HEADQUARTERS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF073B2E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Brajvaas Hospitalities",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    // Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Address",
                            tint = BrajSaffronGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "OFFICE ADDRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrajSaffronGold
                            )
                            Text(
                                "Keshi 1, Keshi Ghat,\nShri Dham Vrindavan, Mathura 281121",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Phone Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = BrajSaffronGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "DIRECT HELPLINE & WHATSAPP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrajSaffronGold
                            )
                            Text(
                                "Ph 9810610554",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // Office Hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏰", fontSize = 14.sp)
                        Text(
                            "Office & Coordinator Timings: 8:00 AM – 8:30 PM (All 7 Days)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9810610554"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajSaffronGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_call_office")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF073B2E), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Call Now", fontWeight = FontWeight.Bold, color = Color(0xFF073B2E))
                        }

                        Button(
                            onClick = {
                                val url = "https://wa.me/919810610554?text=Hare%20Krishna!%20I%20am%20inquiring%20about%20Brajvaas%20Hospitalities%20services."
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_whatsapp_office")
                        ) {
                            Text("💬 WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("geo:0,0?q=Keshi+Ghat,+Vrindavan,+Mathura+281121")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_maps_office")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Locate on Google Maps (Keshi Ghat)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Assistance Highlights
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Our Services at Keshi Ghat Office",
                        fontWeight = FontWeight.Bold,
                        color = BrajEmeraldPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    ContactServiceItem(
                        icon = "🛏️",
                        title = "Sacred Short Stays & Homestays",
                        desc = "Verified pilgrim accommodation with pure Sattvik atmosphere near holy temples."
                    )
                    ContactServiceItem(
                        icon = "📜",
                        title = "11-Month Rental Agreements",
                        desc = "15-day brokerage policy with legal e-stamping and tenant police verification."
                    )
                    ContactServiceItem(
                        icon = "🏛️",
                        title = "MVDA & Revenue Record Checks",
                        desc = "Thorough scrutiny of layout approvals and Mathura registry compliance."
                    )
                }
            }
        }

        // Quick Devotee Callback Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Request a Callback",
                        fontWeight = FontWeight.Bold,
                        color = BrajEmeraldPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Leave your contact details and our Vrindavan coordinator will call you back within 15 minutes.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = devoteeName,
                        onValueChange = { devoteeName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_input_name"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = devoteePhone,
                        onValueChange = { devoteePhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_input_phone"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = inquiryNote,
                        onValueChange = { inquiryNote = it },
                        label = { Text("Requirement / Message") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_input_note"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    if (submittedMessage) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrajVerifiedGreenContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "✓ Hare Krishna! Your message has been sent to our Keshi Ghat office coordinator. We will call you shortly.",
                                color = BrajVerifiedGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (devoteeName.isNotBlank() && devoteePhone.isNotBlank()) {
                                viewModel.createSupportTicket(
                                    category = "Office Visit & Inquiries",
                                    subject = "Devotee Callback Request from $devoteeName",
                                    desc = "Phone: $devoteePhone. Message: $inquiryNote"
                                )
                                submittedMessage = true
                                devoteeName = ""
                                devoteePhone = ""
                                inquiryNote = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_btn_submit"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Request to Brajvaas Hospitalities", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactServiceItem(
    icon: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 20.sp)
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
