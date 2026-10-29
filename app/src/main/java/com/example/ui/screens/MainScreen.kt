package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.*
import com.example.data.repository.StudentWithDetails
import com.example.ui.viewmodel.EduViewModel
import java.text.SimpleDateFormat
import java.util.*

sealed class ScreenTab(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : ScreenTab("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Students : ScreenTab("students", "Students", Icons.Default.People)
    object Classes : ScreenTab("classes", "Classes & Sections", Icons.Default.Class)
    object Attendance : ScreenTab("attendance", "Attendance", Icons.Default.CheckCircle)
    object Fees : ScreenTab("fees", "Fee Ledger", Icons.Default.AccountBalanceWallet)
    object Reports : ScreenTab("reports", "Report Center", Icons.Default.Assessment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: EduViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (!authState.isLoggedIn) {
        LoginScreen(
            errorMsg = authState.loginError,
            onLogin = { username, password -> viewModel.login(username, password) },
            onClearError = { viewModel.clearLoginError() }
        )
    } else {
        val user = authState.currentUser!!
        var currentTab by remember { mutableStateOf<ScreenTab>(ScreenTab.Dashboard) }

        // Determine if screen is wide (Adaptive / Responsive design)
        // Since we don't have Window Size Classes library imported explicitly, 
        // we can dynamically check available size or configuration.
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isWideScreen = configuration.screenWidthDp >= 600

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "EduManage System",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "School Workspace • ${user.fullName} (${user.role})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ),
                    actions = {
                        // User Profile Picture
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_avatar),
                            contentDescription = "User Profile Picture",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { 
                                viewModel.logout()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Log Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    val tabs = listOf(
                        ScreenTab.Dashboard,
                        ScreenTab.Students,
                        ScreenTab.Attendance,
                        ScreenTab.Fees,
                        ScreenTab.Reports
                    )
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Left Navigation Rail for Tablet / Wide screens
                if (isWideScreen) {
                    val tabs = listOf(
                        ScreenTab.Dashboard,
                        ScreenTab.Students,
                        ScreenTab.Classes,
                        ScreenTab.Attendance,
                        ScreenTab.Fees,
                        ScreenTab.Reports
                    )
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(120.dp)
                            .testTag("wide_nav_rail"),
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        tabs.forEach { tab ->
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title, textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Main screen view content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "tabs_navigation"
                    ) { targetTab ->
                        when (targetTab) {
                            ScreenTab.Dashboard -> DashboardView(viewModel, user) { currentTab = ScreenTab.Students }
                            ScreenTab.Students -> StudentsDirectoryView(viewModel, user)
                            ScreenTab.Classes -> ClassesAndGroupsView(viewModel, user)
                            ScreenTab.Attendance -> AttendanceManagementView(viewModel, user)
                            ScreenTab.Fees -> FeeManagementView(viewModel, user)
                            ScreenTab.Reports -> ReportGeneratorView(viewModel, user)
                        }
                    }
                }
            }
        }
    }
}

// 1. LOGIN SCREEN
@Composable
fun LoginScreen(
    errorMsg: String?,
    onLogin: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 450.dp)
                .testTag("login_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF1E3C72),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "EDU-MANAGE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3C72)
                )
                Text(
                    text = "Portal Access",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        onClearError()
                    },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        onClearError()
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onLogin(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3C72)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Authenticate Securely", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💡 QUICK ACCESS DEMO PORTAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3C72)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Username: 3210383002631 • Password: Amir",
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
// 2. DASHBOARD VIEW (Bento Grid Theme)
@Composable
fun DashboardView(
    viewModel: EduViewModel,
    user: UserEntity,
    onNavigateToStudents: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bento Style Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = user.role.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49454F),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "EduTrack Pro",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D1B20)
                )
            }
            // Circle avatar profile picture
            Image(
                painter = painterResource(id = R.drawable.img_profile_avatar),
                contentDescription = "User Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // 1. Primary Bento Card: Total Students (Full span)
        Card(
            onClick = onNavigateToStudents,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .testTag("dashboard_total_students_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD0BCFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Background decorative icon with low alpha
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF21005D).copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.BottomEnd)
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TOTAL STUDENTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D).copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = stats.totalStudents.toString(),
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF21005D)
                        )
                    }

                    // Pill Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF21005D), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE ENROLLMENT DIRECTORY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Row of 2 Secondary Bento Cards (Attendance and Fees)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 2: Attendance
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(135.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ATTENDANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D192B).copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )

                    Column {
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", stats.attendancePercentage)}%",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1D192B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Linear progress bar mimicking style in HTML
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFFCAC4D0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = ((stats.attendancePercentage / 100.0).coerceIn(0.0, 1.0)).toFloat())
                                    .background(Color(0xFF6750A4))
                            )
                        }
                    }
                }
            }

            // Card 3: Fee Collection Balance Tracker
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(135.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FEE COLLECTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F).copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "PAID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = stats.paidCount.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1D1B20)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "UNPAID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = stats.unpaidCount.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1D1B20)
                            )
                        }
                    }
                }
            }
        }

        // Card 4: Class breakdowns (White, border, beautiful round shapes)
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFE7E0EC)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Admissions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    Text(
                        text = "CLASS DISTRIBUTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (stats.classWiseCount.isEmpty()) {
                    Text(
                        text = "No enrollment analytics configured yet. Create a class and assign students to build insights.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    stats.classWiseCount.forEach { (className, count) ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF7F2FA)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = className.take(2).uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6750A4)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = className,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1D1B20)
                                        )
                                        Text(
                                            text = "Active Cohort Division",
                                            fontSize = 10.sp,
                                            color = Color(0xFF49454F)
                                        )
                                    }
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text("$count Students", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                                )
                            }
                            Divider(color = Color(0xFFE7E0EC).copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Action Bento buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToStudents,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "New Student",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFD8E4))
                    .clickable { onNavigateToStudents() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Directory",
                    tint = Color(0xFF31111D),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    color: Color,
    onColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = onColor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                Icon(imageVector = icon, contentDescription = null, tint = onColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = onColor)
                Text(text = subText, fontSize = 10.sp, color = onColor.copy(alpha = 0.7f))
            }
        }
    }
}

