package com.example.quiz.firebase

/**
 * Represents a user profile in the application.
 *
 * This data class models user-related information, including personal details,
 * contact information, preferences, and registration status. It also provides
 * a method for converting Firestore data into a [User] object.
 *
 * @property id Unique identifier for the user.
 * @property name Full name of the user.
 * @property email Email address associated with the user.
 * @property phoneNumber Contact phone number.
 * @property dateOfBirth User's date of birth formatted as a string.
 * @property address A map containing address components (e.g., street, city, country).
 * @property interests A list of topics or activities the user is interested in.
 * @property profilePictureUrl URL of the user's profile picture.
 * @property registeredUser Indicates whether the user is registered in the system.
 */
class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val address: Map<String, String> = emptyMap(),
    val interests: List<String> = emptyList(),
    val profilePictureUrl: String = "",
    val registeredUser: Boolean = false // Defaults to false
) {
    companion object {
        /**
         * Creates a [User] instance from a Firestore data map.
         *
         * This function extracts values from a provided map, ensuring that missing fields
         * are safely handled with default values to prevent runtime errors.
         *
         * @param data A map representing Firestore user data.
         * @return A [User] object populated with the extracted data.
         */
        fun fromMap(data: Map<String, Any>): User {
            return User(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                dateOfBirth = data["dateOfBirth"] as? String ?: "",
                address = data["address"] as? Map<String, String> ?: emptyMap(),
                interests = data["interests"] as? List<String> ?: emptyList(),
                profilePictureUrl = data["profilePictureUrl"] as? String ?: "",
                registeredUser = data["registeredUser"] as? Boolean ?: false // Safely handles missing values
            )
        }
    }
}
