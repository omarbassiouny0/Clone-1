package mega.privacy.android.app.presentation.verification.model

/**
 * Sms verification text state
 *
 * @constructor Create empty Sms verification text state
 */
sealed interface SmsVerificationTextState {
    /**
     * Empty
     */
    object Empty : SmsVerificationTextState

    /**
     * VerifiedSuccessfully
     */
    object VerifiedSuccessfully : SmsVerificationTextState

    /**
     * Failed
     *
     * @property error
     */
    data class Failed(val error: String) : SmsVerificationTextState

    /**
     * Loading
     */
    object Loading : SmsVerificationTextState
}
