package com.example.moviescraper.data.remote
import com.example.moviescraper.data.model.*
import org.jsoup.Jsoup
class HdFilmCehennemScraper {
    private val baseUrl = "https://www.hdfilmcehennemi.nl"
    fun parseList(h: String, name: String): Category {
        val d = Jsoup.parse(h, baseUrl)
        val m = d.select("a").mapNotNull { a ->
            val img = a.selectFirst("img"); val href = a.attr("abs:href")
            if (img != null && href.contains("hdfilmcehennemi.nl") && href != "$baseUrl/") {
                val s = img.attr("data-src").ifEmpty { img.attr("src") }
                val t = a.attr("title").ifEmpty { img.attr("alt") }.ifEmpty { a.text() }
                if (s.isNotEmpty() && t.length > 1) Movie(href.hashCode().toString(), t.trim(), s, href, href.contains("/dizi/")) else null
            } else null
        }.distinctBy { it.detailUrl }
        return Category(name, m)
    }
    fun parseDetail(h: String, u: String): MovieDetail {
        val d = Jsoup.parse(h, u); val isS = u.contains("/dizi/")
        val s = d.select("div.alternatives a, div.player-sources a, iframe").mapNotNull { el ->
            val url = el.attr("data-src").ifEmpty { el.attr("src") }.ifEmpty { el.attr("abs:href") }
            if (url.startsWith("http") || url.startsWith("//")) StreamSource(el.text().ifEmpty { "Kaynak" }, url) else null
        }.distinctBy { it.url }
        val ep = if(isS) d.select("a[href*='bolum']").map { Episode(it.text(), it.attr("abs:href")) } else emptyList()
        return MovieDetail(d.selectFirst("h1")?.text() ?: "Film", d.selectFirst("div.description, p")?.text() ?: "", "", s, ep, isS)
    }
}