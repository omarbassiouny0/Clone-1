package mega.privacy.android.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import mega.privacy.android.data.extensions.failWithError
import mega.privacy.android.data.extensions.getChatRequestListener
import mega.privacy.android.data.extensions.getRequestListener
import mega.privacy.android.data.extensions.toException
import mega.privacy.android.data.gateway.AppEventGateway
import mega.privacy.android.data.gateway.api.MegaApiFolderGateway
import mega.privacy.android.data.gateway.api.MegaApiGateway
import mega.privacy.android.data.gateway.api.MegaChatApiGateway
import mega.privacy.android.data.listener.OptionalMegaRequestListenerInterface
import mega.privacy.android.domain.entity.login.LoginStatus
import mega.privacy.android.domain.exception.ChatLoggingOutException
import mega.privacy.android.domain.exception.ChatNotInitializedErrorStatus
import mega.privacy.android.domain.exception.ChatNotInitializedUnknownStatus
import mega.privacy.android.domain.exception.LoginBlockedAccount
import mega.privacy.android.domain.exception.LoginLoggedOutFromOtherLocation
import mega.privacy.android.domain.exception.LoginRequireValidation
import mega.privacy.android.domain.exception.LoginTooManyAttempts
import mega.privacy.android.domain.exception.LoginUnknownStatus
import mega.privacy.android.domain.exception.LoginWrongEmailOrPassword
import mega.privacy.android.domain.qualifier.IoDispatcher
import mega.privacy.android.domain.repository.LoginRepository
import nz.mega.sdk.MegaChatApi
import nz.mega.sdk.MegaError
import nz.mega.sdk.MegaRequest
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * Default [LoginRepository] implementation.
 *
 * @property megaApiGateway
 * @property megaApiFolderGateway
 * @property megaChatApiGateway
 * @property ioDispatcher
 * @property appEventGateway
 */
