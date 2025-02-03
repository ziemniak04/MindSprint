package com.example.quiz

import QuestionData
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Removes duplicate quiz questions from the Firestore "questions" collection.
 * Two questions are considered duplicates if they have the same (trimmed)
 * question text, category, and correct answer.
 *
 * This function should be called from a coroutine scope, e.g., lifecycleScope in an Activity.
 *
 * Usage:
 * ```kotlin
 * lifecycleScope.launch {
 *     removeDuplicateQuestions()
 * }
 * ```
 */
suspend fun removeDuplicateQuestions() {
    val db = FirebaseFirestore.getInstance()

    /**
     * Part 1: Remove duplicate quiz questions.
     */
    try {
        // Fetch all questions from Firestore.
        val questionsSnapshot = db.collection("questions").get().await()
        val questions = questionsSnapshot.documents.mapNotNull { document ->
            // Convert each document into a QuestionData instance,
            // copying the document ID into the id property.
            document.toObject(QuestionData::class.java)?.copy(id = document.id)
        }
        Log.d("Cleanup", "Found ${questions.size} questions in total.")

        // Group questions by a key composed of trimmed questionText, category, and correctAnswer.
        val groupedQuestions = questions.groupBy { question ->
            Triple(
                question.questionText.trim(),
                question.category.trim(),
                question.correctAnswer.trim()
            )
        }

        // Collect the IDs of duplicate documents (keeping only the first document in each group).
        val duplicateQuestionIds = groupedQuestions.values.flatMap { group ->
            if (group.size > 1) {
                group.drop(1).map { it.id }
            } else {
                emptyList()
            }
        }
        Log.d("Cleanup", "Found ${duplicateQuestionIds.size} duplicate questions to remove.")

        // Delete each duplicate question.
        duplicateQuestionIds.forEach { docId ->
            db.collection("questions").document(docId).delete().await()
            Log.d("Cleanup", "Deleted duplicate question with id: $docId")
        }

        Log.d("Cleanup", "Questions cleanup complete. Removed ${duplicateQuestionIds.size} duplicates.")
    } catch (e: Exception) {
        Log.e("Cleanup", "Error while removing duplicate questions: ${e.message}", e)
    }

    /**
     * Part 2: Remove invalid quiz results (totalQuestions > 20).
     */
    try {
        val resultsSnapshot = db.collection("quiz_results")
            .whereGreaterThan("totalQuestions", 20)
            .get()
            .await()

        val invalidResults = resultsSnapshot.documents
        Log.d("Cleanup", "Found ${invalidResults.size} quiz_results with totalQuestions > 20.")

        invalidResults.forEach { document ->
            val total = document.getLong("totalQuestions")
            db.collection("quiz_results").document(document.id).delete().await()
            Log.d("Cleanup", "Deleted quiz_result with id: ${document.id} (totalQuestions = $total)")
        }
    } catch (e: Exception) {
        Log.e("Cleanup", "Error while removing invalid quiz_results: ${e.message}", e)
    }
}