// 3. STUDENTS DIRECTORY VIEW
@Composable
fun StudentsDirectoryView(
    viewModel: EduViewModel,
    user: UserEntity
) {
    val context = LocalContext.current
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val classFilter by viewModel.selectedClassFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStudentDetails by remember { mutableStateOf<StudentWithDetails?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "👩‍🎓 STUDENTS WORKSPACE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Search, filter academic divisions, and review total files of enrolled students below.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search & Filter Operations Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search by: Name, Class, Roll...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("student_search_input"),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            // Add Student FAB - Role-restricted (Only Admin can add student records)
            if (user.role == "Admin") {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("add_student_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Student")
                }
            }
        }

        // Class quick filters chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputChip(
                selected = classFilter == null,
                onClick = { viewModel.updateClassFilter(null) },
                label = { Text("All Divisions") }
            )
            classes.forEach { classEntity ->
                InputChip(
                    selected = classFilter == classEntity.id,
                    onClick = { viewModel.updateClassFilter(classEntity.id) },
                    label = { Text(classEntity.className) }
                )
            }
        }

        // Student Directory Listings
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No students matching this search criteria.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents) { item ->
                    StudentItemCard(
                        item = item,
                        onClick = { selectedStudentDetails = item }
                    )
                }
            }
        }
    }

    // STUDENT DETAIL OVERLAY DIALOG SHEET
    if (selectedStudentDetails != null) {
        val details = selectedStudentDetails!!
        val attendanceSummary = viewModel.getStudentAttendanceSummary(details.student.id)
        val fees by viewModel.fees.collectAsStateWithLifecycle()
        val studentFeeHistory = remember(fees, details.student.id) { fees.filter { it.studentId == details.student.id } }

        var showEditDialog by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { selectedStudentDetails = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(8.dp)
                    .testTag("student_details_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Student Record Details",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedStudentDetails = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close details")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Student Summary Profile Avatar Placeholder
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = details.student.name.take(1).capitalize(Locale.ROOT),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = details.student.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "ID: #${details.student.id} | ${details.className} - ${details.groupName}",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("📝 DEMOGRAPHICS & CONTACT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    StudentDetailRow("Father's Name:", details.student.fatherName)
                    StudentDetailRow("Age:", "${details.student.age} Years")
                    StudentDetailRow("Date of Birth:", details.student.dob)
                    StudentDetailRow("Phone Contact:", details.student.contactNumber)
                    StudentDetailRow("Address:", details.student.address)
                    StudentDetailRow("Admission Date:", details.student.admissionDate)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("📊 ATTENDANCE HISTORY STATS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttendanceProgressStat("Marked Days", attendanceSummary.total.toString())
                            AttendanceProgressStat("Present Days", attendanceSummary.present.toString())
                            AttendanceProgressStat("Absent Days", attendanceSummary.absent.toString())
                            AttendanceProgressStat("Score", "${String.format(Locale.getDefault(), "%.1f", attendanceSummary.percentage)}%")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("💰 FINANCIAL FEE HISTORY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (studentFeeHistory.isEmpty()) {
                        Text("No fee history recorded.")
                    } else {
                        studentFeeHistory.forEach { fee ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = fee.monthName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Amount: $${fee.amount}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SuggestionChip(
                                        onClick = { 
                                            // Toggle payment status (Admin permission based)
                                            if (user.role == "Admin") {
                                                viewModel.recordFeePayment(fee.id, !fee.isPaid)
                                            } else {
                                                Toast.makeText(context, "Only administrators can perform financial logs", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        label = { 
                                            Text(
                                                text = if (fee.isPaid) "Paid" else "Unpaid", 
                                                color = if (fee.isPaid) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                fontWeight = FontWeight.Bold
                                            ) 
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Admin restricted delete/edit operation
                    if (user.role == "Admin") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.weight(1f).testTag("edit_student_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Student")
                            }
                            Button(
                                onClick = { 
                                    viewModel.deleteStudent(details.student)
                                    selectedStudentDetails = null
                                    Toast.makeText(context, "Student record successfully deleted", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("delete_student_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

        // EDIT DETAILS COMPOSABLE POPUP
        if (showEditDialog) {
            AddOrEditStudentDialog(
                editingStudent = details.student,
                classes = classes,
                groups = groups,
                onDismiss = { showEditDialog = false },
                onSave = { updatedStudent, _, _ ->
                    viewModel.updateStudent(updatedStudent)
                    selectedStudentDetails = selectedStudentDetails?.copy(student = updatedStudent)
                    showEditDialog = false
                    Toast.makeText(context, "Details successfully updated", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // NEW ENROLLMENT (ADD STUDENT) DIALOG POPUP
    if (showAddDialog) {
        AddOrEditStudentDialog(
            editingStudent = null,
            classes = classes,
            groups = groups,
            onDismiss = { showAddDialog = false },
            onSave = { newStudent, initialFee, createMonth ->
                viewModel.addStudent(newStudent, initialFee, createMonth)
                showAddDialog = false
                Toast.makeText(context, "Student successfully registered", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AttendanceProgressStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun StudentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

@Composable
fun StudentItemCard(
    item: StudentWithDetails,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("student_item_card_${item.student.id}"),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.student.name.take(1).capitalize(Locale.ROOT),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.student.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "ID: #${item.student.id} • ${item.className} (${item.groupName})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

// 4. ADD OR EDIT STUDENT FORM COMPOSABLE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditStudentDialog(
    editingStudent: StudentEntity?,
    classes: List<ClassEntity>,
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onSave: (StudentEntity, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(editingStudent?.name ?: "") }
    var fatherName by remember { mutableStateOf(editingStudent?.fatherName ?: "") }
    var ageStr by remember { mutableStateOf(editingStudent?.age?.toString() ?: "") }
    var dob by remember { mutableStateOf(editingStudent?.dob ?: "2010-01-01") }
    var contactNumber by remember { mutableStateOf(editingStudent?.contactNumber ?: "") }
    var address by remember { mutableStateOf(editingStudent?.address ?: "") }
    var admissionDate by remember { mutableStateOf(editingStudent?.admissionDate ?: "2026-06-20") }

    // Dropdowns selection indexes
    var selectedClassIndex by remember { 
        mutableStateOf(
            if (editingStudent != null) classes.indexOfFirst { it.id == editingStudent.classId } else 0
        ) 
    }
    var selectedGroupIndex by remember { 
        mutableStateOf(
            if (editingStudent != null) groups.indexOfFirst { it.id == editingStudent.groupId } else 0
        ) 
    }

    var dropdownClassExpanded by remember { mutableStateOf(false) }
    var dropdownGroupExpanded by remember { mutableStateOf(false) }

    // Initial fields for fees when registering a student
    var initialMonthlyFeeStr by remember { mutableStateOf("150.0") }
    var initialFeeMonth by remember { mutableStateOf("June 2026") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(8.dp)
                .testTag("student_form_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (editingStudent == null) "New Student Enrollment" else "Modify Student Record",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father's Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = { Text("Age (Years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("DOB (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Contact Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = admissionDate,
                    onValueChange = { admissionDate = it },
                    label = { Text("Admission Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Classes Dropdown
                Text("Select Academic Class Division", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownClassExpanded,
                    onExpandedChange = { dropdownClassExpanded = it }
                ) {
                    val labelText = if (classes.isNotEmpty() && selectedClassIndex in classes.indices) {
                        classes[selectedClassIndex].className
                    } else "No Classes Configured"
                    
                    OutlinedTextField(
                        value = labelText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownClassExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownClassExpanded,
                        onDismissRequest = { dropdownClassExpanded = false }
                    ) {
                        if (classes.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("(No Classes recorded!)") },
                                onClick = { dropdownClassExpanded = false }
                            )
                        } else {
                            classes.forEachIndexed { idx, classEntity ->
                                DropdownMenuItem(
                                    text = { Text(classEntity.className) },
                                    onClick = {
                                        selectedClassIndex = idx
                                        dropdownClassExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Groups Dropdown
                Text("Select Group/Section division", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownGroupExpanded,
                    onExpandedChange = { dropdownGroupExpanded = it }
                ) {
                    val labelText = if (groups.isNotEmpty() && selectedGroupIndex in groups.indices) {
                        groups[selectedGroupIndex].groupName
                    } else "No Sections Configured"

                    OutlinedTextField(
                        value = labelText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownGroupExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownGroupExpanded,
                        onDismissRequest = { dropdownGroupExpanded = false }
                    ) {
                        if (groups.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("(No Groups/Sections recorded!)") },
                                onClick = { dropdownGroupExpanded = false }
                            )
                        } else {
                            groups.forEachIndexed { idx, groupEntity ->
                                DropdownMenuItem(
                                    text = { Text(groupEntity.groupName) },
                                    onClick = {
                                        selectedGroupIndex = idx
                                        dropdownGroupExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (editingStudent == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("💰 FINANCIAL ENROLLMENT LEDGER", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = initialMonthlyFeeStr,
                            onValueChange = { initialMonthlyFeeStr = it },
                            label = { Text("Admission Roll Fee") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = initialFeeMonth,
                            onValueChange = { initialFeeMonth = it },
                            label = { Text("Invoice Billing Period") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) return@Button

                            val classSelectedId = if (classes.isNotEmpty() && selectedClassIndex in classes.indices) {
                                classes[selectedClassIndex].id
                            } else 1

                            val groupSelectedId = if (groups.isNotEmpty() && selectedGroupIndex in groups.indices) {
                                groups[selectedGroupIndex].id
                            } else 1

                            val std = StudentEntity(
                                id = editingStudent?.id ?: 0,
                                name = name.trim(),
                                fatherName = fatherName.trim(),
                                age = ageStr.toIntOrNull() ?: 15,
                                dob = dob.trim(),
                                contactNumber = contactNumber.trim(),
                                address = address.trim(),
                                classId = classSelectedId,
                                groupId = groupSelectedId,
                                admissionDate = admissionDate.trim()
                            )
                            onSave(std, initialMonthlyFeeStr.toDoubleOrNull() ?: 150.0, initialFeeMonth.trim())
                        },
                        modifier = Modifier.weight(1f).testTag("save_student_button")
                    ) {
                        Text("Record File")
                    }
                }
            }
        }
    }
}

// 5. CLASSES AND GROUPS MANAGEMENT VIEW
@Composable
fun ClassesAndGroupsView(
    viewModel: EduViewModel,
    user: UserEntity
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var newClassName by remember { mutableStateOf("") }
    var newGroupName by remember { mutableStateOf("") }
    var selectedClassForGroupIndex by remember { mutableStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "🏫 DIVISIONS & GROUPS ENGINE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Configure custom Grade Classes and Section/Groups division mappings.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (user.role == "Admin") {
            // Section 1: Add Classes (Admin Only)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "➕ Create Grade Class Division", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newClassName,
                            onValueChange = { newClassName = it },
                            placeholder = { Text("e.g. Grade 10-Science") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("new_class_input")
                        )
                        Button(
                            onClick = {
                                if (newClassName.isNotBlank()) {
                                    viewModel.addClass(newClassName.trim())
                                    newClassName = ""
                                    Toast.makeText(context, "Class successfully added!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.height(56.dp).testTag("add_class_submit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }

            // Section 2: Add Sections (Admin Only)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "➕ Create Section / Sub-Group division", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Linked Grade Division:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart).padding(vertical = 4.dp)) {
                        val activeClassText = if (classes.isNotEmpty() && selectedClassForGroupIndex in classes.indices) {
                            classes[selectedClassForGroupIndex].className
                        } else "Create classes first!"

                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = activeClassText)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            classes.forEachIndexed { index, classEntity ->
                                DropdownMenuItem(
                                    text = { Text(classEntity.className) },
                                    onClick = {
                                        selectedClassForGroupIndex = index
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newGroupName,
                            onValueChange = { newGroupName = it },
                            placeholder = { Text("e.g. Group A / Arts") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("new_group_input")
                        )
                        Button(
                            onClick = {
                                if (classes.isEmpty()) {
                                    Toast.makeText(context, "Please create class divisions first!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newGroupName.isNotBlank()) {
                                    val linkedClassId = classes[selectedClassForGroupIndex].id
                                    viewModel.addGroup(newGroupName.trim(), linkedClassId)
                                    newGroupName = ""
                                    Toast.makeText(context, "Group successfully added!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.height(56.dp).testTag("add_group_submit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }

        // Section 3: Lists Display
        Text(text = "📂 Configured Classes Divisions List", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        if (classes.isEmpty()) {
            Text("No active class configurations.")
        } else {
            classes.forEach { classEntity ->
                val linkedGroups = groups.filter { it.classId == classEntity.id }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = classEntity.className, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            if (user.role == "Admin") {
                                IconButton(
                                    onClick = { 
                                        viewModel.deleteClass(classEntity) 
                                        Toast.makeText(context, "Class division deleted", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Class", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Sections under this Grade Class Division:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        
                        if (linkedGroups.isEmpty()) {
                            Text(text = "No linked subgroups recorded.", fontSize = 12.sp, color = Color.LightGray)
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                linkedGroups.forEach { grp ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = grp.groupName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            if (user.role == "Admin") {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Delete section",
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable { 
                                                            viewModel.deleteGroup(grp)
                                                        },
                                                    tint = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. ATTENDANCE MANAGEMENT VIEW
@Composable
fun AttendanceManagementView(
    viewModel: EduViewModel,
    user: UserEntity
) {
    val students by viewModel.studentsWithDetails.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendance.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Date for marking attendance (Defaults to today in 2026-06-20 format)
    var selectedDateStr by remember { mutableStateOf("2026-06-20") }
    var selectedClassIndex by remember { mutableStateOf(0) }
    var dropdownClassExpanded by remember { mutableStateOf(false) }

    val activeClass = if (classes.isNotEmpty() && selectedClassIndex in classes.indices) classes[selectedClassIndex] else null
    val targetStudents = if (activeClass != null) {
        students.filter { it.student.classId == activeClass.id }
    } else {
        students
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📋 DAILY ATTENDANCE LEDGER",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Date selection and Class Division Selection header card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedDateStr,
                        onValueChange = { selectedDateStr = it },
                        label = { Text("Log Date (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("date_input"),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )

                    // Class selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Class Grade division:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Box {
                            val btnText = activeClass?.className ?: "All classes"
                            OutlinedButton(
                                onClick = { dropdownClassExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = btnText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = dropdownClassExpanded,
                                onDismissRequest = { dropdownClassExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Show All Classes") },
                                    onClick = {
                                        selectedClassIndex = -1
                                        dropdownClassExpanded = false
                                    }
                                )
                                classes.forEachIndexed { index, classEntity ->
                                    DropdownMenuItem(
                                        text = { Text(classEntity.className) },
                                        onClick = {
                                            selectedClassIndex = index
                                            dropdownClassExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Active Students Registry (${targetStudents.size} Records)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (targetStudents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No registered students found under this division.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(targetStudents) { studentDetails ->
                    val studentId = studentDetails.student.id
                    val statusOnDate = attendanceLogs.find { 
                        it.studentId == studentId && it.dateString == selectedDateStr 
                    }?.status ?: "Unmarked"

                    AttendanceStudentItem(
                        studentDetails = studentDetails,
                        currentStatus = statusOnDate,
                        onMarked = { status ->
                            viewModel.markAttendance(studentId, selectedDateStr, status)
                            Toast.makeText(context, "${studentDetails.student.name} marked as $status", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceStudentItem(
    studentDetails: StudentWithDetails,
    currentStatus: String,
    onMarked: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = studentDetails.student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "${studentDetails.className} • ${studentDetails.groupName}", fontSize = 11.sp, color = Color.Gray)
                }

                // Color-badge based on status on that Date
                AssistChip(
                    onClick = {},
                    label = { Text(text = currentStatus, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (currentStatus) {
                            "Present" -> Color(0xFFD4EDDA)
                            "Absent" -> Color(0xFFFFD2D2)
                            "Late" -> Color(0xFFFFF3CD)
                            else -> Color(0xFFF0F4F8)
                        },
                        labelColor = when (currentStatus) {
                            "Present" -> Color(0xFF155724)
                            "Absent" -> Color(0xFFC62828)
                            "Late" -> Color(0xFF856404)
                            else -> Color.DarkGray
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onMarked("Present") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentStatus == "Present") Color(0xFFE8F5E9) else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, if (currentStatus == "Present") Color(0xFF2E7D32) else Color.LightGray),
                    modifier = Modifier.weight(1f).testTag("mark_present_${studentDetails.student.id}")
                ) {
                    Text("Present", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentStatus == "Present") Color(0xFF2E7D32) else Color.DarkGray)
                }

                OutlinedButton(
                    onClick = { onMarked("Absent") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentStatus == "Absent") Color(0xFFFFEBEE) else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, if (currentStatus == "Absent") Color(0xFFC62828) else Color.LightGray),
                    modifier = Modifier.weight(1f).testTag("mark_absent_${studentDetails.student.id}")
                ) {
                    Text("Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentStatus == "Absent") Color(0xFFC62828) else Color.DarkGray)
                }

                OutlinedButton(
                    onClick = { onMarked("Late") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentStatus == "Late") Color(0xFFFFFDE7) else Color.Transparent
                    ),
                    border = BorderStroke(1.dp, if (currentStatus == "Late") Color(0xFF856404) else Color.LightGray),
                    modifier = Modifier.weight(1f).testTag("mark_late_${studentDetails.student.id}")
                ) {
                    Text("Late", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentStatus == "Late") Color(0xFF856404) else Color.DarkGray)
                }
            }
        }
    }
}

// 7. FEE MANAGEMENT VIEW
@Composable
fun FeeManagementView(
    viewModel: EduViewModel,
    user: UserEntity
) {
    val fees by viewModel.fees.collectAsStateWithLifecycle()
    val students by viewModel.studentsWithDetails.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showInvoiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💼 FINANCIAL FEERATES & LEDGER",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Track invoice histories, outstanding collections, and process billing records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (user.role == "Admin") {
                Button(
                    onClick = { showInvoiceDialog = true },
                    modifier = Modifier.testTag("record_invoice_button")
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Invoice")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics breakdown card
        val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Outstandings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("$${stats.totalPendingFeeAmount}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                VerticalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f), modifier = Modifier.height(48.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unpaid Defaulters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${stats.unpaidCount} Accounts", fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Text(text = "Recent School Ledger Historical Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (fees.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No ledger invoice history recorded.")
            }
        } else {
            val studentMap = students.associateBy { it.student.id }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(fees) { fee ->
                    val student = studentMap[fee.studentId]
                    val studentName = student?.student?.name ?: "Unknown Student (ID #${fee.studentId})"
                    val stClassDetails = student?.let { "${it.className} (${it.groupName})" } ?: "Academic Records Unknown"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = stClassDetails, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Period: ${fee.monthName} | Amount: $${fee.amount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (fee.isPaid && fee.paymentDate != null) {
                                    Text(text = "Payment verified on ${fee.paymentDate}", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                }
                            }

                            Button(
                                onClick = {
                                    if (user.role == "Admin") {
                                        viewModel.recordFeePayment(fee.id, !fee.isPaid)
                                        Toast.makeText(context, "Payment status toggled!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Only Admin accounts can write payment histories.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (fee.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (fee.isPaid) "Paid" else "Unpaid",
                                    color = if (fee.isPaid) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: CREATE CUSTOM MONTH INPUT INVOICE BILL FOR STUDENT
    if (showInvoiceDialog) {
        var selectedStudentIndex by remember { mutableStateOf(0) }
        var inputMonthRef by remember { mutableStateOf("June 2026") }
        var inputAmountStr by remember { mutableStateOf("150.0") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showInvoiceDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Add Billing Invoice", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Enrolled Student Directory File:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val labelText = if (students.isNotEmpty() && selectedStudentIndex in students.indices) {
                            students[selectedStudentIndex].student.name
                        } else "No Student Registered File"

                        OutlinedButton(onClick = { dropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = labelText)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            students.forEachIndexed { idx, item ->
                                DropdownMenuItem(
                                    text = { Text(item.student.name) },
                                    onClick = { 
                                        selectedStudentIndex = idx
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputMonthRef,
                        onValueChange = { inputMonthRef = it },
                        label = { Text("Invoice Billing Period (e.g. June 2026)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputAmountStr,
                        onValueChange = { inputAmountStr = it },
                        label = { Text("Bill Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showInvoiceDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Dismiss")
                        }
                        Button(
                            onClick = {
                                if (students.isEmpty()) {
                                    Toast.makeText(context, "No enrolled students registered", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val stId = students[selectedStudentIndex].student.id
                                val rate = inputAmountStr.toDoubleOrNull() ?: 150.0
                                viewModel.addFeeRecord(
                                    FeeHistoryEntity(
                                        studentId = stId,
                                        monthName = inputMonthRef.trim(),
                                        amount = rate,
                                        isPaid = false
                                    )
                                )
                                showInvoiceDialog = false
                                Toast.makeText(context, "Billing invoice successfully rendered", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("save_invoice_button")
                        ) {
                            Text("Record Invoice")
                        }
                    }
                }
            }
        }
    }
}

// 8. REPORT GENERATOR SCREEN
@Composable
fun ReportGeneratorView(
    viewModel: EduViewModel,
    user: UserEntity
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()

    var reportText by remember { mutableStateOf("") }

    // Generate output immediately on load or when parameters update
    LaunchedEffect(key1 = stats) {
        reportText = viewModel.generateSystemReport()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📊 SCHOOL PORTAL REPORT ENGINE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Generate comprehensive database auditing sheets containing general counts, financials liabilities, and summaries.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 12.dp)
                .testTag("report_text_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SelectionContainer {
                    Text(
                        text = reportText,
                        color = Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(reportText))
                    Toast.makeText(context, "Auditing sheet copied to Clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("copy_report_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Report Metadata")
            }

            Button(
                onClick = {
                    reportText = viewModel.generateSystemReport()
                    Toast.makeText(context, "Syncing and building live snapshot...", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refresh Data Snapshot")
            }
        }
    }
}
