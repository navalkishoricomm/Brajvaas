package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.BrajvaasViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: BrajvaasViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var isLoginTab by remember { mutableStateOf(true) }

    // Login inputs
    var loginIdentifier by remember { mutableStateOf("mukgarg11@gmail.com") }
    var loginPassword by remember { mutableStateOf("braj123") }
    var loginRole by remember { mutableStateOf(UserRole.SEEKER) }

    // Signup inputs
    var signupName by remember { mutableStateOf("") }
    var signupPhone by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupCity by remember { mutableStateOf("Vrindavan") }
    var signupRole by remember { mutableStateOf(UserRole.SEEKER) }
    var signupPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginTab) "Sign In to Brajvaas" else "Create Brajvaas Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("auth_back_btn")) {
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Logo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BrajEmeraldPrimary,
                    modifier = Modifier.size(64.dp),
                    shadowElevation = 3.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.brajvaas_logo_icon),
                        contentDescription = "Brajvaas Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = "Brajvaas Real Estate",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = BrajEmeraldPrimary
                )
                Text(
                    text = "Vrindavan & Mathura Property Services • 15 Days Brokerage",
                    fontSize = 12.sp,
                    color = BrajSaffronGold,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Tab Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isLoginTab,
                    onClick = { isLoginTab = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Sign In")
                }
                SegmentedButton(
                    selected = !isLoginTab,
                    onClick = { isLoginTab = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("New Registration")
                }
            }

            // Service Transparency Notice
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrajEmeraldContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrajEmeraldPrimary, modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Transparent 15-Day Brokerage Model",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BrajEmeraldPrimary
                        )
                        Text(
                            text = "We charge 15 days rent each from tenant and landlord upon successful agreement. E-stamped rent agreement & Mathura police verification are completely done by us.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isLoginTab) {
                // LOGIN FORM
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Sign In With Credentials", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text("Mobile Number or Email") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrajEmeraldPrimary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrajEmeraldPrimary) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Text("Select Profile Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = loginRole == UserRole.SEEKER,
                                onClick = { loginRole = UserRole.SEEKER },
                                label = { Text("Devotee / Tenant") }
                            )
                            FilterChip(
                                selected = loginRole == UserRole.LANDLORD,
                                onClick = { loginRole = UserRole.LANDLORD },
                                label = { Text("Landlord / Owner") }
                            )
                        }

                        Button(
                            onClick = {
                                if (loginIdentifier.isNotBlank()) {
                                    viewModel.login(loginIdentifier.trim(), loginRole)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_login_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                // Quick Demo Profiles for instant 1-Tap login
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Instant Demo Profiles (1-Tap)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.login("mukgarg11@gmail.com", UserRole.SEEKER)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mukesh (Tenant)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.login("govind.das@vrindavan.in", UserRole.LANDLORD)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Govind (Owner)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // SIGN UP FORM
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Register New User", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        OutlinedTextField(
                            value = signupName,
                            onValueChange = { signupName = it },
                            label = { Text("Full Name (as per Aadhaar)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = BrajEmeraldPrimary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = signupPhone,
                            onValueChange = { signupPhone = it },
                            label = { Text("Mobile Number (+91)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrajEmeraldPrimary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_phone_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = signupEmail,
                            onValueChange = { signupEmail = it },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrajEmeraldPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Text("I am a:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = signupRole == UserRole.SEEKER,
                                onClick = { signupRole = UserRole.SEEKER },
                                label = { Text("Tenant / Devotee") }
                            )
                            FilterChip(
                                selected = signupRole == UserRole.LANDLORD,
                                onClick = { signupRole = UserRole.LANDLORD },
                                label = { Text("Landlord / Owner") }
                            )
                        }

                        OutlinedTextField(
                            value = signupCity,
                            onValueChange = { signupCity = it },
                            label = { Text("Primary City (Vrindavan / Mathura)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = signupPassword,
                            onValueChange = { signupPassword = it },
                            label = { Text("Set Secure Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                val name = signupName.ifBlank { "Devotee User" }
                                val phone = signupPhone.ifBlank { "+91 98765 00000" }
                                val email = signupEmail.ifBlank { "user@brajvaas.in" }
                                viewModel.signUp(name, phone, email, signupRole, signupCity)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrajEmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_signup_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Complete Registration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
