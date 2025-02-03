package com.example.quiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/**
 * Base activity that provides authentication checking and UI utilities.
 * This activity ensures that only authenticated users can access certain screens.
 * It also provides a method for displaying snackbars with error or success messages.
 */

open class BaseActivity : AppCompatActivity() {
    protected val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * Called when the activity is first created.
     * @param savedInstanceState Contains the saved state of the activity, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Called when the activity becomes visible to the user.
     * Checks if the user is authenticated and redirects to the login screen if necessary.
     */
    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        if (auth.currentUser == null && this !is LoginActivity && this !is RegisterActivity) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Checks if the user is authenticated.
     * If not, redirects to the login screen.
     * @return `true` if the user is authenticated, `false` otherwise.
     */
    protected fun checkAuthentication(): Boolean {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return false
        }
        return true
    }

    /**
     *
     *
     * Displays a snackbar with a message, indicating success or error.
     * @param message The message to be displayed.
     * @param isError If `true`, the snackbar will have an error color, otherwise a success color.
     */
    protected fun showErrorSnackBar(message: String, isError: Boolean) {
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )

        val snackBarView = snackBar.view
        if (isError) {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorSnackBarError
                )
            )
        } else {
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorSnackBarSuccess
                )
            )
        }
        snackBar.show()
    }
}
