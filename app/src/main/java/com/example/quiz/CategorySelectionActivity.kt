package com.example.quiz

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity for selecting a quiz category.
 * This activity allows users to choose a quiz category and navigate to the quiz screen.
 * It also provides authentication checks and menu options for navigation.
 */
class CategorySelectionActivity : AppCompatActivity() {

    private lateinit var artButton: Button
    private lateinit var musicButton: Button
    private lateinit var olympicButton: Button
    private lateinit var historyButton: Button
    private lateinit var animalsButton: Button
    private lateinit var travelButton: Button
    private lateinit var technicButton: Button
    private lateinit var videoButton: Button
    private lateinit var geoButton: Button
    private lateinit var auth: FirebaseAuth

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets button click listeners, and ensures the user is authenticated.
     * @param savedInstanceState Contains the saved state of the activity, if available.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        supportActionBar?.apply {
            title = "Quiz Categories"
            subtitle = auth.currentUser?.email
        }

        // Initialize category buttons
        artButton = findViewById(R.id.artButton)
        musicButton = findViewById(R.id.musicButton)
        olympicButton = findViewById(R.id.olympicButton)
        historyButton = findViewById(R.id.historyButton)
        animalsButton = findViewById(R.id.animalsButton)
        travelButton = findViewById(R.id.travelButton)
        technicButton = findViewById(R.id.technicButton)
        videoButton = findViewById(R.id.videoButton)
        geoButton = findViewById(R.id.geoButton)

        // Set click listeners for category buttons
        artButton.setOnClickListener { goToQuestionsActivity("Art") }
        musicButton.setOnClickListener { goToQuestionsActivity("Music") }
        olympicButton.setOnClickListener { goToQuestionsActivity("Olympic") }
        historyButton.setOnClickListener { goToQuestionsActivity("History") }
        animalsButton.setOnClickListener { goToQuestionsActivity("Animals") }
        travelButton.setOnClickListener { goToQuestionsActivity("Travel") }
        technicButton.setOnClickListener { goToQuestionsActivity("IT") }
        videoButton.setOnClickListener { goToQuestionsActivity("Video Games") }
        geoButton.setOnClickListener { goToQuestionsActivity("Geography") }

        // Optionally, upload initial questions if needed
        val uploader = QuizDataUploader()
        uploader.uploadInitialQuestions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                goToSplashActivity()
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, UpdateDataActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, NotificationSettingsActivity::class.java))
                true
            }
            R.id.action_quiz_history -> {
                startActivity(Intent(this, QuizHistoryActivity::class.java))
                true
            }
            R.id.action_add_question -> {
                startActivity(Intent(this, AllQuestionsActivity::class.java))
                true
            }
            R.id.action_sign_out -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    /**
     * Handles back button press by navigating to the SplashActivity.
     */
    override fun onBackPressed() {
        goToSplashActivity()
    }
    /**
     * Navigates to the SplashActivity and clears the activity stack.
     */
    private fun goToSplashActivity() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
    /**
     * Displays a confirmation dialog and logs the user out if confirmed.
     */
    private fun signOut() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Starts the QuizActivity with the selected category.
     * @param category The category of the quiz to be started.
     */
    private fun goToQuestionsActivity(category: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("CATEGORY", category)
        startActivity(intent)
    }
}