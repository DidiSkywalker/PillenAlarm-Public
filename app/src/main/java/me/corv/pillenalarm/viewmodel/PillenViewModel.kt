package me.corv.pillenalarm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import me.corv.pillenalarm.ENVIRONMENT
import me.corv.pillenalarm.model.PillenDocument

class PillenViewModel : ViewModel() {

    private val _document: MutableLiveData<PillenDocument> by lazy {
        MutableLiveData<PillenDocument>()
    }
    val document: LiveData<PillenDocument> = _document
    private val firestoreDocument = Firebase.firestore
        .collection(ENVIRONMENT.collection)
        .document(ENVIRONMENT.document)

    private val snapshotListener = firestoreDocument
        .addSnapshotListener { value, error ->
            if (error != null || value == null || value.data == null) {
                Log.e(TAG, "Failed to load PillenDocument", error)
            } else {
                _document.value = value.toObject(PillenDocument::class.java)!!
                Log.d(TAG, "Document update: ${_document.value}")
            }
        }


    /**
     * @return true if the GIF was liked, false if not
     */
    fun toggleLikeCurrentGif(): Boolean {
        return toggleLike(null)
    }

    /**
     * @return true if the GIF was liked, false if not
     */
    fun toggleLike(gif0: String?): Boolean {
        val doc = _document.value ?: return false
        val gif = gif0 ?: doc.catGif
        val isLiked = isGifLiked(gif)
        if (isLiked) {
            doc.likedGifs.remove(gif)
        } else {
            doc.likedGifs.add(gif)
        }
        Firebase.firestore
            .collection(ENVIRONMENT.collection)
            .document(ENVIRONMENT.document)
            .update("likedGifs", doc.likedGifs)
        return !isLiked
    }

    fun isCurrentGifLiked(): Boolean {
        return isGifLiked(null)
    }

    private fun isGifLiked(gif0: String?): Boolean {
        val doc = _document.value ?: return false
        val gif = gif0 ?: doc.catGif
        return doc.likedGifs.contains(gif)
    }

    fun updateDocument() {
        if (_document.value != null) {
            firestoreDocument.set(_document.value!!)
        }
    }

    override fun onCleared() {
        snapshotListener.remove()
        super.onCleared()
    }

    companion object {
        private const val TAG = "PillenViewModel"
    }
}