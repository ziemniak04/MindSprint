package com.example.quiz

import QuestionData
import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity responsible for managing quiz questions.
 * Users can add, update, or delete quiz questions.
 */
class ManageQuizActivity : AppCompatActivity() {

    // UI components for quiz question management
    private lateinit var questionInput: EditText
    private lateinit var option1Input: EditText
    private lateinit var option2Input: EditText
    private lateinit var option3Input: EditText
    private lateinit var option4Input: EditText
    private lateinit var correctAnswerInput: EditText
    private lateinit var categoryInput: EditText
    private lateinit var addButton: Button
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button

    // Firestore database instance
    private val db = FirebaseFirestore.getInstance()

    // Holds the ID of the current question (if updating or deleting)
    private var currentQuestionId: String? = null

    /**
     * Initializes the activity, sets up UI components, and checks for edit mode.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_quiz)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize UI components
        questionInput = findViewById(R.id.questionInput)
        option1Input = findViewById(R.id.option1Input)
        option2Input = findViewById(R.id.option2Input)
        option3Input = findViewById(R.id.option3Input)
        option4Input = findViewById(R.id.option4Input)
        correctAnswerInput = findViewById(R.id.correctAnswerInput)
        categoryInput = findViewById(R.id.categoryInput)
        addButton = findViewById(R.id.addButton)
        updateButton = findViewById(R.id.updateButton)
        deleteButton = findViewById(R.id.deleteButton)

        // Check if the activity is opened in edit mode
        currentQuestionId = intent.getStringExtra("questionId")
        if (currentQuestionId != null) {
            // Hide "Add" button, show "Update" and "Delete" buttons
            addButton.visibility = View.GONE
            updateButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE

            // Load the existing question details
            loadQuestionData(currentQuestionId!!)
        }

        // Set button click listeners
        addButton.setOnClickListener { addQuestion() }
        updateButton.setOnClickListener { updateQuestion() }
        deleteButton.setOnClickListener { deleteQuestion() }
    }

    /**
     * Handles the action bar's back button to return to the previous screen.
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
     * Adds a new question to Firestore after validating the input fields.
     */
    private fun addQuestion() {
        if (!validateInputs()) return

        val question = QuestionData(
            questionText = questionInput.text.toString(),
            answerA = option1Input.text.toString(),
            answerB = option2Input.text.toString(),
            answerC = option3Input.text.toString(),
            answerD = option4Input.text.toString(),
            correctAnswer = correctAnswerInput.text.toString(),
            category = categoryInput.text.toString()
        )

        lifecycleScope.launch {
            try {
                db.collection("questions").add(question).await()
                Toast.makeText(this@ManageQuizActivity, "Question added successfully", Toast.LENGTH_SHORT).show()
                clearInputs()
            } catch (e: Exception) {
                Toast.makeText(this@ManageQuizActivity, "Error adding question: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Updates an existing question in Firestore after validating inputs.
     */
    private fun updateQuestion() {
        if (!validateInputs()) return

        val updatedQuestion = QuestionData(
            id = currentQuestionId!!,
            questionText = questionInput.text.toString(),
            answerA = option1Input.text.toString(),
            answerB = option2Input.text.toString(),
            answerC = option3Input.text.toString(),
            answerD = option4Input.text.toString(),
            correctAnswer = correctAnswerInput.text.toString(),
            category = categoryInput.text.toString()
        )

        lifecycleScope.launch {
            try {
                db.collection("questions").document(currentQuestionId!!).set(updatedQuestion).await()
                Toast.makeText(this@ManageQuizActivity, "Question updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ManageQuizActivity, "Error updating question: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Deletes a question from Firestore after user confirmation.
     */
    private fun deleteQuestion() {
        AlertDialog.Builder(this)
            .setTitle("Delete Question")
            .setMessage("Are you sure you want to delete this question?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    try {
                        db.collection("questions").document(currentQuestionId!!).delete().await()
                        Toast.makeText(this@ManageQuizActivity, "Question deleted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@ManageQuizActivity, "Error deleting question: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Loads an existing question's data from Firestore.
     *
     * @param questionId The ID of the question to load.
     */
    private fun loadQuestionData(questionId: String) {
        lifecycleScope.launch {
            try {
                val document = db.collection("questions").document(questionId).get().await()
                if (document.exists()) {
                    val question = document.toObject(QuestionData::class.java)
                    question?.let {
                        questionInput.setText(it.questionText)
                        option1Input.setText(it.answerA)
                        option2Input.setText(it.answerB)
                        option3Input.setText(it.answerC)
                        option4Input.setText(it.answerD)
                        correctAnswerInput.setText(it.correctAnswer)
                        categoryInput.setText(it.category)
                    }
                } else {
                    Toast.makeText(this@ManageQuizActivity, "Question not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ManageQuizActivity, "Error loading question: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Validates user inputs to ensure all fields are filled.
     *
     * @return `true` if all fields are valid, `false` otherwise.
     */
    private fun validateInputs(): Boolean {
        if (questionInput.text.isBlank() ||
            option1Input.text.isBlank() ||
            option2Input.text.isBlank() ||
            option3Input.text.isBlank() ||
            option4Input.text.isBlank() ||
            correctAnswerInput.text.isBlank() ||
            categoryInput.text.isBlank()
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * Clears all input fields after a question is added successfully.
     */
    private fun clearInputs() {
        questionInput.text.clear()
        option1Input.text.clear()
        option2Input.text.clear()
        option3Input.text.clear()
        option4Input.text.clear()
        correctAnswerInput.text.clear()
        categoryInput.text.clear()
    }
}
