package me.corv.pillenalarm.actions

import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import me.corv.pillenalarm.ENVIRONMENT
import me.corv.pillenalarm.toEpochMilli
import java.time.LocalDateTime

class DelayAction() {

    companion object {
        private val TAG = "DelayAction"
    }

    fun execute(nextAlarm: LocalDateTime, callback: (error: Exception?) -> Unit) {
        execute(toEpochMilli(nextAlarm) - System.currentTimeMillis(), callback)
    }

    fun execute(delay: Long, callback: (error: Exception?) -> Unit) {
        Firebase.functions.getHttpsCallable("scheduleDelay")
            .call(
                hashMapOf(
                    "delay" to delay,
                    "doc" to ENVIRONMENT.document
                )
            )
            .continueWith { task ->
                val result = task.result?.data
                result
            }.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    Log.e(TAG, "Failed to schedule delayed alarm", e)
                    callback.invoke(e)
                } else {
                    callback.invoke(null)
                }
            }
    }
}