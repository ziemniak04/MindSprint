package com.example.quiz

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * NotificationSettingsActivity allows the user to configure daily notification settings.
 *
 * This activity enables users to:
 * - Enable or disable daily reminders.
 * - Change the daily notification time via a time picker.
 * - Request and handle notification permission (required for Android 13 and above).
 * - Open the system settings to adjust notification permissions if needed.
 *
 * The activity saves the user's settings in SharedPreferences under the "notification_prefs" file.
 */
class NotificationSettingsActivity : AppCompatActivity() {

    // Instance of NotificationManager used to schedule/cancel daily reminders.
    private lateinit var notificationManager: NotificationManager

    // Switch widget to enable/disable daily reminders.
    private lateinit var switchDailyReminder: SwitchCompat

    /**
     * Launcher to request the POST_NOTIFICATIONS permission (for Android 13+).
     *
     * When the permission is requested, if granted, daily reminders will be enabled;
     * if denied, the switch is turned off and the user is prompted to enable permission manually.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableDailyReminder()
        } else {
            switchDailyReminder.isChecked = false
            showPermissionDeniedDialog()
        }
    }

    /**
     * Called when the activity is first created.
     *
     * Sets the content view, configures the toolbar, initializes the notification manager,
     * sets up UI event listeners, and loads the current notification settings.
     *
     * @param savedInstanceState A [Bundle] containing the activity's previously saved state (if any).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        // Set up the toolbar with a back button and title.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Notification Settings"

        // Initialize the NotificationManager.
        notificationManager = NotificationManager(this)

        // Set up UI components and event listeners.
        setupViews()

        // Load the current notification settings from SharedPreferences.
        loadSettings()
    }

    /**
     * Handles the selection of items in the options menu.
     *
     * If the home/up button is pressed, this method calls [onBackPressed].
     *
     * @param item The selected [MenuItem].
     * @return True if the event is handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Initializes the UI components and sets click listeners for them.
     *
     * - Sets a change listener on the daily reminder switch to request permission or disable reminders.
     * - Sets a click listener on the "Change Time" button to show a time picker dialog.
     */
    private fun setupViews() {
        switchDailyReminder = findViewById(R.id.switchDailyReminder)
        switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestNotificationPermissionIfNeeded()
            } else {
                disableDailyReminder()
            }
        }

        // Set a click listener on the "Change Time" button to show the time picker dialog.
        findViewById<View>(R.id.btnChangeTime)?.setOnClickListener {
            showTimePickerDialog()
        }
    }

    /**
     * Loads notification settings from SharedPreferences and updates the UI accordingly.
     *
     * Retrieves the state of daily reminders and the notification time (hour and minute),
     * then updates the switch and the time display.
     */
    private fun loadSettings() {
        val prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE)
        switchDailyReminder.isChecked = prefs.getBoolean("daily_reminder_enabled", false)

        val notificationHour = prefs.getInt("notification_hour", 10)
        val notificationMinute = prefs.getInt("notification_minute", 0)
        updateTimeDisplay(notificationHour, notificationMinute)
    }

    /**
     * Checks if notification permission is needed and requests it if required (Android 13+).
     *
     * For Android versions below TIRAMISU, it directly enables daily reminders.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    enableDailyReminder()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationaleDialog()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            enableDailyReminder()
        }
    }

    /**
     * Enables daily reminders by scheduling them and saving the preference.
     *
     * This method calls [notificationManager.scheduleDailyReminder] to schedule the work,
     * then updates SharedPreferences and displays a success message.
     */
    private fun enableDailyReminder() {
        try {
            notificationManager.scheduleDailyReminder()
            getSharedPreferences("notification_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("daily_reminder_enabled", true)
                .apply()
            showSuccessMessage("Daily reminders enabled")
        } catch (e: Exception) {
            showErrorMessage("Failed to enable reminders")
            switchDailyReminder.isChecked = false
        }
    }

    /**
     * Disables daily reminders by canceling them and updating the saved preference.
     *
     * Calls [notificationManager.cancelDailyReminder] and updates SharedPreferences,
     * then shows a success message. In case of an error, the switch is reset.
     */
    private fun disableDailyReminder() {
        try {
            notificationManager.cancelDailyReminder()
            getSharedPreferences("notification_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("daily_reminder_enabled", false)
                .apply()
            showSuccessMessage("Daily reminders disabled")
        } catch (e: Exception) {
            showErrorMessage("Failed to disable reminders")
            switchDailyReminder.isChecked = true
        }
    }

    /**
     * Displays a time picker dialog for selecting the notification time.
     *
     * Retrieves the current notification time from SharedPreferences and displays a dialog.
     * When a new time is selected, [updateNotificationTime] is called.
     */
    private fun showTimePickerDialog() {
        val prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE)
        val currentHour = prefs.getInt("notification_hour", 10)
        val currentMinute = prefs.getInt("notification_minute", 0)

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                updateNotificationTime(hourOfDay, minute)
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    /**
     * Updates the stored notification time and reschedules reminders if necessary.
     *
     * Saves the new hour and minute to SharedPreferences, updates the time display, and if daily
     * reminders are enabled, re-schedules them with the new time.
     *
     * @param hour The newly selected hour (in 24-hour format).
     * @param minute The newly selected minute.
     */
    private fun updateNotificationTime(hour: Int, minute: Int) {
        getSharedPreferences("notification_prefs", MODE_PRIVATE)
            .edit()
            .putInt("notification_hour", hour)
            .putInt("notification_minute", minute)
            .apply()

        updateTimeDisplay(hour, minute)

        if (switchDailyReminder.isChecked) {
            notificationManager.scheduleDailyReminder()
        }
    }

    /**
     * Updates the UI display to show the selected notification time.
     *
     * Formats the hour and minute as "HH:mm" and sets the text of the TextView with id [tvNotificationTime].
     *
     * @param hour The hour to display.
     * @param minute The minute to display.
     */
    private fun updateTimeDisplay(hour: Int, minute: Int) {
        val timeFormat = String.format("%02d:%02d", hour, minute)
        findViewById<TextView>(R.id.tvNotificationTime)?.text =
            "Notification time: $timeFormat"
    }

    /**
     * Displays a dialog explaining why notification permission is needed.
     *
     * The dialog asks the user whether they would like to enable notification permission,
     * and if so, requests the permission via the [requestPermissionLauncher].
     */
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission")
            .setMessage("Daily reminders require notification permission. Would you like to enable it?")
            .setPositiveButton("Yes") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("No") { _, _ ->
                switchDailyReminder.isChecked = false
            }
            .show()
    }

    /**
     * Checks whether the application is ignoring battery optimizations.
     *
     * This method returns true if the system is not restricting the app's background work due to battery optimizations.
     *
     * @return True if battery optimizations are ignored, false otherwise.
     */
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    /**
     * Displays a dialog when the notification permission is denied.
     *
     * The dialog informs the user that notifications are required for daily reminders
     * and provides a button to open the system settings so they can manually enable permission.
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Notifications are required for daily reminders. Please enable them in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                switchDailyReminder.isChecked = false
            }
            .show()
    }

    /**
     * Opens the Android application details settings screen.
     *
     * This allows the user to manually enable notification permissions and adjust other settings for the app.
     */
    private fun openNotificationSettings() {
        Intent().also { intent ->
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        }
    }

    /**
     * Displays a success message using a Snackbar.
     *
     * @param message The message to be displayed.
     */
    private fun showSuccessMessage(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    /**
     * Displays an error message using a Snackbar with a "Retry" action.
     *
     * When the "Retry" action is pressed, the state of the daily reminder switch is toggled.
     *
     * @param message The error message to display.
     */
    private fun showErrorMessage(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).setAction("Retry") {
            switchDailyReminder.isChecked = !switchDailyReminder.isChecked
        }.show()
    }

    /**
     * Handles the action bar's up (back) button press.
     *
     * This method simply calls [onBackPressed] to return to the previous activity.
     *
     * @return True after handling the up button press.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
