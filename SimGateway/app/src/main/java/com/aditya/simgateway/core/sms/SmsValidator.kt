package com.aditya.simgateway.core.sms

class SmsValidator {

    fun validate(
        recipient: String,
        message: String
    ): SmsValidationResult {
        val normalizedRecipient = recipient.trim()
        val normalizedMessage = message.trim()

        if (normalizedMessage.isBlank()) {
            return SmsValidationResult.Invalid("Message cannot be empty")
        }
        if (normalizedMessage.length > SmsConstants.MAX_MESSAGE_LENGTH) {
            return SmsValidationResult.Invalid(
                "Message cannot exceed ${SmsConstants.MAX_MESSAGE_LENGTH} characters"
            )
        }
        if (!PHONE_REGEX.matches(normalizedRecipient)) {
            return SmsValidationResult.Invalid("Phone number format is invalid")
        }

        return SmsValidationResult.Valid(
            recipient = normalizedRecipient,
            message = normalizedMessage
        )
    }

    private companion object {
        val PHONE_REGEX = Regex("^(\\+\\d{10,15}|\\d{10,15})$")
    }
}

sealed interface SmsValidationResult {
    data class Valid(
        val recipient: String,
        val message: String
    ) : SmsValidationResult

    data class Invalid(
        val reason: String
    ) : SmsValidationResult
}
