package me.corv.pillenalarm.gif

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest

class TenorProvider : GifProvider {

    companion object {
        const val TAG = "TenorProvider"
        const val BASE_URL = "https://tenor.googleapis.com/v2/search"
        const val API_KEY = "AIzaSyDTHWLxDt1_4kL61NFffdWAyhI6xNcHX1A"
    }

    override fun getGifRequest(
        tags: List<String>?,
        callback: (gifUrl: String) -> Unit,
        onError: ((error: Throwable) -> Unit)?
    ): JsonObjectRequest {
        return JsonObjectRequest(
            Request.Method.GET, getRequestUrl(tags ?: GifProvider.TAGS), null,
            { response ->
                var gifUrl = GifProvider.FALLBACK
                try {
                    gifUrl = response
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("media_formats")
                        .getJSONObject("gif")
                        .getString("url")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get gif from response", e)
                    onError?.invoke(e)
                }
                callback.invoke(gifUrl)
            }, onError
        )
    }

    private fun getRequestUrl(tags: List<String>): String {
        val params = listOf(
            "key=$API_KEY",
            "q=" + sanitize(tags).joinToString("+"),
            "country=DE",
            "locale=en_US",
            "contentfilter=off",
            "media_filter=gif",
            "ar_range=standard",
            "limit=1",
            "random=true"
        )
        return "$BASE_URL?${params.joinToString("&")}"
    }
}