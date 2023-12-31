package mega.privacy.android.domain.usecase

/**
 * Resets the credentials of a given user.
 */
fun interface ResetCredentials {

    /**
     * Invoke.
     *
     * @param userEmail User's email
     */
    suspend operator fun invoke(userEmail: String)
}