package com.example.quiz

import QuestionData
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Handles Firestore operations related to quiz questions and quiz results.
 * Provides functionality to fetch, add, update, and delete questions, as well as manage quiz results.
 */
class QuizFirestore {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Retrieves a list of questions filtered by category from Firestore.
     * @param category The category of questions to fetch.
     * @return A list of [QuestionData] objects.
     */
    suspend fun getQuestionsByCategory(category: String): List<QuestionData> {
        return try {
            val snapshot = db.collection("questions")
                .whereEqualTo("category", category)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    QuestionData(
                        id = document.id,
                        questionText = document.getString("questionText") ?: return@mapNotNull null,
                        answerA = document.getString("answerA") ?: return@mapNotNull null,
                        answerB = document.getString("answerB") ?: return@mapNotNull null,
                        answerC = document.getString("answerC") ?: return@mapNotNull null,
                        answerD = document.getString("answerD") ?: return@mapNotNull null,
                        correctAnswer = document.getString("correctAnswer") ?: return@mapNotNull null,
                        category = document.getString("category") ?: return@mapNotNull null
                    )
                } catch (e: Exception) {
                    Log.e("QuizFirestore", "Error parsing question: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("QuizFirestore", "Error getting questions: ${e.message}")
            emptyList()
        }
    }

    /**
     * Adds a new question to Firestore.
     * @param question The question data to be added.
     */
    suspend fun addQuestion(question: QuestionData) {
        try {
            val questionMap = hashMapOf(
                "questionText" to question.questionText,
                "answerA" to question.answerA,
                "answerB" to question.answerB,
                "answerC" to question.answerC,
                "answerD" to question.answerD,
                "correctAnswer" to question.correctAnswer,
                "category" to question.category
            )
            db.collection("questions").add(questionMap).await()
        } catch (e: Exception) {
            Log.e("QuizFirestore", "Error adding question: ${e.message}")
            throw e
        }
    }

    /**
     * Saves the result of a completed quiz to Firestore.
     * @param result The quiz result data to be saved.
     */
    suspend fun saveQuizResult(result: QuizResult) {
        try {
            val resultMap = hashMapOf(
                "userId" to result.userId,
                "category" to result.category,
                "score" to result.score,
                "totalQuestions" to result.totalQuestions,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("quiz_results").add(resultMap).await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Retrieves quiz results for a specific user, ordered by timestamp.
     * @param userId The user ID to fetch quiz results for.
     * @return A list of [QuizResult] objects.
     */
    suspend fun getUserQuizResults(userId: String): List<QuizResult> {
        return try {
            val querySnapshot = db.collection("quiz_results")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                val timestampField = document.get("timestamp")
                val timestampDate = when (timestampField) {
                    is com.google.firebase.Timestamp -> timestampField.toDate()
                    is Number -> Date(timestampField.toLong())
                    else -> Date()
                }
                QuizResult(
                    userId = document.getString("userId") ?: return@mapNotNull null,
                    category = document.getString("category") ?: return@mapNotNull null,
                    score = document.getLong("score")?.toInt() ?: return@mapNotNull null,
                    totalQuestions = document.getLong("totalQuestions")?.toInt() ?: return@mapNotNull null,
                    timestamp = timestampDate
                )
            }
        } catch (e: Exception) {
            Log.e("QuizFirestore", "Error getting quiz results", e)
            emptyList()
        }
    }

    /**
     * Updates an existing question in Firestore.
     * @param id The document ID of the question.
     * @param question The updated question data.
     */
    suspend fun updateQuestion(id: String, question: QuestionData) {
        try {
            val questionMap = hashMapOf(
                "questionText" to question.questionText,
                "answerA" to question.answerA,
                "answerB" to question.answerB,
                "answerC" to question.answerC,
                "answerD" to question.answerD,
                "correctAnswer" to question.correctAnswer,
                "category" to question.category
            )
            db.collection("questions").document(id).update(questionMap as Map<String, Any>).await()
        } catch (e: Exception) {
            Log.e("QuizFirestore", "Error updating question: ${e.message}")
            throw e
        }
    }

    /**
     * Retrieves all quiz questions stored in Firestore.
     * @return A list of [QuestionData] objects.
     */
    suspend fun getAllQuestions(): List<QuestionData> {
        return try {
            val querySnapshot = db.collection("questions").get().await()
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(QuestionData::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Deletes a specific question from Firestore.
     * @param id The document ID of the question to delete.
     */
    suspend fun deleteQuestion(id: String) {
        try {
            db.collection("questions").document(id).delete().await()
        } catch (e: Exception) {
            Log.e("QuizFirestore", "Error deleting question: ${e.message}")
            throw e
        }
    }

    /**
     * Retrieves a single question by its document ID.
     * @param id The document ID of the question.
     * @return The retrieved [QuestionData] object, or null if not found.
     */
    suspend fun getQuestionById(id: String): QuestionData? {
        return try {
            val document = db.collection("questions").document(id).get().await()
            if (document.exists()) {
                QuestionData(
                    id = document.id,
                    questionText = document.getString("questionText") ?: return null,
                    answerA = document.getString("answerA") ?: return null,
                    answerB = document.getString("answerB") ?: return null,
                    answerC = document.getString("answerC") ?: return null,
                    answerD = document.getString("answerD") ?: return null,
                    correctAnswer = document.getString("correctAnswer") ?: return null,
                    category = document.getString("category") ?: return null
                )
            } else null
        } catch (e: Exception) {
            Log.e("QuizFirestore", "Error getting question: ${e.message}")
            null
        }
    }
}
