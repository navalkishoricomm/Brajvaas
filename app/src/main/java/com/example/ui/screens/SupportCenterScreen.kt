@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SupportCenterScreen(
    viewModel: BrajvaasViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val supportTickets by viewModel.supportTickets.collectAsState()
    val mitraMessages by viewModel.mitraChatMessages.collectAsState()

    var newTicketCategory by remember { mutableStateOf("MVDA Legal Verification") }
    var newTicketSubject by remember { mutableStateOf("") }
    var newTicketDesc by remember { mutableStateOf("") }

    var mitraInputText by remember { mutableStateOf("") }

    val categories = listOf(
        "MVDA Legal Verification",
        "Rent Receipt & Agreement",
        "Listing Approval Assistance",
        "Police Verification Help",
        "Ashram Rules Inquiry"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Brajvaas Sahayata Kendra", fontWeight = FontWeight.Black)
                        Text(
                            "Dedicated Vrindavan & Mathura Real Estate Support",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrajEmeraldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Regional FAQs", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Brajvaas Mitra", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Support Tickets", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Contact Us", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> FaqSection()
                1 -> MitraChatSection(
                    messages = mitraMessages,
                    inputText = mitraInputText,
                    onInputTextChange = { mitraInputText = it },
                    onSend = {
                        if (mitraInputText.isNotBlank()) {
                            viewModel.sendMitraMessage(mitraInputText.trim())
                            mitraInputText = ""
                        }
                    }
                )
                2 -> TicketsSection(
                    tickets = supportTickets,
                    category = newTicketCategory,
                    categories = categories,
                    onCategoryChange = { newTicketCategory = it },
                    subject = newTicketSubject,
                    onSubjectChange = { newTicketSubject = it },
                    description = newTicketDesc,
                    onDescriptionChange = { newTicketDesc = it },
                    onSubmit = {
                        if (newTicketSubject.isNotBlank() && newTicketDesc.isNotBlank()) {
                            viewModel.createSupportTicket(newTicketCategory, newTicketSubject.trim(), newTicketDesc.trim())
                            newTicketSubject = ""
                            newTicketDesc = ""
                        }
                    }
                )
                3 -> ContactUsContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun FaqSection() {
    val faqs = listOf(
        Pair(
            "What is MVDA approval and why is it crucial in Vrindavan?",
            "The Mathura-Vrindavan Development Authority (MVDA) regulates all municipal colony maps, residential layouts, and construction zones. Purchasing or renting in MVDA approved schemes (like Rukmini Vihar, Ganpati Infinity, Omaxe) protects devotees against unapproved colony demolitions and ensures authorized civic utilities."
        ),
        Pair(
            "What is the Brajvaas 15-Day Brokerage Model?",
            "Unlike traditional unorganized brokers who charge full months of rent and offer no legal support, Brajvaas charges a transparent 15 days rent as brokerage. In return, the entire 11-month legal e-stamped rental agreement and mandatory Mathura District police verification are prepared and executed by our dedicated team."
        ),
        Pair(
            "How does tenant police verification get done by Brajvaas in Mathura District?",
            "Under Senior Superintendent of Police (SSP) Mathura directives, tenant verification is mandatory. The Brajvaas verification cell collects KYC (Aadhaar & photos), completes physical on-ground verification, and processes clearance directly with the Mathura Police / UPCOP portal on your behalf."
        ),
        Pair(
            "What does 'Pure Sattvik Society' mean in Vrindavan?",
            "Vrindavan is a sacred holy town. Most societies around Raman Reti, Bhaktivedanta Marg, and Parikrama Marg enforce strict Sattvik guidelines: no non-vegetarian food, no alcohol, and quietude during evening Sandhya Aarti."
        ),
        Pair(
            "Are Brajvaas rent receipts valid for income tax HRA claims?",
            "Yes! Every rent receipt generated through Brajvaas Rent Pay includes landlord name, PAN/address details, receipt number, and digital transaction seal valid under Section 10(13A) of the Income Tax Act."
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrajEmeraldContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.HelpCenter, contentDescription = null, tint = BrajEmeraldPrimary)
                    Column {
                        Text("Mathura & Vrindavan Tenancy Guide", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrajEmeraldPrimary)
                        Text("Essential legal, administrative, and pilgrim guidelines for Braj residents.", fontSize = 11.sp)
                    }
                }
            }
        }

        items(faqs) { (q, a) ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = q,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = BrajEmeraldPrimary
                        )
                    }
                    if (expanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = a,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MitraChatSection(
    messages: List<com.example.data.model.ChatMessage>,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.isFromMe
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMe) BrajEmeraldPrimary else MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isMe) {
                                Text("Brajvaas Mitra", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrajSaffronGold)
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = msg.messageText,
                                fontSize = 13.sp,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Bottom input
        Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = { Text("Ask about MVDA, Police verification, rent receipts...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mitra_input"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 2
                )
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BrajEmeraldPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TicketsSection(
    tickets: List<com.example.data.model.SupportTicket>,
    category: String,
    categories: List<String>,
    onCategoryChange: (String) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Raise New Ticket Form
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
                    Text("Raise a Support Ticket", fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary)

                    Text("Query Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { onCategoryChange(cat) },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = onSubjectChange,
                        label = { Text("Subject / Issue Summary") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ticket_subject_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Detailed Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ticket_desc_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_ticket_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit Ticket", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Tickets History
        item {
            Text("Your Previous Tickets", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        items(tickets, key = { it.id }) { tkt ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tkt.id, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrajEmeraldPrimary)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (tkt.status == "Resolved") BrajVerifiedGreenContainer else BrajEmeraldContainer
                        ) {
                            Text(
                                text = tkt.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tkt.status == "Resolved") BrajVerifiedGreen else BrajEmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(tkt.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(tkt.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Category: ${tkt.category} • ${tkt.createdAtFormatted}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
