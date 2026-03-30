package com.nanmmm

import com.lagradost.cloudstream3.*
import org.jsoup.nodes.Element

class AllMoviesHubProvider : MainAPI() {
    override var mainUrl = "https://allmovieshub.golf"
    override var name = "AllMoviesHub"
    override val hasMainPage = true
    override var lang = "en"
    override val supportedTypes = setOf(TvType.Movie)

    override val mainPage = mainPageOf(
        "$mainUrl/movies/" to "Latest Movies",
        "$mainUrl/bollywood-movies/" to "Bollywood",
        "$mainUrl/hollywood-movies/" to "Hollywood",
        "$mainUrl/south-movies/" to "South Indian",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val url = if (page == 1) request.data else "${request.data}page/$page/"
        val doc = app.get(url).document
        val items = doc.select("article.post").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = selectFirst("h2.post-title a")?.text() ?: return null
        val href = selectFirst("h2.post-title a")?.attr("href") ?: return null
        val poster = selectFirst("img")?.attr("src")
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = poster
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("article.post").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1.post-title")?.text() ?: return null
        val poster = doc.selectFirst(".post-thumbnail img")?.attr("src")
        val plot = doc.selectFirst(".entry-content p")?.text()
        val year = Regex("""\b(19|20)\d{2}\b""").find(title)?.value?.toIntOrNull()
        return newMovieLoadResponse(title, url, TvType.Movie, url) {
            this.posterUrl = poster
            this.plot = plot
            this.year = year
        }
    }
}
