package me.corv.pillenalarm.actions

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import me.corv.pillenalarm.ENVIRONMENT
import me.corv.pillenalarm.gif.GifProvider
import me.corv.pillenalarm.gif.TenorProvider
import me.corv.pillenalarm.model.PillenDocument

class DoneAction(private val context: Context) {

    companion object {
        private const val TAG = "DoneAction"
        const val FALLBACK =
            "https://media0.giphy.com/media/uB95dmqTMDCsU/giphy.gif?cid=d7a716cdznlhw68i4kjlbvlw2vgjkuchk3uz4dkxsmoyhzvw&ep=v1_gifs_random&rid=giphy.gif&ct=g"
    }

    fun execute(document: PillenDocument) {
        Firebase.firestore
            .collection(ENVIRONMENT.collection)
            .document(ENVIRONMENT.document)
            .update("done", true)

        val gifProvider = GifProvider.PROVIDERS.getOrDefault(document.gifProvider, TenorProvider())
        Volley.newRequestQueue(context).add(gifProvider.getGifRequest(document.gifTags,
            { url ->
                Firebase.firestore
                    .collection(ENVIRONMENT.collection)
                    .document(ENVIRONMENT.document)
                    .update("catGif", url)
            }, { error ->
                Log.e(TAG, "Failed to fetch new cat gif", error)
            })
        )
    }

}