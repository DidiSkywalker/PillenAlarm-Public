package me.corv.pillenalarm.gif

import com.android.volley.toolbox.JsonObjectRequest

interface GifProvider {
    companion object {
        val TAGS = listOf("baby", "animal", "cute")
        const val FALLBACK =
            "https://media0.giphy.com/media/uB95dmqTMDCsU/giphy.gif?cid=d7a716cdznlhw68i4kjlbvlw2vgjkuchk3uz4dkxsmoyhzvw&ep=v1_gifs_random&rid=giphy.gif&ct=g"
        val PROVIDERS = mapOf(
            "giphy" to GiphyProvider(),
            "tenor" to TenorProvider()
        )
    }

    fun getGifRequest(tags: List<String>?, callback: (gifUrl: String) -> Unit, onError: ((error: Throwable) -> Unit)?): JsonObjectRequest

    fun sanitize(tags: List<String>): List<String> {
        return tags.map { it.replace(" ", "+") }
    }
}