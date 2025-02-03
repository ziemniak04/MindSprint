package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import com.example.quiz.firebase.FirestoreClass
import com.example.quiz.firebase.User

/**
 * Handles user registration with input validation and Firebase Authentication integration.
 */
class RegisterActivity : BaseActivity() {

    // UI Components for the registration form
    private var registerButton: Button? = null
    private var inputEmail: EditText? = null
    private var inputName: EditText? = null
    private var inputPassword: EditText? = null
    private var inputPasswordRepeat: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize input fields and the registration button
        registerButton = findViewById(R.id.registerButton)
        inputEmail = findViewById(R.id.inputEmail)
        inputName = findViewById(R.id.inputName)
        inputPassword = findViewById(R.id.inputPasswordRegister)
        inputPasswordRepeat = findViewById(R.id.inputPasswordRepeat)

        // Set a click listener for the registration button
        registerButton?.setOnClickListener {
            registerUser()
        }
    }

    /**
     * Validates the registration details entered by the user.
     *
     * @return True if the details are valid, otherwise False.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            inputEmail?.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }
            inputName?.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_name), true)
                false
            }
            inputPassword?.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }
            inputPasswordRepeat?.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_reppassword), true)
                false
            }
            inputPassword?.text.toString().trim() != inputPasswordRepeat?.text.toString().trim() -> {
                showErrorSnackBar(getString(R.string.err_msg_password_mismatch), true)
                false
            }
            else -> true
        }
    }

    /**
     * Called when the "Already have an account? Login" view is clicked.
     * This method must be linked in the XML via android:onClick.
     */
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Closes this activity
    }

    /**
     * Registers the user using Firebase Authentication.
     */
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email = inputEmail?.text.toString().trim()
            val password = inputPassword?.text.toString().trim()
            val name = inputName?.text.toString().trim()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )

                        val user = User(
                            id = firebaseUser.uid,
                            name = name,
                            email = email,
                            phoneNumber = "", // Default empty
                            dateOfBirth = "", // Default empty
                            address = mapOf(), // Default empty map
                            interests = listOf(), // Default empty list
                            profilePictureUrl = "" // Default empty
                        )

                        // Launch coroutine and wait for Firestore update to complete before finishing.
                        lifecycleScope.launch {
                            try {
                                val firestoreClass = FirestoreClass()
                                firestoreClass.registerOrUpdateUser(user)
                                Toast.makeText(this@RegisterActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                                // Now sign out and finish
                                FirebaseAuth.getInstance().signOut()
                                finish()
                            } catch (e: Exception) {
                                Toast.makeText(this@RegisterActivity, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }
}
