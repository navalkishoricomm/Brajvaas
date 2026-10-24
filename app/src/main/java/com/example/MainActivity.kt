package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.BrajvaasViewModel
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.RentReceiptDialog
import com.example.ui.screens.*
import com.example.ui.theme.BrajEmeraldPrimary
import com.example.ui.theme.BrajPendingOrange
import com.example.ui.theme.BrajSaffronGold
import com.example.ui.theme.BrajvaasTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BrajvaasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BrajvaasTheme {
                BrajvaasApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BrajvaasApp(viewModel: BrajvaasViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showFilterSheet by viewModel.showFilterSheet.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val selectedProperty by viewModel.selectedProperty.collectAsState()
    val activeReceipt by viewModel.activeReceiptRecord.collectAsState()
    val notification by viewModel.notification.collectAsState()
    val pendingListingsCount by viewModel.pendingListingsCount.collectAsState()
    val shortlistedIds by viewModel.shortlistedPropertyIds.collectAsState()
    val agreements by viewModel.agreements.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it.message)
            viewModel.clearNotification()
        }
    }

    // Screens that should show the bottom navigation bar
    val showBottomBar = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.USER_ACTIVITY,
        AppScreen.MANAGED_AGREEMENTS,
        AppScreen.MESSAGES,
        AppScreen.RENT_PAY,
        AppScreen.SUPPORT
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_explore")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.USER_ACTIVITY,
                        onClick = { viewModel.navigateTo(AppScreen.USER_ACTIVITY) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (shortlistedIds.isNotEmpty()) {
                                        Badge(containerColor = BrajSaffronGold) {
                                            Text("${shortlistedIds.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.USER_ACTIVITY) Icons.Filled.Bookmarks else Icons.Outlined.Bookmarks,
                                    contentDescription = "My Activity"
                                )
                            }
                        },
                        label = { Text("Activity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_activity")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.POST_PROPERTY,
                        onClick = { viewModel.navigateTo(AppScreen.POST_PROPERTY) },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "Post",
                                tint = BrajEmeraldPrimary
                            )
                        },
                        label = { Text("Post", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrajEmeraldPrimary) },
                        modifier = Modifier.testTag("nav_post")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.MANAGED_AGREEMENTS,
                        onClick = { viewModel.navigateTo(AppScreen.MANAGED_AGREEMENTS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (agreements.isNotEmpty()) {
                                        Badge(containerColor = BrajEmeraldPrimary) {
                                            Text("${agreements.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == AppScreen.MANAGED_AGREEMENTS) Icons.Filled.AssignmentTurnedIn else Icons.Outlined.Assignment,
                                    contentDescription = "Agreements"
                                )
                            }
                        },
                        label = { Text("Agreements", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_agreements")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.MESSAGES,
                        onClick = { viewModel.navigateTo(AppScreen.MESSAGES) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.MESSAGES) Icons.Filled.Chat else Icons.Outlined.Chat,
                                contentDescription = "Messages"
                            )
                        },
                        label = { Text("Messages", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("nav_messages")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                AppScreen.PROPERTY_DETAIL -> PropertyDetailScreen(
                    property = selectedProperty,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.POST_PROPERTY -> PostPropertyScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.ADMIN_PORTAL -> AdminPortalScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.MESSAGES -> MessagesScreen(viewModel = viewModel)
                AppScreen.CHAT_DETAIL -> ChatConversationScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.RENT_PAY -> RentPayScreen(viewModel = viewModel)
                AppScreen.SUPPORT -> SupportCenterScreen(viewModel = viewModel)
                AppScreen.AUTH -> AuthScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.USER_ACTIVITY -> UserActivityScreen(viewModel = viewModel)
                AppScreen.MANAGED_AGREEMENTS -> ManagedAgreementsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.SHORT_STAYS -> ShortStaysScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.WEB_ADMIN_PANEL -> WebAdminPortalScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                AppScreen.CONTACT_US -> ContactUsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = filter,
            onApplyFilter = { updated ->
                viewModel.updateFilter(updated)
            },
            onDismiss = {
                viewModel.toggleFilterSheet(false)
            }
        )
    }

    // Stamped Rent Receipt Dialog
    activeReceipt?.let { record ->
        RentReceiptDialog(
            record = record,
            onDismiss = { viewModel.dismissReceipt() },
            onDownloadOrShare = {
                viewModel.showNotification("Receipt ${record.receiptNumber} saved & ready to share!")
                viewModel.dismissReceipt()
            }
        )
    }
}
