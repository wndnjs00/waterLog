package com.app.presentation.ui.Screen

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.domain.model.UserInfo
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.domain.model.WaterLog
import com.app.presentation.R
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

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is UiEvent.NavigateToLogin -> navController.navigate(Screens.Login.route) {
                        popUpTo(Screens.Main.route) { inclusive = true }
                    }
            }
        }
    }

    LaunchedEffect(accountUserInfo) {
        accountUserInfo?.let {
            Toast.makeText(context, "${it.name}님 환영합니다", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { TopAppBars(viewModel = viewModel, navController = navController) },
//        bottomBar = {
//            MainBottomNavigationBar(navController)
//        }
    ) { innerPadding ->
        MainNavigationScreen(modifier = Modifier.padding(innerPadding))
    }
}

sealed class MainNavigationItem(val route: String, val icon: ImageVector, val name: String) {
    object Main : MainNavigationItem("Main Tab", Icons.Filled.Home, "물마시기")
    object Ai : MainNavigationItem("Ai Tab", Icons.Filled.Star, "Ai도우미")
}


@Composable
fun MainBottomNavigationBar(navController: NavHostController) {

    // 탭 아이템 구성
    val bottomNavigationItems = listOf(
        MainNavigationItem.Main,
        MainNavigationItem.Ai,
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.route) },
                label = { Text(text = item.name) },
                selected = currentRoute == item.route,
                onClick = {
                    // 현재선택한 탭의 route로 이동해라
                    navController.navigate(item.route) {

                        navController.graph.startDestinationRoute?.let { startRoute ->
                            //백스택 정리
                            popUpTo(startRoute) {
                                // 이전화면 기억하고 복원
                                saveState = true
                            }
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
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is UiEvent.NavigateToLogin -> {  }
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
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val userInfo by viewModel.userInfo.collectAsStateWithLifecycle()
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    var showWithdrawConfirmDialog by remember { mutableStateOf(false) }
    var isEmailPasswordStep by remember { mutableStateOf(false) }
    var emailDeletePassword by remember { mutableStateOf("") }
    var showBadgeInfoDialog by remember { mutableStateOf(false) }

    // 회원탈퇴 다이얼로그 (1단계: 확인 / 2단계: 이메일일때 비밀번호 입력)
    if (showWithdrawConfirmDialog) {
        val dialogTextColor = Color.Black
        val cancelBorderGray = Color(0xFFE0E0E0)
        val provider = userInfo?.loginProvider

        WaterLogBaseDialog(
            onDismissRequest = {
                showWithdrawConfirmDialog = false
                isEmailPasswordStep = false
                emailDeletePassword = ""
            }
        ) {
            if (isEmailPasswordStep) {
                // 2단계: 이메일 비밀번호 입력
                Text(
                    text = "정말 탈퇴하시겠습니까?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = dialogTextColor
                )
                Text(
                    text = "본인 확인을 위해 비밀번호를 입력해주세요.",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = dialogTextColor
                )
                OutlinedTextField(
                    value = emailDeletePassword,
                    onValueChange = { emailDeletePassword = it },
                    label = { Text("비밀번호") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            } else {
                // 1단계: 탈퇴 확인
                Text(
                    text = "정말 탈퇴하시겠습니까?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = dialogTextColor
                )
                Text(
                    text = buildAnnotatedString {
                        append("탈퇴시 계정과 저장된 사항이 모두 삭제되며,")
                        append("\n")
                        append("복구되지 않습니다. 계속 진행하시겠습니까?")
                    },
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = dialogTextColor
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (isEmailPasswordStep) {
                            isEmailPasswordStep = false
                            emailDeletePassword = ""
                        } else {
                            showWithdrawConfirmDialog = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    border = BorderStroke(1.dp, cancelBorderGray),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("취소", style = MaterialTheme.typography.bodyLarge)
                }
                OutlinedButton(
                    onClick = {
                        if (isEmailPasswordStep) {
                            provider?.let { p ->
                                coroutineScope.launch {
                                    viewModel.deleteAccount(p, emailDeletePassword)
                                    showWithdrawConfirmDialog = false
                                    isEmailPasswordStep = false
                                    emailDeletePassword = ""
                                }
                            }
                        } else {
                            when (provider) {
                                UserInfo.LoginProvider.EMAIL -> {
                                    isEmailPasswordStep = true
                                }
                                else -> {
                                    showWithdrawConfirmDialog = false
                                    provider?.let { p ->
                                        coroutineScope.launch {
                                            viewModel.deleteAccount(p)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MainBlue),
                    border = BorderStroke(1.dp, MainBlue),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("탈퇴하기", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    // 뱃지 안내 다이얼로그
    if (showBadgeInfoDialog) {
        WaterLogBaseDialog(
            onDismissRequest = { showBadgeInfoDialog = false }
        ) {
            Text(
                text = "뱃지 기능 개발중입니다.",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = "조금만 기다려주세요!",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showBadgeInfoDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MainBlue),
                    border = BorderStroke(1.dp, MainBlue),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("확인", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MainBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = { showBadgeInfoDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
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
                        painter = painterResource(id = R.drawable.alert),
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
                    painter = painterResource(id = R.drawable.logout),
                    contentDescription = "로그아웃 아이콘"
                )
            }

            IconButton(onClick = {
                showWithdrawConfirmDialog = true
            }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_set_as),
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