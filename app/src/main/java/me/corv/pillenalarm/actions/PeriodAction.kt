package me.corv.pillenalarm.actions

import android.content.Context
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import me.corv.pillenalarm.ENVIRONMENT
import me.corv.pillenalarm.model.PillenDocument
import me.corv.pillenalarm.nextSundayAt9pm
import me.corv.pillenalarm.toEpochMilli

class PeriodAction(private val context: Context) {
    fun execute(document: PillenDocument) {
        DoneAction(context).execute(document)
        Firebase.firestore
            .collection(ENVIRONMENT.collection)
            .document(ENVIRONMENT.document)
            .update("periodEnd", toEpochMilli(nextSundayAt9pm()))
    }
}