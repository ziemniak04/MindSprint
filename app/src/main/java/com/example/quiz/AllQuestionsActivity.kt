package com.example.quiz

import QuestionData
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Activity that displays a list of all quiz questions.
 * This activity fetches questions from Firestore and displays them in a RecyclerView.
 * Users can navigate to the question editing screen or add new questions.
 */
class AllQuestionsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuestionListAdapter
    private val quizFirestore = QuizFirestore()
    private val auth = FirebaseAuth.getInstance()
    /**
     * Called when the activity is first created.
     * Initializes the UI components and loads the list of questions.
     * @param savedInstanceState Contains the saved state of the activity, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_questions)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        loadQuestions()
    }
    /**
     * Sets up the RecyclerView to display the list of questions.
     * Configures the layout manager and adapter.
     */
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.questionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = QuestionListAdapter { question -> navigateToEdit(question) }
        recyclerView.adapter = adapter
    }
    /**
     * Loads quiz questions from Firestore and updates the RecyclerView.
     * Displays a toast message if loading fails.
     */
    private fun loadQuestions() {
        lifecycleScope.launch {
            try {
                val questions = quizFirestore.getAllQuestions()
                adapter.submitList(questions)
            } catch (e: Exception) {
                Toast.makeText(this@AllQuestionsActivity, "Failed to load questions: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    /**
     * Navigates to the ManageQuizActivity for editing a selected question.
     * @param question The selected question to be edited.
     */
    private fun navigateToEdit(question: QuestionData) {
        val intent = Intent(this, ManageQuizActivity::class.java).apply {
            putExtra("questionId", question.id)
        }
        startActivity(intent)
    }
    /**
     * Inflates the options menu for the activity.
     * @param menu The menu to be inflated.
     * @return True if the menu is successfully created.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.questions_menu, menu)
        return true
    }
    /**
     * Handles menu item selections.
     * @param item The selected menu item.
     * @return True if the item was handled, otherwise calls the superclass implementation.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_question -> {
                startActivity(Intent(this, ManageQuizActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}