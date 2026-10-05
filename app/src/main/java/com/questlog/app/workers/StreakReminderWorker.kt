package com.questlog.app.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.questlog.app.R
import com.questlog.app.data.repository.ClockInRepository
import com.questlog.app.data.repository.ReminderPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val clockInRepository: ClockInRepository,
    private val reminderPreferences: ReminderPreferences,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!reminderPreferences.enabled.first()) return Result.success()

        val today = currentEpochDay()
        if (clockInRepository.lastCheckInEpochDay.first() == today) return Result.success()
        if (reminderPreferences.lastNaggedEpochDay.first() == today) return Result.success()
        if (!notificationsGranted()) return Result.success()

        notifyOnce()
        reminderPreferences.markNagged(today)
        return Result.success()
    }

    private fun notificationsGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun notifyOnce() {
        val manager = NotificationManagerCompat.from(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    applicationContext.getString(R.string.clockin_reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.clockin_reminder_title))
            .setContentText(applicationContext.getString(R.string.clockin_reminder_text))
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        }
    }

    private fun currentEpochDay(): Long = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .toEpochDays()
        .toLong()

    private companion object {
        const val CHANNEL_ID = "questlog_reminders"
        const val NOTIFICATION_ID = 1001
    }
}
