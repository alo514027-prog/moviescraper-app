package com.example.moviescraper.ui.screens

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.*
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviescraper.viewmodel.*
import kotlinx.coroutines.delay
import java.util.HashMap

class WebInterface(val onH: (String) -> Unit) { @JavascriptInterface fun process(h: String) { onH(h) } }

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNav: (String) -> Unit) {
    val vm: HomeViewModel = viewModel(); val state by vm.state.collectAsState(); val ctx = LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text(text = "🎬 HDFilmCehennemi") }) }) { p ->
        Box(Modifier.fillMaxSize()) {
            if (state is HomeUiState.Loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                AndroidView(factory = { c -> WebView(c).apply {
                    settings.javaScriptEnabled = true; settings.userAgentString = "Mozilla/5.0 Chrome/122.0.0.0"
                    addJavascriptInterface(WebInterface { h -> vm.onHtml(h, "🎬 Filmler") }, "Android")
                    webViewClient = object : WebViewClient() { override fun onPageFinished(v: WebView?, u: String?) { v?.loadUrl("javascript:window.Android.process(document.documentElement.outerHTML);") }}
                    loadUrl("https://www.hdfilmcehennemi.nl")
                }}, Modifier.size(1.dp))
                AndroidView(factory = { c -> WebView(c).apply {
                    settings.javaScriptEnabled = true; settings.userAgentString = "Mozilla/5.0 Chrome/122.0.0.0"
                    addJavascriptInterface(WebInterface { h -> vm.onHtml(h, "📺 Diziler") }, "Android")
                    webViewClient = object : WebViewClient() { override fun onPageFinished(v: WebView?, u: String?) { v?.loadUrl("javascript:window.Android.process(document.documentElement.outerHTML);") }}
                    loadUrl("https://www.hdfilmcehennemi.nl/yabancidiziizle-5/")
                }}, Modifier.size(1.dp))
            }
            if (state is HomeUiState.Success) {
                LazyColumn(Modifier.padding(p)) { items((state as HomeUiState.Success).cats) { c ->
                    Text(text = c.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    LazyRow { items(c.movies) { m ->
                        Card(Modifier.width(160.dp).padding(8.dp).clickable { onNav(m.detailUrl) }) {
                            Box {
                                AsyncImage(ImageRequest.Builder(ctx).data(m.posterUrl).addHeader("Referer","https://www.hdfilmcehennemi.nl/").build(), null, Modifier.height(220.dp), contentScale = ContentScale.Crop)
                                Text(text = m.title, maxLines = 1, color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(0.6f)).fillMaxWidth().padding(4.dp))
                            }
                        }
                    }}
                }}
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DetailScreen(url: String, onPlay: (String, String) -> Unit) {
    val vm: DetailViewModel = viewModel(); val detail by vm.state.collectAsState()
    if (detail == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(); AndroidView(factory = { c -> WebView(c).apply {
            settings.javaScriptEnabled = true; settings.userAgentString = "Mozilla/5.0 Chrome/122.0.0.0"
            addJavascriptInterface(WebInterface { h -> vm.onHtml(h, url) }, "Android")
            webViewClient = object : WebViewClient() { override fun onPageFinished(v: WebView?, u: String?) { v?.loadUrl("javascript:window.Android.process(document.documentElement.outerHTML);") }}
            loadUrl(url)
        }}, Modifier.size(1.dp)) }
    }
    detail?.let { d ->
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(text = d.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if(d.isSeries) {
                Text(text = "📺 Bölümler:", modifier = Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold)
                d.episodes.forEach { e -> Button(onClick = { onPlay(e.url, d.title) }, Modifier.fillMaxWidth().padding(top = 4.dp)) { Text(text = e.title) } }
            } else {
                d.sources.forEach { s -> Button(onClick = { onPlay(s.url, d.title) }, Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(text = "▶ " + s.name) } }
            }
            Text(text = d.description, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayerScreen(videoUrl: String, title: String) {
    val context = LocalContext.current; var directUrl by remember { mutableStateOf<String?>(null) }
    var useWeb by remember { mutableStateOf(false) }; val logs = remember { mutableStateListOf<String>() }
    val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

    LaunchedEffect(videoUrl) { delay(10000); if(directUrl == null) useWeb = true }
    DisposableEffect(Unit) {
        val a = context as? ComponentActivity; a?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { a?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
    }

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        if (directUrl != null && !useWeb) {
            val exo = remember(directUrl) {
                val ds = DefaultHttpDataSource.Factory().setUserAgent(ua).setDefaultRequestProperties(mapOf("Referer" to "https://www.hdfilmcehennemi.nl/"))
                ExoPlayer.Builder(context).setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(ds)).build().apply {
                    setMediaItem(MediaItem.fromUri(Uri.parse(directUrl!!))); prepare(); playWhenReady = true
                    addListener(object : Player.Listener { override fun onPlayerError(e: PlaybackException) { useWeb = true } })
                }
            }
            DisposableEffect(exo) { onDispose { exo.release() } }
            AndroidView(factory = { ctx -> PlayerView(ctx).apply { player = exo; layoutParams = FrameLayout.LayoutParams(-1,-1) } }, Modifier.fillMaxSize())
        } else {
            if(!useWeb) CircularProgressIndicator(color = Color.White)
            AndroidView(factory = { ctx -> WebView(ctx).apply {
                settings.javaScriptEnabled = true; settings.domStorageEnabled = true; settings.userAgentString = ua
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(v: WebView?, r: WebResourceRequest?): WebResourceResponse? {
                        val u = r?.url.toString()
                        if((u.contains(".m3u8") || u.contains(".mp4")) && directUrl == null) directUrl = u
                        return super.shouldInterceptRequest(v, r)
                    }
                }
                loadUrl(videoUrl, mapOf("Referer" to "https://www.hdfilmcehennemi.nl/"))
            }}, if(useWeb) Modifier.fillMaxSize() else Modifier.size(1.dp))
        }
        Text(text = if(directUrl == null) "Yayın aranıyor..." else "Oynatılıyor", color = Color.White.copy(0.5f), modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
    }
}