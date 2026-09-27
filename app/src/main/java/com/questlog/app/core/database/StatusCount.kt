package com.questlog.app.core.database

import com.questlog.app.core.model.GameStatus

data class StatusCount(
    val status: GameStatus,
    val count: Int,
)
