package com.example.nass.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nass.data.local.SessionStore
import com.example.nass.ui.admin.AdminDashboardScreen
import com.example.nass.ui.auth.LoginScreen
import com.example.nass.ui.auth.RegisterScreen
import com.example.nass.ui.buyer.BuyerDashboardScreen
import com.example.nass.ui.seller.SellerDashboardScreen
import com.example.nass.ui.splash.SplashScreen
import com.example.nass.util.Logger
import kotlinx.coroutines.launch

@Composable
fun NassNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Centralised logout: clears DataStore, then routes to Login.
    val logout: () -> Unit = {
        scope.launch {
            Logger.i("Auth", "Logging out — clearing session")
            SessionStore.from(context).clearSession()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onRouteToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onRouteToDashboard = { role ->
                    val target = when (role.lowercase()) {
                        "seller" -> Routes.SELLER_DASHBOARD
                        "buyer" -> Routes.BUYER_DASHBOARD
                        "admin" -> Routes.ADMIN_DASHBOARD
                        else -> Routes.LOGIN
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val target = when (role.lowercase()) {
                        "seller" -> Routes.SELLER_DASHBOARD
                        "buyer" -> Routes.BUYER_DASHBOARD
                        "admin" -> Routes.ADMIN_DASHBOARD
                        else -> Routes.LOGIN
                    }
                    navController.navigate(target) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack(Routes.LOGIN, inclusive = false) },
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.SELLER_DASHBOARD) {
            SellerDashboardScreen(onLogout = logout)
        }

        composable(Routes.BUYER_DASHBOARD) {
            BuyerDashboardScreen(onLogout = logout)
        }

        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(onLogout = logout)
        }
    }
}