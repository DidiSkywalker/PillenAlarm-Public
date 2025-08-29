package me.corv.pillenalarm.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.corv.pillenalarm.actions.DelayAction

/**
 * ViewModel for the [TimerFragment][me.corv.pillenalarm.fragments.TimerFragment] which is based
 * on the timer creation of the default Google Clock app.
 *
 * Since input is based on basically typing out the time, I'm using Strings and parse them to
 * numbers on commit.
 */
class TimerViewModel : ViewModel() {

    /**
     * Stores the time typed out by the user
     */
    private var fullText: String = ""

    private val _hoursText: MutableLiveData<String> by lazy {
        MutableLiveData<String>(null)
    }
    val hoursText = _hoursText

    private val _minutesText: MutableLiveData<String> by lazy {
        MutableLiveData<String>(null)
    }
    val minutesText = _minutesText

    private val _commitVisible: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    val commitVisible = _commitVisible

    /**
     * Called on button input with the text value of the button.
     * This can be 0-9 or the special case 00
     */
    fun input(value: String) {
        // Catch leading zeros
        if (fullText.isEmpty() && value.startsWith("0")) return
        if (fullText.length + value.length <= 4) { // ensure no more than two-digit minutes and hours
            // Normal case 0-9: just add the new value
            fullText = "$fullText$value"
            Log.d(TAG, "fullText = $fullText")
        } else if (fullText.length == 3 && value == "00") {
            // Special case: 3 characters and 00 input adds a single 0 instead
            fullText = "${fullText}0"
        }
        updateDisplay()
    }

    /**
     * Called on backspace input.
     * Removes the last typed character.
     */
    fun backspace() {
        if (fullText.isEmpty()) return
        fullText = fullText.substring(0, fullText.length - 1)
        updateDisplay()
    }

    /**
     * Called on input on the commit button.
     * Calculates the input time and calls the [DelayAction] to perform the call to
     * Firebase Functions.
     *
     * @param callback Called after completion of the Functions call. error may be null on
     *                 success or an exception on failure.
     */
    fun commit(callback: (error: Exception?) -> Unit) {
        DelayAction().execute(calculateDelay(), callback)
    }

    /**
     * Converts the input string to minutes and hours and then to milliseconds from now.
     */
    private fun calculateDelay(): Long {
        val minutes = _minutesText.value?.toLong() ?: 0
        val hours = _hoursText.value?.toLong() ?: 0
        val minutesToMillis = minutes * MINUTE_TO_MILLIS_MULTIPLIER
        val hoursToMillis = hours * HOURS_TO_MILLIS_MULTIPLIER
        return minutesToMillis + hoursToMillis
    }

    /**
     * Calculate [hoursText] and [minutesText] from [fullText]
     */
    private fun updateDisplay() {
        when {
            fullText.isEmpty() -> {
                _minutesText.value = null
                _hoursText.value = null
                _commitVisible.value = false
            }

            fullText.length <= 2 -> {
                _minutesText.value = fullText
                _hoursText.value = null
                _commitVisible.value = true
            }

            fullText.length == 3 -> {
                _hoursText.value = fullText.substring(0, 1)
                _minutesText.value = fullText.substring(1)
                _commitVisible.value = true
            }

            fullText.length == 4 -> {
                _hoursText.value = fullText.substring(0, 2)
                _minutesText.value = fullText.substring(2)
                _commitVisible.value = true
            }
        }
        Log.d(TAG, "hours = ${_hoursText.value} minutes = ${_minutesText.value}")
    }

    companion object {
        private const val TAG = "TimerViewModel"
        private const val MINUTE_TO_MILLIS_MULTIPLIER = 60 * 1000
        private const val HOURS_TO_MILLIS_MULTIPLIER = 60 * 60 * 1000
    }
}