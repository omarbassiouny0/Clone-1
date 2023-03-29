package mega.privacy.android.domain.usecase.login

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import mega.privacy.android.domain.entity.login.FetchNodesUpdate
import mega.privacy.android.domain.exception.login.FetchNodesBlockedAccount
import mega.privacy.android.domain.qualifier.LoginMutex
import mega.privacy.android.domain.repository.LoginRepository
import mega.privacy.android.domain.usecase.camerauploads.EstablishCameraUploadsSyncHandles
import mega.privacy.android.domain.usecase.setting.ResetChatSettingsUseCase
import javax.inject.Inject

/**
 * Use case for fetching nodes.
 */
class FetchNodesUseCase @Inject constructor(
    private val establishCameraUploadsSyncHandles: EstablishCameraUploadsSyncHandles,
    private val loginRepository: LoginRepository,
    private val resetChatSettingsUseCase: ResetChatSettingsUseCase,
    @LoginMutex private val loginMutex: Mutex,
) {

    /**
     * Invoke.
     *
     * @return Flow of [FetchNodesUpdate].
     */
    operator fun invoke() = callbackFlow {
        loginMutex.lock()

        runCatching {
            loginRepository.fetchNodesFlow()
                .map { update ->
                    if (update.progress?.floatValue == 1F) {
                        establishCameraUploadsSyncHandles()
                        loginMutex.unlock()
                    }
                    update
                }
                .collectLatest { update ->
                    trySend(update)
                }
        }.onFailure {
            if (it !is FetchNodesBlockedAccount) {
                resetChatSettingsUseCase()
            }

            loginMutex.unlock()
            throw it
        }

        awaitClose {
            loginMutex.unlock()
        }
    }
}