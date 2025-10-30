package com.app.presentation.ui

import androidx.compose.runtime.Composable

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.presentation.ui.Screen.Auth.LoginScreen
import com.app.presentation.ui.Screen.MainScreen
import com.app.presentation.ui.Screen.Auth.SignInScreen
import com.app.presentation.ui.Screen.Auth.SignUpScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.Login.route
    ){
        composable(Screens.Login.route){
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screens.Main.route){
                        popUpTo(Screens.Login.route) {inclusive = true} //로그인 후 "뒤로가기" 눌러도 다시 로그인화면으로 안 돌아감
                    }
                },
                onSignUpClick = {
                    navController.navigate(Screens.SignUp.route)
                },
                onSignInClick = {
                    navController.navigate(Screens.SignIn.route)
                },
                onKaKaoClick = {
                    // TODO: kokao 로그인 버튼클릭동작 구현
                },
                onNaverClick = {
                    // TODO: naver 로그인 버튼클릭동작 구현
                },
                onGoogleClick = {
                    // TODO: google 로그인 버튼클릭동작 구현
                }
            )
        }

        composable(Screens.SignUp.route){
            SignUpScreen(
                onSignUpComplete = {
                    navController.popBackStack() // 회원가입 완료후 로그인으로 복귀
                }
            )
        }

        composable(Screens.SignIn.route){
            SignInScreen(
                onLoginSuccess = {
                    // TODO: 로그인완료버튼 클릭 동작
                    // MainScreen으로 이동
                    navController.navigate(Screens.Main.route){
                        popUpTo(Screens.Login.route) {inclusive = true} //로그인 후 "뒤로가기" 눌러도 다시 로그인화면으로 안 돌아감
                    }
                }
            )
        }

        composable(Screens.Main.route){
            MainScreen()
        }
    }
}

sealed class Screens(val route: String){
    object Login : Screens("login")
    object SignUp : Screens("signup")
    object SignIn : Screens("signin")
    object Main : Screens("main")
}