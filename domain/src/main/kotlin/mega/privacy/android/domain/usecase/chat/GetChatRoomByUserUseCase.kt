package mega.privacy.android.domain.usecase.chat

import mega.privacy.android.domain.entity.chat.ChatRoom
import mega.privacy.android.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for getting the updated main data of a chat room from user info
 */
class GetChatRoomByUserUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    /**
     * Invoke.
     *
     * @param userHandle        User handle.
     * @return [ChatRoom]   containing the updated data.
     */
    suspend operator fun invoke(userHandle: Long) = chatRepository.getChatRoomByUser(userHandle)
}