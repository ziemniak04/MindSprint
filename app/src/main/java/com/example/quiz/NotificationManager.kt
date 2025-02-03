package com.example.quiz

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * The [NotificationManager] class is responsible for scheduling, executing immediately,
 * and canceling daily notification reminders using WorkManager.
 *
 * It reads the user's notification time preferences from SharedPreferences and schedules
 * a periodic daily work request. If the calculated delay until the next notification is very short,
 * an immediate one-time work is also scheduled.
 *
 * @property context The [Context] used to access SharedPreferences and schedule work.
 */
class NotificationManager(private val context: Context) {

    /**
     * Schedules a daily reminder notification.
     *
     * This method performs the following steps:
     * 1. Reads the preferred notification hour and minute from SharedPreferences.
     * 2. Cancels any previously scheduled daily reminder work.
     * 3. Calculates the initial delay until the next occurrence of the desired time.
     * 4. If the initial delay is less than 2 minutes, schedules an immediate one-time notification.
     * 5. Creates and enqueues a periodic work request (with a 15-minute flex window) that repeats every 24 hours.
     *
     * The work is enqueued using a unique name, replacing any previous work with the same name.
     */
    fun scheduleDailyReminder() {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("notification_hour", 10)
        val minute = prefs.getInt("notification_minute", 0)

        // Cancel any existing daily reminder work.
        cancelDailyReminder()

        // Calculate the initial delay until the next notification time.
        val initialDelay = calculateInitialDelay(hour, minute)

        // If the delay is very short (less than 2 minutes), schedule an immediate one-time notification.
        if (initialDelay < TimeUnit.MINUTES.toMillis(2)) {
            scheduleImmediateNotification()
        }

        // Define constraints for the work; here, network is not required.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Create a periodic work request for DailyReminderWorker with a 24-hour interval and 15-minute flex interval.
        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        // Enqueue the periodic work uniquely, replacing any existing work with the same name.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyReminderRequest
        )
    }

    /**
     * Schedules an immediate one-time notification.
     *
     * This method creates a [OneTimeWorkRequest] for [DailyReminderWorker] with a 1-minute initial delay
     * and enqueues it. This is used when the calculated delay until the next notification is very short.
     */
    private fun scheduleImmediateNotification() {
        val immediateWork = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(immediateWork)
    }

    /**
     * Cancels the scheduled daily reminder notification.
     *
     * This method cancels any unique periodic work enqueued with the work name [DailyReminderWorker.WORK_NAME].
     */
    fun cancelDailyReminder() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(DailyReminderWorker.WORK_NAME)
    }

    /**
     * Calculates the initial delay until the next occurrence of a specified time.
     *
     * Given a target [hour] and [minute] (e.g. 10:00 AM), this method calculates the delay in milliseconds
     * between the current time and the next occurrence of that time. If the target time has already passed
     * today, it schedules the notification for the next day.
     *
     * @param hour The target hour (in 24-hour format) for the notification.
     * @param minute The target minute for the notification.
     * @return The delay in milliseconds until the next scheduled notification.
     */
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val currentTime = Calendar.getInstance()
        val desiredTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the desired time has already passed today, add one day to schedule for tomorrow.
        if (currentTime.after(desiredTime)) {
            desiredTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        return desiredTime.timeInMillis - currentTime.timeInMillis
    }

    companion object {
        /**
         * A constant representing the unique name for immediate work requests.
         */
        private const val IMMEDIATE_WORK_NAME = "immediate_notification_work"
    }
}
