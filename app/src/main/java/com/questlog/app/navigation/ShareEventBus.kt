package com.questlog.app.navigation

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PendingShare {
    data class Text(val body: String) : PendingShare()
    data class Image(val uri: Uri) : PendingShare()
}

@Singleton
class ShareEventBus @Inject constructor() {

    private val _pending = MutableStateFlow<PendingShare?>(null)
    val pending: StateFlow<PendingShare?> = _pending.asStateFlow()

    fun publish(share: PendingShare) {
        _pending.value = share
    }

    fun consume(): PendingShare? {
        val share = _pending.value
        _pending.value = null
        return share
    }
}
