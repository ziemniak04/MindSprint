package com.example.quiz

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Class responsible for uploading initial quiz questions to Firestore.
 * This class provides a method to upload predefined quiz questions into the Firestore database.
 * Currently, the implementation is commented out, but it can be enabled when needed.
 */
class QuizDataUploader {
    private val quizFirestore = QuizFirestore()

    /**
     * Uploads predefined questions for different quiz categories to Firestore.
     *
     * This function iterates over the predefined question sets from different categories
     * and uploads them to Firestore using the `QuizFirestore` class.
     *
     * Note: The implementation is currently commented out to prevent accidental data duplication.
     * Uncomment the relevant lines when needed.
     */
    fun uploadInitialQuestions() {
        // List of quiz categories and their respective question sources
        // "Art" -> ArtQuestions().getQuestions()
        // "Olympic" -> OlympicQuestions().getQuestions()
        // "Music" -> MusicQuestions().getQuestions()
        // "History" -> HistoryQuestions().getQuestions()
        // "Animals" -> AnimalQuestions().getQuestions()
        // "Travel" -> TravelQuestions().getQuestions()
        // "IT" -> TechQuestions().getQuestions()
        // "Video Games" -> GamingQuestions().getQuestions()
        // "Geography" -> GeographyQuestions().getQuestions()

        // Launches a coroutine to upload questions asynchronously
        // CoroutineScope(Dispatchers.IO).launch {

        // Upload Art questions
        // ArtQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload Music questions
        // MusicQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload Olympic questions
        // OlympicQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload History questions
        // HistoryQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload Animals questions
        // AnimalQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload Travel questions
        // TravelQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload IT questions
        // TechQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload Video Games questions
        // GamingQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // Upload Geography questions
        // GeographyQuestions().getQuestions().forEach { question ->
        //    try {
        //        quizFirestore.addQuestion(question)
        //    } catch (e: Exception) {
        //        // Handle error
        //    }
        // }

        // }
    }
}
