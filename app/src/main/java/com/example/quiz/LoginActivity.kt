package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText

/**
 * LoginActivity handles user login functionality.
 *
 * Users can enter their email and password to authenticate via Firebase.
 * If the user is already logged in, they are redirected to the category selection screen.
 * This activity also implements a click listener for a registration link.
 *
 * Inherits from [BaseActivity] to share common functionality such as error display.
 */
class LoginActivity : BaseActivity(), View.OnClickListener {

    // UI Components for login input and button.
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null

    /**
     * Called when the activity is first created.
     *
     * Sets the content view, checks if the user is already authenticated,
     * initializes the input fields and login button, and sets click listeners for login
     * and registration navigation.
     *
     * @param savedInstanceState Contains the activity's previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // If the user is already authenticated, go directly to the CategorySelectionActivity.
        if (auth.currentUser != null) {
            startActivity(Intent(this, CategorySelectionActivity::class.java))
            finish()
            return
        }

        // Initialize UI components.
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        loginButton = findViewById(R.id.loginButton)

        // Set click listener for the login button.
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }

        // Set click listener for the registration link.
        findViewById<View>(R.id.registerTextViewClickable)?.setOnClickListener(this)
    }

    /**
     * Handles click events for views in this activity.
     *
     * Currently handles clicks on the registration link, navigating to [RegisterActivity].
     *
     * @param view The view that was clicked.
     */
    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.registerTextViewClickable -> {
                    // Navigate to the registration screen when the registration link is clicked.
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Validates the login credentials entered by the user.
     *
     * Checks that both email and password fields are non-empty.
     *
     * @return `true` if both email and password are provided; `false` otherwise.
     */
    private fun validateLoginDetails(): Boolean {
        val email = inputEmail?.text.toString().trim { it <= ' ' }
        val password = inputPassword?.text.toString().trim { it <= ' ' }

        return when {
            email.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> true
        }
    }

    /**
     * Attempts to sign in the registered user using Firebase Authentication.
     *
     * If the credentials are valid and sign-in is successful, the user is redirected to the
     * [CategorySelectionActivity] and the activity finishes. Otherwise, an error is displayed.
     */
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar("You are logged in successfully.", false)
                        startActivity(Intent(this, CategorySelectionActivity::class.java))
                        finish()
                    } else {
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }
}
