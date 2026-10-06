package com.example.moviescraper.viewmodel
import androidx.lifecycle.*
import com.example.moviescraper.data.model.*
import com.example.moviescraper.di.AppContainer
import kotlinx.coroutines.flow.*
sealed class HomeUiState { object Loading : HomeUiState(); data class Success(val cats: List<Category>) : HomeUiState(); data class Error(val m: String) : HomeUiState() }
class HomeViewModel : ViewModel() {
    private val _s = MutableStateFlow<HomeUiState>(HomeUiState.Loading); val state = _s.asStateFlow(); private val cats = mutableListOf<Category>()
    fun onHtml(h: String, name: String) { try { val c = AppContainer.scraper.parseList(h, name); if(!cats.any { it.name == name }) { cats.add(c); _s.value = HomeUiState.Success(cats.toList()) } } catch(e: Exception) { _s.value = HomeUiState.Error(e.message ?: "Hata") } }
}
class DetailViewModel : ViewModel() {
    private val _s = MutableStateFlow<MovieDetail?>(null); val state = _s.asStateFlow()
    fun onHtml(h: String, u: String) { try { _s.value = AppContainer.scraper.parseDetail(h, u) } catch(e: Exception) {} }
}