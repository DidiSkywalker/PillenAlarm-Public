package me.corv.pillenalarm

private const val COLLECTION = "pillenalarm"
val ENVIRONMENT = Environment.DEV
enum class Environment (val collection: String, val document: String) {
    DEV(COLLECTION, "dev"),
    PROD(COLLECTION, "prod");
}