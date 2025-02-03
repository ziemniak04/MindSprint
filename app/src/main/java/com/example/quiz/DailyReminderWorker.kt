package com.example.quiz

import NotificationHelper
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker class responsible for handling daily reminder notifications.
 *
 * This worker is scheduled to run daily and triggers a notification
 * to remind users about quiz-related activities.
 *
 * @property notificationHelper Helper class to manage notifications.
 */
class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val notificationHelper = NotificationHelper(context)

    /**
     * Executes the worker task to show a daily reminder notification.
     *
     * @return Result indicating the success or failure of the work execution.
     */
    override fun doWork(): Result {
        Log.d("DailyReminderWorker", "Worker executing at ${System.currentTimeMillis()}")
        notificationHelper.showDailyReminder()
        return Result.success()
    }

    companion object {
        /**
         * Unique name used to identify the daily reminder work request.
         */
        const val WORK_NAME = "daily_reminder_work"
    }
}
