package com.example.moviescraper.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
@Composable fun MovieTheme(c: @Composable () -> Unit) { MaterialTheme(colorScheme = darkColorScheme(), content = c) }