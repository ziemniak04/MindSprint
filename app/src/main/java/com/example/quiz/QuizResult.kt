package com.example.quiz
import java.util.Date

/**
 * Data class representing the result of a quiz attempt.
 * This class stores information about a user's quiz session,
 * including their score, total number of questions, category, and timestamp.
 * @property userId Unique identifier for the user who completed the quiz.
 * @property category The category of the quiz, matching Firestore field names.
 * @property score The user's score for the quiz attempt, matching Firestore field names.
 * @property totalQuestions The total number of questions in the quiz.
 * @property timestamp The timestamp when the quiz was completed.
 */
data class QuizResult(
    val userId: String = "",
    val category: String = "",  // Must match Firestore field name
    val score: Int = 0,         // Must match Firestore field name
    val totalQuestions: Int = 0,
    val timestamp: Date = Date()
)