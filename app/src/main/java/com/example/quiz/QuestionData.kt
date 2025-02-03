/**
 * Data class representing a quiz question.
 * This class stores information about a quiz question, including its text,
 * possible answers, the correct answer, and its category.
 * @property id Unique identifier for the question.
 * @property questionText The text of the quiz question.
 * @property answerA First possible answer choice.
 * @property answerB Second possible answer choice.
 * @property answerC Third possible answer choice.
 * @property answerD Fourth possible answer choice.
 * @property correctAnswer The correct answer among the provided options.
 * @property category The category to which the question belongs.
 * @property options A list containing all possible answer choices.
 */
data class QuestionData(
    val id: String = "",
    val questionText: String = "",
    val answerA: String = "",
    val answerB: String = "",
    val answerC: String = "",
    val answerD: String = "",
    val correctAnswer: String = "",
    val category: String = ""
) {
    /**
     * Provides a list of possible answers for the question.
     */
    val options: List<String> = listOf(answerA, answerB, answerC, answerD)
}
