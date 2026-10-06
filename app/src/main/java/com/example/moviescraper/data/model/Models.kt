package com.example.moviescraper.data.model
data class Movie(val id: String, val title: String, val posterUrl: String, val detailUrl: String, val isSeries: Boolean = false)
data class Category(val name: String, val movies: List<Movie>)
data class StreamSource(val name: String, val url: String)
data class Episode(val title: String, val url: String)
data class MovieDetail(val title: String, val description: String, val posterUrl: String, val sources: List<StreamSource> = emptyList(), val episodes: List<Episode> = emptyList(), val isSeries: Boolean = false)