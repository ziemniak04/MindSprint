import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.quiz.CategorySelectionActivity
import com.example.quiz.QuizHistoryActivity
import com.example.quiz.R

/**
 * Helper class responsible for managing notifications in the quiz app.
 * It creates notification channels, sends daily reminders, and quiz completion notifications.
 *
 * @param context The application context required to interact with the system services.
 */
class NotificationHelper(private val context: Context) {

    // Access the system's notification manager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Unique identifier for the quiz notification channel
    private val channelId = "quiz_channel"

    /**
     * Initializes the NotificationHelper by creating the notification channel
     * if the device is running Android 8.0 (Oreo) or higher.
     */
    init {
        createNotificationChannel()
    }

    /**
     * Creates a notification channel for Android 8.0+ (API level 26+).
     * Channels allow users to control notification preferences.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Quiz Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for quiz results and reminders"
                enableLights(true) // Enable LED notification light
                lightColor = Color.BLUE // Set LED color to blue
                enableVibration(true) // Enable vibration for notifications
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canShowNotifications(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled() &&
                    notificationManager.notificationChannels.any { it.importance != NotificationManager.IMPORTANCE_NONE }
        } else {
            notificationManager.areNotificationsEnabled()
        }
    }

    /**
     * Displays a notification when the user completes a quiz.
     * @param score The number of correct answers.
     * @param total The total number of questions in the quiz.
     */
    fun showQuizCompletionNotification(score: Int, total: Int) {
        // Check if notifications are permitted (Android 13+ requires explicit permission)
        if (!areNotificationsEnabled()) {
            return
        }

        // Intent to open the Quiz History screen when the notification is tapped
        val intent = Intent(context, QuizHistoryActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build the quiz completion notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.button_art) // Ensure this drawable exists in the project
            .setContentTitle("Quiz Completed!")
            .setContentText("Great job! You scored $score out of $total")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Dismiss notification when tapped
            .setContentIntent(pendingIntent)
            .build()

        // Show the notification
        notificationManager.notify(QUIZ_COMPLETION_NOTIFICATION_ID, notification)
    }

    /**
     * Displays a daily reminder notification to encourage the user to take a quiz.
     */
    fun showDailyReminder() {
        // Check if notifications are permitted (Android 13+ requires explicit permission)
        if (!areNotificationsEnabled()) {
            return
        }

        // Intent to open the Category Selection screen when the notification is tapped
        val intent = Intent(context, CategorySelectionActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build the daily reminder notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.button_art)
            .setContentTitle("Time for Your Daily Quiz!")
            .setContentText("Challenge yourself with new questions today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Dismiss notification when tapped
            .setContentIntent(pendingIntent)
            .build()

        // Show the notification
        notificationManager.notify(DAILY_REMINDER_NOTIFICATION_ID, notification)
    }

    /**
     * Checks if the app has permission to send notifications.
     * Required for Android 13+ (API level 33+).
     *
     * @return `true` if the app has notification permission, `false` otherwise.
     */
    private fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // On older Android versions, permission is granted by default
        }
    }

    /**
     * Companion object to hold unique notification IDs.
     */
    companion object {
        const val DAILY_REMINDER_NOTIFICATION_ID = 100
        const val QUIZ_COMPLETION_NOTIFICATION_ID = 101
    }
}
