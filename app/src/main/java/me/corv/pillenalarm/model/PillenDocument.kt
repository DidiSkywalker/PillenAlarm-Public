package me.corv.pillenalarm.model

import me.corv.pillenalarm.actions.DoneAction

data class PillenDocument(
    var done: Boolean = false,
    var periodEnd: Long = 0,
    var catGif: String = DoneAction.FALLBACK,
    var scheduledReminder: Long = 0,
    var token: String = "",
    var likedGifs: ArrayList<String> = ArrayList(),
    var gifTags: ArrayList<String> = ArrayList(listOf("cat", "cute")),
    var gifProvider: String = "Tenor"
)
