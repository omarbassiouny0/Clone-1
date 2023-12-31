package mega.privacy.android.domain.usecase

import mega.privacy.android.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Default get extended account detail
 *
 */
internal class DefaultGetExtendedAccountDetail @Inject constructor(
    private val repository: AccountRepository,
    private val isExtendedAccountDetailStale: IsExtendedAccountDetailStale,
) : GetExtendedAccountDetail {
    override suspend fun invoke(
        forceRefresh: Boolean,
        sessions: Boolean,
        purchases: Boolean,
        transactions: Boolean
    ) {
        if (forceRefresh || isExtendedAccountDetailStale()) {
            repository.resetExtendedAccountDetailsTimestamp()
            repository.getExtendedAccountDetails(sessions, purchases, transactions)
        }
    }
}