internal class DefaultLoginRepository @Inject constructor(
    private val megaApiGateway: MegaApiGateway,
    private val megaApiFolderGateway: MegaApiFolderGateway,
    private val megaChatApiGateway: MegaChatApiGateway,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val appEventGateway: AppEventGateway,
) : LoginRepository {

    override suspend fun initMegaChat(session: String) =
        withContext(ioDispatcher) {
            suspendCoroutine { continuation ->
                var state = megaChatApiGateway.initState

                when (state) {
                    MegaChatApi.INIT_NOT_DONE, MegaChatApi.INIT_ERROR -> {
                        state = megaChatApiGateway.init(session)

                        when (state) {
                            MegaChatApi.INIT_NO_CACHE -> Timber.d("INIT_NO_CACHE")
                            MegaChatApi.INIT_ERROR -> {
                                val exception = ChatNotInitializedErrorStatus()
                                Timber.e("Init chat error: ${exception.message}. Logout...")
                                megaChatApiGateway.logout(null)
                                continuation.resumeWith(Result.failure(exception))
                                return@suspendCoroutine
                            }
                            else -> Timber.d("Chat correctly initialized")
                        }
                    }
                    MegaChatApi.INIT_TERMINATED -> {
                        Timber.w("Chat with terminated state, a logout is in progress.")
                        continuation.resumeWith(Result.failure(ChatLoggingOutException()))
                        return@suspendCoroutine
                    }
                }

                continuation.resumeWith(Result.success(Unit))
            }
        }


    override suspend fun fastLogin(session: String) =
        withContext(ioDispatcher) {
            suspendCoroutine { continuation ->
                Timber.d("Fast login allowed.")
                megaApiGateway.fastLogin(
                    session,
                    OptionalMegaRequestListenerInterface(
                        onRequestFinish = onFastLoginFinish(continuation)
                    )
                )
            }
        }

    private fun onFastLoginFinish(continuation: Continuation<Unit>) =
        { _: MegaRequest, error: MegaError ->
            if (error.errorCode == MegaError.API_OK) {
                Timber.d("Fast login success")
                megaApiFolderGateway.accountAuth = megaApiGateway.accountAuth
                continuation.resumeWith(Result.success(Unit))
            } else {
                Timber.e("Fast login error: ${error.errorString}")
                continuation.failWithError(error)
            }
        }

    override suspend fun fetchNodes() =
        withContext(ioDispatcher) {
            suspendCoroutine { continuation ->
                Timber.d("Fetch nodes allowed.")
                megaApiGateway.fetchNodes(
                    OptionalMegaRequestListenerInterface(
                        onRequestFinish = onFetchNodesFinish(continuation)
                    )
                )
            }
        }

    private fun onFetchNodesFinish(continuation: Continuation<Unit>) =
        { _: MegaRequest, error: MegaError ->
            if (error.errorCode == MegaError.API_OK) {
                Timber.d("Fetch nodes success")
                continuation.resumeWith(Result.success(Unit))
            } else {
                Timber.e("Fetch nodes error: ${error.errorString}")
                continuation.failWithError(error)
            }
        }

    override fun monitorLogout() = appEventGateway.monitorLogout()

    override suspend fun broadcastLogout() = appEventGateway.broadcastLogout()

    override suspend fun localLogout() = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->
            val listener = continuation.getRequestListener {}

            megaApiGateway.localLogout(listener)

            continuation.invokeOnCancellation {
                megaApiGateway.removeRequestListener(listener)
            }
        }
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->
            val listener = continuation.getRequestListener { }

            megaApiGateway.logout(listener)

            continuation.invokeOnCancellation {
                megaApiGateway.removeRequestListener(listener)
            }
        }
    }

    override suspend fun chatLogout() = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->
            val listener = continuation.getChatRequestListener { }

            megaChatApiGateway.logout(listener)

            continuation.invokeOnCancellation {
                megaChatApiGateway.removeRequestListener(listener)
            }
        }
    }

    override fun monitorFinishActivity() = appEventGateway.monitorFinishActivity()

    override suspend fun broadcastFinishActivity() = appEventGateway.broadcastFinishActivity()

    override suspend fun initMegaChat() = withContext(ioDispatcher) {
        suspendCoroutine { continuation ->
            var state = megaChatApiGateway.initState
            Timber.d("MegaChat init state: $state")

            when (state) {
                MegaChatApi.INIT_NOT_DONE, MegaChatApi.INIT_ERROR -> {
                    state = megaChatApiGateway.init(null)

                    when (state) {
                        MegaChatApi.INIT_WAITING_NEW_SESSION -> {
                            continuation.resumeWith(Result.success(Unit))
                        }
                        MegaChatApi.INIT_ERROR -> {
                            val exception = ChatNotInitializedErrorStatus()
                            Timber.e("Init chat error: ${exception.message}. Logout...")
                            continuation.resumeWith(Result.failure(exception))
                        }
                    }

                    continuation.resumeWith(
                        Result.failure(ChatNotInitializedUnknownStatus())
                    )
                }
            }
        }
    }

    override fun login(email: String, password: String) =
        callbackFlow {
            val listener = OptionalMegaRequestListenerInterface(
                onRequestStart = { trySend(LoginStatus.LoginStarted) },
                onRequestFinish = { _, error ->
                    when (error.errorCode) {
                        MegaError.API_OK -> {
                            megaApiFolderGateway.accountAuth = megaApiGateway.accountAuth
                            trySend(LoginStatus.LoginSucceed)
                        }
                        MegaError.API_ESID -> {
                            Timber.w("Logged out from other location.")
                            throw LoginLoggedOutFromOtherLocation()
                        }
                        MegaError.API_EMFAREQUIRED -> {
                            Timber.w("Require 2FA.")
                            trySend(LoginStatus.LoginRequire2FA)
                        }
                        MegaError.API_ENOENT -> {
                            Timber.w("Wrong email or password")
                            throw LoginWrongEmailOrPassword()
                        }
                        MegaError.API_ETOOMANY -> {
                            Timber.w("Too many attempts")
                            throw LoginTooManyAttempts()
                        }
                        MegaError.API_EINCOMPLETE -> {
                            Timber.w("Account not validated")
                            throw LoginRequireValidation()
                        }
                        MegaError.API_EBLOCKED -> {
                            Timber.w("Blocked account")
                            throw LoginBlockedAccount()
                        }
                        else -> {
                            Timber.w("MegaRequest.TYPE_LOGIN error $error")
                            throw LoginUnknownStatus(error.toException())
                        }
                    }

                    close()
                }
            )

            megaApiGateway.login(email, password, listener)

            awaitClose { megaApiGateway.removeRequestListener(listener) }
        }.flowOn(ioDispatcher)
}