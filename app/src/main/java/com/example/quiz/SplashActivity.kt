package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * SplashActivity is the entry point of the application.
 *
 * This activity checks the current authentication state using FirebaseAuth and configures the UI accordingly.
 * Depending on whether the user is already logged in or not, it displays appropriate navigation buttons.
 * It also provides navigation to the Leaderboard, Login, Category Selection, and Registration screens.
 *
 * Additionally, this activity optionally invokes [removeDuplicateQuestions] (if implemented elsewhere)
 * to clean up any duplicate quiz data on startup.
 */
class SplashActivity : AppCompatActivity() {

    // FirebaseAuth instance to manage user authentication.
    private lateinit var auth: FirebaseAuth

    /**
     * Called when the activity is first created.
     *
     * This method sets up the layout, initializes Firebase authentication, and calls [setupUI] to
     * configure the user interface.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied; otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()
        setupUI()
    }

    /**
     * Configures the UI elements and sets click listeners for navigation.
     *
     * - If a user is already authenticated, the Login and Sign Up buttons are hidden and the Explore button is shown.
     * - Otherwise, the Login and Sign Up buttons are visible while the Explore button is hidden.
     * - The Leaderboard button navigates to [LeaderboardActivity].
     * - The Login button navigates to [LoginActivity].
     * - The Explore button navigates to [CategorySelectionActivity]. (If you wish to keep SplashActivity in the back stack,
     *   do not call finish() after launching CategorySelectionActivity.)
     * - The Sign Up button navigates to [RegisterActivity].
     * - Optionally, the function [removeDuplicateQuestions] is launched to remove any duplicate questions.
     */
    private fun setupUI() {
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val exploreButton = findViewById<Button>(R.id.btnExplore)
        val signUpButton = findViewById<TextView>(R.id.btnSignUp)
        val leaderboardButton = findViewById<Button>(R.id.btnLeaderboard)

        // Set click listener to navigate to the Leaderboard screen.
        leaderboardButton.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        // Check the user's authentication status and update button visibility.
        if (auth.currentUser != null) {
            loginButton.visibility = View.GONE
            signUpButton.visibility = View.GONE
            exploreButton.visibility = View.VISIBLE
        } else {
            loginButton.visibility = View.VISIBLE
            signUpButton.visibility = View.VISIBLE
            exploreButton.visibility = View.GONE
        }

        // Set click listener for the Login button.
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Set click listener for the Explore button.
        exploreButton.setOnClickListener {
            // If you want to allow going back to the splash screen, do not finish() here.
            startActivity(Intent(this, CategorySelectionActivity::class.java))
            // Uncomment the line below if you want to remove SplashActivity from the back stack:
            // finish()
        }

        // Set click listener for the Sign Up text view.
        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Optionally, remove duplicate questions on startup (assuming removeDuplicateQuestions() is defined elsewhere).
        lifecycleScope.launch {
            removeDuplicateQuestions()
        }
    }

    /**
     * Called when the activity becomes visible to the user.
     *
     * This method can be used to refresh or update the UI state if needed.
     */
    override fun onStart() {
        super.onStart()
        // You can refresh UI state here if necessary.
    }
}
