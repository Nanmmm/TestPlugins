package com.nanmmm

import com.lagradost.cloudstream3.*

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
        val items = doc.select("article.post").mapNotNull {
            val title = it.selectFirst("h2.post-title a")?.text() ?: return@mapNotNull null
            val href = it.selectFirst("h2.post-title a")?.attr("href") ?: return@mapNotNull null
            newMovieSearchResponse(title, href, TvType.Movie)
        }
        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/?s=$query").document
        return doc.select("article.post").mapNotNull {
            val title = it.selectFirst("h2.post-title a")?.text() ?: return@mapNotNull null
            val href = it.selectFirst("h2.post-title a")?.attr("href") ?: return@mapNotNull null
            newMovieSearchResponse(title, href, TvType.Movie)
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1.post-title")?.text() ?: url
        return newMovieLoadResponse(title, url, TvType.Movie, url)
    }
}
