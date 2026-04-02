package com.app.presentation.ui.Screen

import android.os.Build
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.app.domain.model.UserInfo
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.domain.model.WaterLog
import com.app.presentation.R
import com.app.presentation.ui.Screen.Ai.AiScreen
import com.app.presentation.ui.Screen.Main.Badge.BadgeDialog
import com.app.presentation.ui.Screen.Main.MainNavigationContent
import com.app.presentation.ui.Screens
import com.app.presentation.ui.components.WaterLogBaseDialog
import com.app.presentation.ui.event.UiEvent
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.ui.theme.WaterLogTheme
import com.app.presentation.viewModel.MainViewModel
import com.app.presentation.viewModel.NotificationViewModel
import com.app.presentation.viewModel.WaterViewModel
import kotlinx.coroutines.launch

// 큰틀
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    credentialManager: CredentialManager,
    navController: NavHostController
) {
    val accountUserInfo by viewModel.userInfo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val innerNavController = rememberNavController()
    val innerNavBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentInnerRoute = innerNavBackStackEntry?.destination?.route
    var showBadgeDialog by remember { mutableStateOf(false) }
    var badgeKey by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
                    .show()

                is UiEvent.NavigateToLogin -> navController.navigate(Screens.Login.route) {
                    popUpTo(Screens.Main.route) { inclusive = true }
                }

                is UiEvent.ShowBadgeDialog -> {
                    badgeKey = event.badgeKey
                    showBadgeDialog = true
                }
            }
        }
    }

    if (showBadgeDialog) {
        BadgeDialog(
            badgeKey = badgeKey,
            onDismiss = { showBadgeDialog = false }
        )
    }

    LaunchedEffect(accountUserInfo) {
        accountUserInfo?.let {
            Toast.makeText(context, "${it.name}님 환영합니다", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBars(
                viewModel = viewModel,
                navController = navController,
                currentInnerRoute = currentInnerRoute
            )
        },
        bottomBar = {
            MainBottomNavigationBar(innerNavController)
        }
    ) { innerPadding ->

        NavHost(
            navController = innerNavController,
            startDestination = MainNavigationItem.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainNavigationItem.Main.route) {
                MainNavigationScreen()
            }

            composable(MainNavigationItem.Ai.route) {
                AiScreen()
            }
        }
    }
}

sealed class MainNavigationItem(
    val route: String,
    @DrawableRes val iconRes: Int,
    val name: String
) {
    object Main : MainNavigationItem("Main Tab", R.drawable.water_drop_img, "물마시기")
    object Ai : MainNavigationItem("Ai Tab", R.drawable.ai_chat_img, "Ai 도우미")
}


@Composable
fun MainBottomNavigationBar(navController: NavHostController) {

    // 탭 아이템 구성
    val items = listOf(
        MainNavigationItem.Main,
        MainNavigationItem.Ai,
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.route
                    )
                },
                label = { Text(text = item.name) },
                selected = currentRoute == item.route,
                onClick = {
                    // 현재선택한 탭의 route로 이동해라
                    navController.navigate(item.route) {
                        //백스택 정리
                        popUpTo(navController.graph.startDestinationId) {
                            // 이전화면 기억하고 복원
                            saveState = true
                        }
                        //중복된 화면 재활용
                        launchSingleTop = true
                        //이전화면 기억하고 복원
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MainBlue,
                    selectedTextColor = MainBlue,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

//각각에 들어갈 화면 구현
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigationScreen(
    waterViewModel: WaterViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayLog by waterViewModel.todayLog.collectAsState()
    val userInfo by mainViewModel.userInfo.collectAsState()

    // 소셜 로그인 시 userInfo가 나중에 로드되므로, userInfo가 준비되면 오늘 물로그 로드
    LaunchedEffect(userInfo) {
        if (userInfo != null) {
            waterViewModel.loadToday()
        }
    }

    LaunchedEffect(Unit) {
        waterViewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
                    .show()

                is UiEvent.NavigateToLogin -> {}
                is UiEvent.ShowBadgeDialog -> {}
            }
        }
    }

    todayLog?.let { log ->
        userInfo?.let { user ->
            MainNavigationContent(
                log = log,
                streakDays = user.streakDays ?: 0,
                onAdd = { waterViewModel.addCup() },
                onRemove = { waterViewModel.removeCup() },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBars(
    viewModel: MainViewModel,
    navController: NavHostController,
    currentInnerRoute: String?,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val userInfo by viewModel.userInfo.collectAsStateWithLifecycle()
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    var showWithdrawConfirmDialog by remember { mutableStateOf(false) }
    var isEmailPasswordStep by remember { mutableStateOf(false) }
    var emailDeletePassword by remember { mutableStateOf("") }

    val title = when (currentInnerRoute) {
        MainNavigationItem.Ai.route -> "Ai 수분섭취 도우미"
        MainNavigationItem.Main.route, null -> "WaterLog"
        else -> "WaterLog"
    }

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MainBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = {
                navController.navigate(Screens.Badge.route)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.badge_img),
                    contentDescription = "뱃지 아이콘"
                )
            }
            IconButton(onClick = {
                navController.navigate(Screens.Notification.route)
            }) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color.Red
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bell_img),
                        contentDescription = "알람 아이콘"
                    )
                }
            }

            IconButton(onClick = {
                coroutineScope.launch {
                    userInfo?.loginProvider?.let { viewModel.logout(it) }
                    // 로그인 화면으로 이동
                    navController.navigate(Screens.Login.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.logout_img),
                    contentDescription = "로그아웃 아이콘"
                )
            }

            IconButton(onClick = {
                showWithdrawConfirmDialog = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.signout_img),
                    contentDescription = "회원탈퇴 아이콘"
                )
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "목표달성 못함")
@Composable
fun MainNavigationScreenProgressPreview() {

    val fakeLog = WaterLog(
        date = "2026/02/27",
        cups = 5,
        targetCups = 8,
        totalMl = 1250,
        updatedAt = "2026/02/27 15:20:00",
    )

    WaterLogTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("WaterLog") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MainBlue,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->

            MainNavigationContent(
                log = fakeLog,
                streakDays = 6,
                onAdd = {},
                onRemove = {},
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "목표달성함")
@Composable
fun MainNavigationScreenGoalPreview() {
    val goalFakeLog = WaterLog(
        date = "2026/02/27",
        cups = 8,
        targetCups = 8,
        totalMl = 2000,
        updatedAt = "2026/02/27 15:25:00",
    )

    WaterLogTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("WaterLog") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MainBlue,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->

            MainNavigationContent(
                log = goalFakeLog,
                streakDays = 10,
                onAdd = {},
                onRemove = {},
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    }
}