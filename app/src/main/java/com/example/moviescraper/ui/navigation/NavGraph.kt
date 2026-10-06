package com.example.moviescraper.ui.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.moviescraper.ui.screens.*
import java.net.URLDecoder
import java.net.URLEncoder
@Composable
fun AppNav() {
    val c = rememberNavController()
    NavHost(c, "home") {
        composable("home") { HomeScreen { u -> c.navigate("detail/${URLEncoder.encode(u, "UTF-8")}") } }
        composable("detail/{u}", arguments = listOf(navArgument("u"){type=NavType.StringType})) { 
            val u = URLDecoder.decode(it.arguments?.getString("u") ?: "", "UTF-8")
            DetailScreen(u) { vu, t -> c.navigate("player/${URLEncoder.encode(vu, "UTF-8")}/${URLEncoder.encode(t, "UTF-8")}") }
        }
        composable("player/{vu}/{t}", arguments = listOf(navArgument("vu"){type=NavType.StringType}, navArgument("t"){type=NavType.StringType})) {
            PlayerScreen(URLDecoder.decode(it.arguments?.getString("vu")?:"", "UTF-8"), URLDecoder.decode(it.arguments?.getString("t")?:"", "UTF-8"))
        }
    }
}