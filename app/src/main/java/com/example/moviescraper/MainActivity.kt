package com.example.moviescraper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.moviescraper.ui.navigation.AppNav
import com.example.moviescraper.ui.theme.MovieTheme
class MainActivity : ComponentActivity() { override fun onCreate(s: Bundle?) { super.onCreate(s); setContent { MovieTheme { AppNav() } } } }