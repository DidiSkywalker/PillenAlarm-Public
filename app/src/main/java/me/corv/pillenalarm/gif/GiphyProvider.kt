package me.corv.pillenalarm.gif

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import me.corv.pillenalarm.gif.GifProvider.Companion.FALLBACK
import me.corv.pillenalarm.gif.GifProvider.Companion.TAGS

class GiphyProvider : GifProvider {

    companion object {
        private const val TAG = "GiphyProvider"
        private const val BASE_URL = "https://api.giphy.com/v1/gifs/random"
        private const val API_KEY = "pDKvWGNRx2eRAmNfnctEwsZyEzd7wbYg"
    }

    override fun getGifRequest(
        tags: List<String>?,
        callback: (gifUrl: String) -> Unit,
        onError: ((error: Throwable) -> Unit)?
    ): JsonObjectRequest {
        return JsonObjectRequest(
            Request.Method.GET, getRequestUrl(tags ?: TAGS), null,
            { response ->
                var gifUrl = FALLBACK
                try {
                    gifUrl = response
                        .getJSONObject("data")
                        .getJSONObject("images")
                        .getJSONObject("original")
                        .getString("url")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get gif from response", e)
                    onError?.invoke(e)
                }
                callback.invoke(gifUrl)
            }, onError)
    }

    private fun getRequestUrl(tags: List<String>): String {
        return "${BASE_URL}?api_key=${API_KEY}&tag=${sanitize(tags).joinToString("+")}"
    }
}