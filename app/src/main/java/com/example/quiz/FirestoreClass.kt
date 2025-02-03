package com.example.quiz.firebase

import android.util.Log
import com.example.quiz.QuizResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * A class that manages Firestore interactions related to user data and quiz results.
 */
class FirestoreClass {

    private val firestore = FirebaseFirestore.getInstance()
    private val mFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Retrieves a reference to a user's document in Firestore.
     * @param userId The ID of the user.
     * @return DocumentReference pointing to the user's document.
     */
    fun getUserDocumentReference(userId: String): DocumentReference {
        return firestore.collection("users").document(userId)
    }

    /**
     * Updates user data in Firestore.
     * @param userId The ID of the user.
     * @param updatedData A map of fields to update.
     * @param onSuccess Callback invoked on success.
     * @param onFailure Callback invoked on failure with error message.
     */
    fun updateUserData(userId: String, updatedData: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        mFireStore.collection("users")
            .document(userId)
            .update(updatedData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Unknown error")
            }
    }

    /**
     * Loads user data from Firestore.
     * @param userId The ID of the user.
     * @return A map of user data or null if retrieval fails.
     */
    suspend fun loadUserData(userId: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Registers or updates user data in Firestore.
     * @param user The user object to register or update.
     */
    suspend fun registerOrUpdateUser(user: User) {
        try {
            val userMap = hashMapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "dateOfBirth" to user.dateOfBirth,
                "address" to user.address,
                "interests" to user.interests,
                "profilePictureUrl" to user.profilePictureUrl
            )
            firestore.collection("users").document(user.id).set(userMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Saves a quiz result to Firestore.
     * @param userId The ID of the user.
     * @param quizId The ID of the quiz.
     * @param correctAnswers The number of correct answers.
     * @param incorrectAnswers The number of incorrect answers.
     * @param totalTime The total time taken to complete the quiz.
     * @return Boolean indicating success or failure.
     */
    suspend fun saveQuizResult(
        userId: String,
        quizId: String,
        correctAnswers: Int,
        incorrectAnswers: Int,
        totalTime: Long
    ): Boolean {
        return try {
            val quizResult = hashMapOf(
                "userId" to userId,
                "quizId" to quizId,
                "correctAnswers" to correctAnswers,
                "incorrectAnswers" to incorrectAnswers,
                "totalTime" to totalTime,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("quizzes")
                .add(quizResult)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Retrieves quiz results for a specific user.
     * @param userId The ID of the user.
     * @return A list of QuizResult objects.
     */
    suspend fun getUserQuizResults(userId: String): List<QuizResult> {
        return try {
            val querySnapshot = db.collection("quiz_results")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()
                QuizResult(
                    userId = document.getString("userId") ?: return@mapNotNull null,
                    category = document.getString("category") ?: return@mapNotNull null,
                    score = document.getLong("score")?.toInt() ?: return@mapNotNull null,
                    totalQuestions = document.getLong("totalQuestions")?.toInt() ?: return@mapNotNull null,
                    timestamp = timestamp
                )
            }
        } catch (e: Exception) {
            Log.e("QuizFirestore", "Error getting quiz results", e)
            emptyList()
        }
    }

    /**
     * Retrieves quiz results for a specific quiz ID.
     * @param quizId The ID of the quiz.
     * @return A list of quiz result maps.
     */
    suspend fun getQuizResultsByQuizId(quizId: String): List<Map<String, Any>> {
        return try {
            val querySnapshot = firestore.collection("quizzes")
                .whereEqualTo("quizId", quizId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
