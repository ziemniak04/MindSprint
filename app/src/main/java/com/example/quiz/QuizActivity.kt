package com.example.quiz

import QuestionData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Activity handling the quiz functionality.
 * This activity retrieves quiz questions based on the selected category,
 * manages the quiz flow, evaluates user answers, and saves quiz results.
 */
class QuizActivity : AppCompatActivity() {
    private lateinit var questions: List<QuestionData>
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private val quizFirestore = QuizFirestore()
    private lateinit var category: String
    private val auth = FirebaseAuth.getInstance()

    /**
     * Called when the activity is first created.
     * Retrieves the quiz category, fetches questions, and starts the quiz.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        category = intent.getStringExtra("CATEGORY").orEmpty().trim().takeIf { it.isNotEmpty() } ?: "Art"
        Log.d("CATEGORY_FLOW", "Quiz started with category: $category")

        // Fetch questions from Firestore
        lifecycleScope.launch {
            questions = getQuestionsByCategory(category)
            if (questions.isNotEmpty()) {
                displayQuestion()
            } else {
                Toast.makeText(this@QuizActivity, "No questions available for this category", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * Fetches quiz questions based on the selected category from Firestore.
     * Returns a shuffled list of up to 20 questions.
     *
     * @param category The category of questions to fetch.
     * @return A list of shuffled questions or an empty list if none are available.
     */
    private suspend fun getQuestionsByCategory(category: String): List<QuestionData> {
        return try {
            val allQuestions = quizFirestore.getQuestionsByCategory(category)
            if (allQuestions.isEmpty()) {
                Toast.makeText(this, "No questions found for $category", Toast.LENGTH_LONG).show()
                finish()
            }
            allQuestions.shuffled().take(20)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            emptyList()
        }
    }

    /**
     * Displays the current question and its possible answers.
     * Updates UI elements accordingly.
     */
    private fun displayQuestion() {
        val questionTextView: TextView = findViewById(R.id.questionText)
        val answerButton1: Button = findViewById(R.id.answerButton1)
        val answerButton2: Button = findViewById(R.id.answerButton2)
        val answerButton3: Button = findViewById(R.id.answerButton3)
        val answerButton4: Button = findViewById(R.id.answerButton4)

        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]

            questionTextView.text = question.questionText
            val options = question.options
            answerButton1.text = options[0]
            answerButton2.text = options[1]
            answerButton3.text = options[2]
            answerButton4.text = options[3]

            findViewById<TextView>(R.id.progressText).text = "Question ${currentQuestionIndex + 1} of ${questions.size}"

            setupAnswerButtons(answerButton1, answerButton2, answerButton3, answerButton4, question.correctAnswer)
        } else {
            // End of quiz
            saveQuizResults()
        }
    }

    /**
     * Sets up click listeners for answer buttons.
     * Evaluates if the selected answer is correct and proceeds to the next question.
     *
     * @param button1 The first answer button.
     * @param button2 The second answer button.
     * @param button3 The third answer button.
     * @param button4 The fourth answer button.
     * @param correctAnswer The correct answer for the current question.
     */
    private fun setupAnswerButtons(button1: Button, button2: Button, button3: Button, button4: Button, correctAnswer: String) {
        val buttons = listOf(button1, button2, button3, button4)

        buttons.forEach { button ->
            button.setOnClickListener {
                if (button.text == correctAnswer) {
                    correctAnswers++
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show()
                }

                // Move to the next question
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    displayQuestion()
                } else {
                    saveQuizResults()
                }
            }
        }
    }

    /**
     * Saves quiz results to Firestore and navigates to the summary screen.
     */
    private fun saveQuizResults() {
        lifecycleScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val quizResult = QuizResult(
                    userId = userId,
                    category = category,
                    score = correctAnswers,
                    totalQuestions = questions.size,
                    timestamp = Date()
                )
                quizFirestore.saveQuizResult(quizResult)

                // Navigate to summary screen
                val intent = Intent(this@QuizActivity, QuizSummaryActivity::class.java).apply {
                    putExtra("CORRECT_ANSWERS", correctAnswers)
                    putExtra("TOTAL_QUESTIONS", questions.size)
                    putExtra("CATEGORY", category)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@QuizActivity,
                    "Error saving results: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
