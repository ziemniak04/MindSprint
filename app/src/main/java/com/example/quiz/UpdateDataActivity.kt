package com.example.quiz

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.quiz.firebase.FirestoreClass
import com.example.quiz.firebase.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * UpdateDataActivity allows the user to update their profile details in Firestore.
 *
 * This activity displays the current user information (such as name, email, phone, address,
 * interests, date of birth, and profile picture) and enables the user to update these details.
 * The activity also allows the user to change their profile picture by tapping the image, which
 * launches a gallery picker via the Activity Result API.
 */
class UpdateDataActivity : AppCompatActivity() {

    // UI components for updating user details.
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var dobButton: Button
    private lateinit var dobText: TextView

    /**
     * Holds the selected date of birth (as a formatted string, e.g. "2023-05-22").
     */
    private var selectedDateOfBirth: String = ""

    /**
     * Holds the new selected image URI if the user picks a new profile picture.
     */
    private var selectedImageUri: Uri? = null

    // Firebase Authentication and Firestore helper instances.
    private val auth = FirebaseAuth.getInstance()
    private val firestoreClass = FirestoreClass()

    /**
     * Launcher for picking an image from the gallery using ActivityResultContracts.GetContent.
     *
     * When an image is selected, its URI is stored in [selectedImageUri] and the profile image
     * is immediately updated.
     */
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImageView.setImageURI(it)
        }
    }

    /**
     * Called when the activity is created.
     *
     * Initializes the UI, sets up the date picker, listens for profile image clicks to allow the
     * user to change their profile picture, and listens for Firestore updates to populate the UI.
     *
     * @param savedInstanceState A [Bundle] containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initializeUI()
        setupDatePicker()

        // Allow the user to change the profile picture by tapping the ImageView.
        profileImageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val userId = auth.currentUser?.uid

        // Listen for changes to the user document in Firestore to update the UI.
        if (userId != null) {
            firestoreClass.getUserDocumentReference(userId).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = User.fromMap(snapshot.data!!)
                    populateUI(user)
                } else {
                    Log.d("Firestore", "No such document.")
                    Toast.makeText(this, "No user data found.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set the submit button listener to update the user data.
        submitButton.setOnClickListener {
            if (userId != null) {
                lifecycleScope.launch {
                    updateUserData(userId)
                }
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set the cancel button listener to finish the activity without changes.
        cancelButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Sets up the date picker by assigning a click listener to the [dobButton].
     */
    private fun setupDatePicker() {
        dobButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    /**
     * Displays a DatePickerDialog for the user to select their date of birth.
     *
     * Once a date is selected, [selectedDateOfBirth] is updated and [dobText] displays the chosen date.
     */
    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDateOfBirth = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                dobText.text = "Selected Date: $selectedDateOfBirth"
            },
            year,
            month,
            day
        )

        // Set the maximum selectable date to today.
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    /**
     * Handles menu item selections.
     *
     * When the up (home) button is selected, the activity calls [onBackPressed].
     *
     * @param item The selected [MenuItem].
     * @return True if the event is handled, otherwise calls the super implementation.
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
     * Initializes all UI components by finding them from the layout.
     */
    private fun initializeUI() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        addressInput = findViewById(R.id.addressInput)
        interestsInput = findViewById(R.id.interestsInput)
        submitButton = findViewById(R.id.submitButton)
        cancelButton = findViewById(R.id.cancelButton)
        profileImageView = findViewById(R.id.profileImageView)
        dobButton = findViewById(R.id.dobButton)
        dobText = findViewById(R.id.dobText)
    }

    /**
     * Populates the UI fields with the current user data retrieved from Firestore.
     *
     * Sets the text for name, email, phone, address, interests, and date of birth.
     * If no new profile picture is selected, the stored profile picture URL is loaded using Glide.
     *
     * @param user The [User] object containing the user's current details.
     */
    private fun populateUI(user: User) {
        nameInput.setText(user.name ?: "")
        emailInput.setText(user.email)
        phoneInput.setText(user.phoneNumber)
        val address = user.address.values.joinToString(", ")
        addressInput.setText(address)
        interestsInput.setText(user.interests.joinToString(", "))
        if (user.dateOfBirth.isNotEmpty()) {
            dobText.text = "Selected Date: ${user.dateOfBirth}"
            selectedDateOfBirth = user.dateOfBirth
        }

        // Load the profile picture. If no new image is selected, use the stored URL.
        if (selectedImageUri == null) {
            if (user.profilePictureUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(user.profilePictureUrl))
                    .placeholder(R.drawable.default_profile)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile)
            }
        }
    }

    /**
     * Collects updated data from the UI and updates the Firestore user document.
     *
     * The method builds a map of updated fields. If the address is in the format of three comma‑separated
     * values (city, street, postcode), it is converted into a structured map. If a new profile picture
     * has been selected, its URI string is included. On success, a success message is shown, the activity's
     * result is set, and the activity finishes.
     *
     * @param userId The unique identifier of the current user.
     */
    private suspend fun updateUserData(userId: String) {
        // Parse the address input into parts and create a map if exactly three parts exist.
        val addressParts = addressInput.text.toString().split(",").map { it.trim() }
        val addressMap = if (addressParts.size == 3) {
            mapOf(
                "city" to addressParts[0],
                "street" to addressParts[1],
                "postcode" to addressParts[2]
            )
        } else {
            mapOf()
        }

        // Build the updated data map.
        val updatedData = mutableMapOf<String, Any>(
            "name" to nameInput.text.toString(),
            "email" to emailInput.text.toString(),
            "phoneNumber" to phoneInput.text.toString(),
            "dateOfBirth" to selectedDateOfBirth,
            "address" to addressMap,
            "interests" to interestsInput.text.toString().split(",").map { it.trim() }
        )

        // If a new profile picture was selected, add it to the updated data.
        if (selectedImageUri != null) {
            updatedData["profilePictureUrl"] = selectedImageUri.toString()
        }

        try {
            firestoreClass.updateUserData(
                userId,
                updatedData,
                onSuccess = {
                    runOnUiThread {
                        Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                },
                onFailure = { errorMessage ->
                    runOnUiThread {
                        Toast.makeText(this, "Failed to update data: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to update data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
