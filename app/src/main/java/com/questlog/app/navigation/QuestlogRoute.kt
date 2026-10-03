package com.questlog.app.navigation

sealed class QuestlogRoute(val route: String) {
    object Library : QuestlogRoute("library")
    object Discover : QuestlogRoute("discover")
    object Capture : QuestlogRoute("capture")
    object CaptureResult : QuestlogRoute("capture/result")

    data class GameDetail(val gameId: String) : QuestlogRoute("game/$gameId") {
        companion object {
            const val ROUTE = "game/{gameId}"
        }
    }

    object Stats : QuestlogRoute("stats")
    object Settings : QuestlogRoute("settings")
}
