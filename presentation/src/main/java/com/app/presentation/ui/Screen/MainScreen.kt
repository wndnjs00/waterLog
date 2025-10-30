package com.app.presentation.ui.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.presentation.R
import com.app.presentation.ui.theme.MainBlue
import com.app.presentation.ui.theme.WaterLogTheme

// 큰틀
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopAppBars() },
//        bottomBar = {
//            MainBottomNavigationBar(navController)
//        }
    ) { innerPadding ->
        MainNavigationScreen(modifier = Modifier.padding(innerPadding))
    }
}

sealed class MainNavigationItem(val route: String, val icon: ImageVector,val name: String){
    object Main: MainNavigationItem("Main Tab", Icons.Filled.Home, "물마시기")
    object Ai: MainNavigationItem("Ai Tab", Icons.Filled.Star, "Ai도우미")
}


@Composable
fun MainBottomNavigationBar(navController: NavHostController){

    // 탭 아이템 구성
    val bottomNavigationItems = listOf(
        MainNavigationItem.Main,
        MainNavigationItem.Ai,
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavigationItems.forEach{ item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.route)},
                label = {Text(text = item.name)},
                selected = currentRoute == item.route,
                onClick = {
                    // 현재선택한 탭의 route로 이동해라
                    navController.navigate(item.route){

                        navController.graph.startDestinationRoute?.let {startRoute ->
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
@Composable
fun MainNavigationScreen(modifier: Modifier = Modifier){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Main 화면")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBars(){
    TopAppBar(
        title = {Text(stringResource(id = R.string.app_name), color = Color.White)},
        colors = TopAppBarDefaults.largeTopAppBarColors(
            actionIconContentColor = Color.White
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
        }
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WaterLogTheme {
        MainScreen()
    }
}