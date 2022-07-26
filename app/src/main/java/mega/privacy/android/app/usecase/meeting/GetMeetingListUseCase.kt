package mega.privacy.android.app.usecase.meeting

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import mega.privacy.android.app.R
import mega.privacy.android.app.constants.BroadcastConstants.ACTION_UPDATE_PUSH_NOTIFICATION_SETTING
import mega.privacy.android.app.contacts.group.data.ContactGroupUser
import mega.privacy.android.app.di.MegaApi
import mega.privacy.android.app.listeners.OptionalMegaRequestListenerInterface
import mega.privacy.android.app.main.controllers.ChatController
import mega.privacy.android.app.meeting.list.MeetingItem
import mega.privacy.android.app.usecase.chat.GetChatChangesUseCase
import mega.privacy.android.app.usecase.chat.GetChatChangesUseCase.Result
import mega.privacy.android.app.usecase.exception.toMegaException
import mega.privacy.android.app.utils.AvatarUtil
import mega.privacy.android.app.utils.ChatUtil
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.app.utils.StringResourcesUtils
import mega.privacy.android.app.utils.TimeUtils
import nz.mega.sdk.MegaApiAndroid
import nz.mega.sdk.MegaApiJava
import nz.mega.sdk.MegaChatApi
import nz.mega.sdk.MegaChatApiAndroid
import nz.mega.sdk.MegaChatRoom
import nz.mega.sdk.MegaError
import nz.mega.sdk.MegaRequest
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Get meeting list use case
 *
 * @property context                Android context
 * @property megaApi                MegaApi
 * @property megaChatApi            MegaChatApi
 * @property getChatChangesUseCase  Use case needed to get latest changes from Mega Api
 */
class GetMeetingListUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @MegaApi private val megaApi: MegaApiAndroid,
    private val megaChatApi: MegaChatApiAndroid,
    private val getChatChangesUseCase: GetChatChangesUseCase,
) {

    private val chatController: ChatController by lazy { ChatController(context) }

    /**
     * Get a list of updated MeetingItems
     *
     * @return  Flowable
     */
    fun get(): Flowable<List<MeetingItem>> =
        Flowable.create({ emitter ->
            val changesSubscription = CompositeDisposable()
            val meetings = mutableListOf<MeetingItem>()

            val userAttrsListener = OptionalMegaRequestListenerInterface(
                onRequestFinish = { request, error ->
                    if (emitter.isCancelled) return@OptionalMegaRequestListenerInterface

                    if (error.errorCode == MegaError.API_OK) {
                        val index = meetings.indexOfFirst {
                            request.nodeHandle == it.firstUser.handle || request.nodeHandle == it.lastUser?.handle
                                    || request.email == it.firstUser.email || request.email == it.lastUser?.email
                        }

                        if (index != Constants.INVALID_POSITION) {
                            val currentItem = meetings[index]
                            when (request.paramType) {
                                MegaRequest.TYPE_GET_USER_EMAIL -> {
                                    meetings[index] =
                                        if (request.nodeHandle == currentItem.firstUser.handle) {
                                            currentItem.copy(
                                                firstUser = currentItem.firstUser.copy(email = request.text)
                                            )
                                        } else {
                                            currentItem.copy(
                                                lastUser = currentItem.lastUser?.copy(email = request.text)
                                            )
                                        }
                                }
                                MegaApiJava.USER_ATTR_FIRSTNAME -> {
                                    meetings[index] =
                                        if (request.nodeHandle == currentItem.firstUser.handle) {
                                            currentItem.copy(
                                                firstUser = currentItem.firstUser.copy(firstName = request.text)
                                            )
                                        } else {
                                            currentItem.copy(
                                                lastUser = currentItem.lastUser?.copy(firstName = request.text)
                                            )
                                        }
                                }
                                MegaApiJava.USER_ATTR_AVATAR -> {
                                    meetings[index] =
                                        if (request.email == currentItem.firstUser.email) {
                                            currentItem.copy(
                                                firstUser = currentItem.firstUser.copy(avatar = File(
                                                    request.file).toUri())
                                            )
                                        } else {
                                            currentItem.copy(
                                                lastUser = currentItem.lastUser?.copy(avatar = File(
                                                    request.file).toUri())
                                            )
                                        }
                                }
                            }

                            emitter.onNext(meetings.sortedByDescending { it.timeStamp })
                        }
                    } else {
                        Timber.w(error.toMegaException())
                    }
                },
                onRequestTemporaryError = { _, error ->
                    Timber.w(error.toMegaException())
                }
            )

            val muteObserver = Observer<Any> {
                if (emitter.isCancelled) return@Observer
                val index = meetings.indexOfFirst { it.isMuted != !megaApi.isChatNotifiable(it.chatId) }
                if (index != Constants.INVALID_POSITION) {
                    val oldItem = meetings[index]
                    meetings[index] = oldItem.copy(isMuted = !oldItem.isMuted)
                    emitter.onNext(meetings.sortedByDescending { it.timeStamp })
                }
            }

            megaChatApi.getChatRoomsByType(MegaChatApi.CHAT_TYPE_MEETING_ROOM)
                .filter { it.isActive && !it.isArchived }
                .forEach { chatRoom ->
                    meetings.add(chatRoom.toMeetingItem(userAttrsListener))
                }

            emitter.onNext(meetings.sortedByDescending { it.timeStamp })

            getChatChangesUseCase.get()
                .filter { it is Result.OnChatListItemUpdate && it.item != null }
                .subscribeBy(
                    onNext = { change ->
                        if (emitter.isCancelled) return@subscribeBy

                        val updateChange = change as Result.OnChatListItemUpdate
                        val updatedChatId = requireNotNull(updateChange.item).chatId
                        val updatedChatRoom = megaChatApi.getChatRoom(updatedChatId)

                        val index = meetings.indexOfFirst { it.chatId == updatedChatId }
                        if (index != Constants.INVALID_POSITION) {
                            if (updatedChatRoom.isArchived || !updatedChatRoom.isActive) {
                                meetings.removeAt(index)
                            } else {
                                meetings[index] = updatedChatRoom.toMeetingItem(userAttrsListener)
                            }
                            emitter.onNext(meetings.sortedByDescending { it.timeStamp })
                        } else if (updatedChatRoom.isMeeting && updatedChatRoom.isActive && !updatedChatRoom.isArchived) {
                            meetings.add(updatedChatRoom.toMeetingItem(userAttrsListener))
                            emitter.onNext(meetings.sortedByDescending { it.timeStamp })
                        }
                    },
                    onError = Timber::e
                ).addTo(changesSubscription)

            LiveEventBus.get(ACTION_UPDATE_PUSH_NOTIFICATION_SETTING).observeForever(muteObserver)

            emitter.setCancellable {
                changesSubscription.dispose()
                LiveEventBus.get(ACTION_UPDATE_PUSH_NOTIFICATION_SETTING).removeObserver(muteObserver)
            }
        }, BackpressureStrategy.LATEST)

    /**
     * Build MeetingItem given a MegaChatRoom
     *
     * @param listener Listener to deliver user attributes
     * @return                  MeetingItem
     */
    private fun MegaChatRoom.toMeetingItem(listener: OptionalMegaRequestListenerInterface): MeetingItem {
        val chatListItem = megaChatApi.getChatListItem(chatId)
        val title = ChatUtil.getTitleChat(this)
        val isMuted = !megaApi.isChatNotifiable(chatId)
        val formattedDate = TimeUtils.formatDateAndTime(
            context,
            chatListItem.lastTimestamp,
            TimeUtils.DATE_LONG_FORMAT)
        val lastMessage = megaChatApi.getMessage(chatId, chatListItem.lastMessageId)
        val lastMessageFormatted = if (lastMessage != null) {
            chatController.createManagementString(lastMessage, this)
        } else {
            StringResourcesUtils.getString(R.string.no_conversation_history)
        }
        val firstUser: ContactGroupUser
        var lastUser: ContactGroupUser? = null

        when (peerCount) {
            0L -> {
                firstUser = getGroupUserFromHandle(megaChatApi.myUserHandle, listener)
            }
            1L -> {
                firstUser = getGroupUserFromHandle(megaChatApi.myUserHandle, listener)
                lastUser = getGroupUserFromHandle(getPeerHandle(0), listener)
            }
            else -> {
                firstUser = getGroupUserFromHandle(getPeerHandle(0), listener)
                lastUser = getGroupUserFromHandle(getPeerHandle(peerCount - 1), listener)
            }
        }

        return MeetingItem(
            chatId = chatId,
            title = title,
            lastMessage = lastMessageFormatted,
            isMuted = isMuted,
            firstUser = firstUser,
            lastUser = lastUser,
            timeStamp = chatListItem.lastTimestamp,
            formattedTimestamp = formattedDate)
    }

    /**
     * Build ContactGroupUser given an User handle
     *
     * @param userHandle    User handle to obtain group
     * @param listener      Listener to deliver user attributes
     * @return              ContactGroupUser
     */
    private fun getGroupUserFromHandle(
        userHandle: Long,
        listener: OptionalMegaRequestListenerInterface,
    ): ContactGroupUser {
        val myself = userHandle == megaChatApi.myUserHandle
        var userAvatar: File? = null
        val userName = if (myself) {
            megaChatApi.myFirstname
        } else {
            megaChatApi.getUserFirstnameFromCache(userHandle)
        }
        val userEmail = if (myself) {
            megaChatApi.myEmail
        } else {
            megaChatApi.getUserEmailFromCache(userHandle)
        }
        val userAvatarColor = megaApi.getUserAvatarColor(userHandle.toString()).toColorInt()

        if (userName.isNullOrBlank()) {
            megaApi.getUserAttribute(userHandle.toString(),
                MegaApiJava.USER_ATTR_FIRSTNAME,
                listener)
        }

        if (userEmail.isNullOrBlank()) {
            megaApi.getUserEmail(userHandle, listener)
        } else {
            val avatarFile = AvatarUtil.getUserAvatarFile(context, userEmail)
            if (avatarFile?.exists() == true) {
                userAvatar = avatarFile
            } else {
                megaApi.getUserAvatar(userEmail, avatarFile?.absolutePath, listener)
            }
        }

        return ContactGroupUser(
            handle = userHandle,
            email = userEmail,
            firstName = userName,
            avatar = userAvatar?.toUri(),
            avatarColor = userAvatarColor
        )
    }
}

