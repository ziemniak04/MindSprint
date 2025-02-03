package com.example.quiz
/**
 * Data class representing an entry in the quiz leaderboard.
 *
 * This class stores information about a user's ranking in the leaderboard,
 * including their ID, name, and total score.
 *
 * @property userId Unique identifier for the user.
 * @property userName Name of the user displayed in the leaderboard.
 * @property totalScore Total score accumulated by the user.
 */
data class LeaderboardEntry(
    val userId: String,
    val userName: String,
    val totalScore: Int
)
