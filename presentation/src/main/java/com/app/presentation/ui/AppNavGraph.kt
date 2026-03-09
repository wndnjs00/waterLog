@file:Suppress("UNREACHABLE_CODE")

package com.app.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.presentation.ui.Screen.Auth.LoginScreen
import com.app.presentation.ui.Screen.MainScreen
import com.app.presentation.ui.Screen.Auth.SignInScreen
import com.app.presentation.ui.Screen.Auth.SignUpScreen
import com.app.presentation.ui.Screen.Auth.kakaoLogin
import com.app.presentation.ui.Screen.Auth.naverLogin
import com.app.presentation.ui.Screen.Notification.NotificationScreen
import com.app.presentation.viewModel.MainViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    credentialManager: CredentialManager
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screens.Login.route
    ) {
        composable(Screens.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                credentialManager = credentialManager,
                onLoginSuccess = {
                    navController.navigate(Screens.Main.route) {
                        popUpTo(Screens.Login.route) {
                            inclusive = true
                        } //로그인 후 "뒤로가기" 눌러도 다시 로그인화면으로 안 돌아감
                    }
                },
                onSignUpClick = {
                    navController.navigate(Screens.SignUp.route)
                },
                onSignInClick = {
                    navController.navigate(Screens.SignIn.route)
                },
                onKaKaoClick = {
                    // kakao 로그인 버튼클릭동작
                    kakaoLogin(
                        context = context,
                        viewModel = viewModel,
                        onLoginSuccess = {
                            navController.navigate(Screens.Main.route) {
                                popUpTo(Screens.Login.route) { inclusive = true }
                            }
                        }
                    )
                },
                onNaverClick = {
                    // naver 로그인 버튼클릭동작
                    naverLogin(
                        context = context,
                        viewModel = viewModel,
                        onLoginSuccess = {
                            navController.navigate(Screens.Main.route){
                                popUpTo(Screens.Login.route) {inclusive = true}
                            }
                        }
                    )
                },
                navController = navController
            )
        }

        composable(Screens.SignUp.route) {
            SignUpScreen(
                viewModel = viewModel,
                onSignUpComplete = {
                    navController.navigate(Screens.Main.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screens.SignIn.route) {
            SignInScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    // MainScreen으로 이동
                    navController.navigate(Screens.Main.route) {
                        popUpTo(Screens.Login.route) { inclusive = true } //로그인 후 "뒤로가기" 눌러도 다시 로그인화면으로 안 돌아감
                    }
                },
            )
        }

        composable(Screens.Main.route) {
            MainScreen(
                viewModel = viewModel,
                credentialManager = credentialManager,
                navController = navController
            )
        }

        composable(Screens.Notification.route) {
            NotificationScreen(
                navController = navController
            )
        }
    }
}

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object SignUp : Screens("signup")
    object SignIn : Screens("signin")
    object Main : Screens("main")
    object Notification: Screens("notification")
}

