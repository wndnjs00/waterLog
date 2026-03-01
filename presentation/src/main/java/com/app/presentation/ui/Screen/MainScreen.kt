package com.app.presentation.ui.Screen

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.domain.model.UserInfo
import com.app.domain.model.WaterLog
import com.app.presentation.R
import com.app.presentation.ui.Screen.Main.CircularWaterProgress
import com.app.presentation.ui.Screen.Main.MainNavigationContent
import com.app.presentation.ui.Screen.Main.WaterControlSection
import com.app.presentation.ui.Screen.Main.WeeklyChartScreen
import com.app.presentation.ui.Screens
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.ui.theme.WaterLogTheme
import com.app.presentation.viewModel.MainViewModel
import com.app.presentation.viewModel.WaterViewModel
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.core.data.errorcode.NidOAuthErrorCode
import com.navercorp.nid.oauth.util.NidOAuthCallback
import kotlinx.coroutines.launch
import okhttp3.internal.wait

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
    viewModel: WaterViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val todayLog by viewModel.todayLog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadToday()
        viewModel.loadWeekly()
        viewModel.loadMonthly()
    }

    todayLog?.let { log ->
        MainNavigationContent(
            log = log,
            onAdd = { viewModel.addCup() },
            onRemove = { viewModel.removeCup() },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBars(viewModel: MainViewModel, navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val userInfo by viewModel.userInfo.collectAsStateWithLifecycle()

    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MainBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "뱃지 아이콘"
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.alert),
                    contentDescription = "알람 아이콘"
                )
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
        streak = 6,
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
        streak = 10
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
                onAdd = {},
                onRemove = {},
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    }
}