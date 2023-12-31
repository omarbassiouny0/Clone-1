package test.mega.privacy.android.app.presentation.meeting.chat

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import mega.privacy.android.app.R
import mega.privacy.android.app.presentation.contact.view.getLastSeenString
import mega.privacy.android.app.presentation.meeting.chat.model.ChatRoomMenuAction
import mega.privacy.android.app.presentation.meeting.chat.model.ChatRoomMenuAction.Companion.TEST_TAG_ADD_PARTICIPANTS_ACTION
import mega.privacy.android.app.presentation.meeting.chat.model.ChatRoomMenuAction.Companion.TEST_TAG_AUDIO_CALL_ACTION
import mega.privacy.android.app.presentation.meeting.chat.model.ChatRoomMenuAction.Companion.TEST_TAG_INFO_ACTION
import mega.privacy.android.app.presentation.meeting.chat.model.ChatRoomMenuAction.Companion.TEST_TAG_VIDEO_CALL_ACTION
import mega.privacy.android.app.presentation.meeting.chat.model.ChatUiState
import mega.privacy.android.app.presentation.meeting.chat.view.ChatView
import mega.privacy.android.app.presentation.meeting.chat.view.TEST_TAG_NOTIFICATION_MUTE
import mega.privacy.android.app.presentation.meeting.chat.view.TEST_TAG_PRIVATE_ICON
import mega.privacy.android.app.presentation.meeting.chat.view.TEST_TAG_USER_CHAT_STATE
import mega.privacy.android.core.ui.controls.menus.TAG_MENU_ACTIONS_SHOW_MORE
import mega.privacy.android.domain.entity.ChatRoomPermission
import mega.privacy.android.domain.entity.contacts.UserChatStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import test.mega.privacy.android.app.onNodeWithPlural
import test.mega.privacy.android.app.onNodeWithText

@RunWith(AndroidJUnit4::class)
class ChatViewTest {

    private val actionPressed = mock<(ChatRoomMenuAction) -> Unit>()

    @get:Rule
    var composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `test that title shows correctly when passing title to uiState`() {
        val title = "my chat room title"
        initComposeRuleContent(
            ChatUiState(title = title)
        )
        composeTestRule.onAllNodesWithText(title).onFirst().assertIsDisplayed()

    }

    @Test
    fun `test that status icon is not visible when the user chat status is null`() {
        initComposeRuleContent(
            ChatUiState(userChatStatus = null)
        )
        composeTestRule.onNodeWithTag(TEST_TAG_USER_CHAT_STATE).assertDoesNotExist()
    }

    @Test
    fun `test that status icon is not visible when the user chat status is invalid`() {
        initComposeRuleContent(
            ChatUiState(userChatStatus = UserChatStatus.Invalid)
        )
        composeTestRule.onNodeWithTag(TEST_TAG_USER_CHAT_STATE).assertDoesNotExist()
    }

    @Test
    fun `test that status icon is visible when the user chat status is online`() {
        initComposeRuleContent(
            ChatUiState(userChatStatus = UserChatStatus.Online)
        )
        composeTestRule.onNodeWithTag(TEST_TAG_USER_CHAT_STATE).assertIsDisplayed()
    }

    @Test
    fun `test that mute icon is visible when chat notification is mute`() {
        initComposeRuleContent(
            ChatUiState(isChatNotificationMute = true)
        )
        composeTestRule.onNodeWithTag(TEST_TAG_NOTIFICATION_MUTE).assertIsDisplayed()
    }

    @Test
    fun `test that mute icon is hidden when chat notification is mute`() {
        initComposeRuleContent(
            ChatUiState(isChatNotificationMute = false)
        )
        composeTestRule.onNodeWithTag(TEST_TAG_NOTIFICATION_MUTE).assertDoesNotExist()
    }

    @Test
    fun `test that private icon is visible when chat room is private`() {
        initComposeRuleContent(
            ChatUiState(isPrivateChat = true)
        )
        composeTestRule.onNodeWithTag(TEST_TAG_PRIVATE_ICON).assertIsDisplayed()
    }

    @Test
    fun `test that private icon is hidden when chat room is public`() {
        initComposeRuleContent(
            ChatUiState(isPrivateChat = false)
        )
        composeTestRule.onNodeWithTag(TEST_TAG_PRIVATE_ICON).assertDoesNotExist()
    }

    @Test
    fun `test that audio call is hidden when my permission is unknown`() {
        initComposeRuleContent(ChatUiState())
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that audio call is hidden when my permission is removed`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.Removed))
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that audio call is hidden when my permission is read only`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.ReadOnly))
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that audio call is hidden when is joining or leaving`() {
        initComposeRuleContent(ChatUiState(isJoiningOrLeaving = true))
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that audio call is hidden when is preview mode`() {
        initComposeRuleContent(ChatUiState(isPreviewMode = true))
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that audio call is shown and enabled when my permission is standard`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.Standard))
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertIsDisplayed()
    }

    @Test
    fun `test that audio call is shown and enabled when my permission is moderator`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.Moderator))
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertIsDisplayed()
    }

    @Test
    fun `test that audio call is shown but disabled when my permission is moderator and this chat has a call`() {
        initComposeRuleContent(
            ChatUiState(
                myPermission = ChatRoomPermission.Moderator,
                hasACallInThisChat = true
            )
        )
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).performClick()
        verifyNoInteractions(actionPressed)
    }

    @Test
    fun `test that video call is hidden when my permission is unknown`() {
        initComposeRuleContent(ChatUiState())
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that video call is hidden when my permission is removed`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.Removed))
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that video call is hidden when my permission is read only`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.ReadOnly))
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that video call is hidden when is joining or leaving`() {
        initComposeRuleContent(ChatUiState(isJoiningOrLeaving = true))
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that video call is hidden when is preview mode`() {
        initComposeRuleContent(ChatUiState(isPreviewMode = true))
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that video call is hidden when the chat is a group`() {
        initComposeRuleContent(ChatUiState(isGroup = true))
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that video call is shown and enabled when my permission is standard`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.Standard))
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertIsDisplayed()
    }

    @Test
    fun `test that video call is shown and enabled when my permission is moderator`() {
        initComposeRuleContent(ChatUiState(myPermission = ChatRoomPermission.Moderator))
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertIsDisplayed()
    }

    @Test
    fun `test that video call is shown but disabled when my permission is moderator and this chat has a call`() {
        initComposeRuleContent(
            ChatUiState(
                myPermission = ChatRoomPermission.Moderator,
                hasACallInThisChat = true
            )
        )
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_AUDIO_CALL_ACTION).performClick()
        verifyNoInteractions(actionPressed)
    }

    @Test
    fun `test that add participants is available when the chat is a group my permission is moderator`() {
        initComposeRuleContent(
            ChatUiState(
                isGroup = true,
                myPermission = ChatRoomPermission.Moderator
            )
        )
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_ADD_PARTICIPANTS_ACTION).assertExists()
    }

    @Test
    fun `test that add participants is available when the chat is a group and is open invite`() {
        initComposeRuleContent(
            ChatUiState(
                isGroup = true,
                isOpenInvite = true
            )
        )
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_ADD_PARTICIPANTS_ACTION).assertIsDisplayed()
    }

    @Test
    fun `test that add participants is not available when the chat is not a group`() {
        initComposeRuleContent(ChatUiState(isGroup = false))
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_ADD_PARTICIPANTS_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that add participants is not available when is joining or leaving`() {
        initComposeRuleContent(ChatUiState(isJoiningOrLeaving = true))
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_ADD_PARTICIPANTS_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that add participants is not available when the group is not active`() {
        initComposeRuleContent(ChatUiState(isGroup = true, isActive = false))
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_ADD_PARTICIPANTS_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that add participants is not available when the group does not open invite`() {
        initComposeRuleContent(ChatUiState(isGroup = true, isOpenInvite = false))
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).assertDoesNotExist()
    }

    @Test
    fun `test that add participants is not available when my permission is not moderator`() {
        initComposeRuleContent(
            ChatUiState(
                isGroup = true,
                myPermission = ChatRoomPermission.Standard
            )
        )
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_ADD_PARTICIPANTS_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that participating in a call dialog show when click to audio call and user is in another call`() {
        initComposeRuleContent(
            ChatUiState(
                myPermission = ChatRoomPermission.Standard,
                isParticipatingInACall = true
            )
        )
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.ongoing_call_content))
            .assertIsDisplayed()
    }

    @Test
    fun `test that participating in a call dialog doesn't show when click to audio call and user is in another call`() {
        initComposeRuleContent(
            ChatUiState(
                myPermission = ChatRoomPermission.Standard,
                isParticipatingInACall = false
            )
        )
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_VIDEO_CALL_ACTION).performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.ongoing_call_content))
            .assertDoesNotExist()
    }

    @Test
    fun `test that archive label shown when chat is archived`() {
        initComposeRuleContent(
            ChatUiState(
                isArchived = true
            )
        )
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.archived_chat))
            .assertIsDisplayed()
    }

    @Test
    fun `test that archive label doesn't show when chat is not archived`() {
        initComposeRuleContent(
            ChatUiState(
                isArchived = false
            )
        )
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.archived_chat))
            .assertDoesNotExist()
    }

    @Test
    fun `test that read only label shown when in a chat I have read only permission`() {
        initComposeRuleContent(
            ChatUiState(
                myPermission = ChatRoomPermission.ReadOnly
            )
        )
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.observer_permission_label_participants_panel))
            .assertIsDisplayed()
    }

    @Test
    fun `test that ready only label does not show in a chat I do not have read only permission`() {
        initComposeRuleContent(ChatUiState())
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.observer_permission_label_participants_panel))
            .assertDoesNotExist()
    }

    @Test
    fun `test that inactive label shown when in a chat I have read only permission`() {
        initComposeRuleContent(
            ChatUiState(
                myPermission = ChatRoomPermission.Removed
            )
        )
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.inactive_chat))
            .assertIsDisplayed()
    }

    @Test
    fun `test that inactive label does not show in a chat I do not have read only permission`() {
        initComposeRuleContent(ChatUiState())
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.inactive_chat))
            .assertDoesNotExist()
    }

    @Test
    fun `test that Info menu action is not available when chat is joining or leaving`() {
        initComposeRuleContent(ChatUiState(isGroup = true, isOpenInvite = false))
        composeTestRule.onNodeWithTag(TEST_TAG_INFO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that Info menu action is not available when chat is in preview mode`() {
        initComposeRuleContent(ChatUiState(isPreviewMode = true))
        composeTestRule.onNodeWithTag(TEST_TAG_INFO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that Info menu action is not available when chat is not connected`() {
        initComposeRuleContent(ChatUiState(isConnected = false))
        composeTestRule.onNodeWithTag(TEST_TAG_INFO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that Info menu action is not available when 1on1 chat is read only`() {
        initComposeRuleContent(
            ChatUiState(
                isGroup = false,
                myPermission = ChatRoomPermission.ReadOnly
            )
        )

        composeTestRule.onNodeWithTag(TEST_TAG_INFO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `test that Info menu action is available in 1on1 chat and my permission is Moderator`() {
        initComposeRuleContent(
            ChatUiState(
                isJoiningOrLeaving = false,
                isPreviewMode = false,
                isConnected = true,
                isGroup = false,
                myPermission = ChatRoomPermission.Moderator
            )
        )
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_INFO_ACTION).assertIsDisplayed()
    }


    @Test
    fun `test that Info menu action is available in group chat`() {
        initComposeRuleContent(
            ChatUiState(
                isJoiningOrLeaving = false,
                isPreviewMode = false,
                isConnected = true,
                isGroup = true,
            )
        )
        composeTestRule.onNodeWithTag(TAG_MENU_ACTIONS_SHOW_MORE).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_INFO_ACTION).assertIsDisplayed()
    }


    fun `test that online label shows if the chat is 1to1 and the contacts status is online`() {
        initComposeRuleContent(ChatUiState(isGroup = false, userChatStatus = UserChatStatus.Online))
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.online_status))
            .assertIsDisplayed()
    }

    @Test
    fun `test that away label shows if the chat is 1to1 and the contacts status is away`() {
        initComposeRuleContent(ChatUiState(isGroup = false, userChatStatus = UserChatStatus.Away))
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.away_status))
            .assertIsDisplayed()
    }

    @Test
    fun `test that busy label shows if the chat is 1to1 and the contacts status is busy`() {
        initComposeRuleContent(ChatUiState(isGroup = false, userChatStatus = UserChatStatus.Busy))
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.busy_status))
            .assertIsDisplayed()
    }

    @Test
    fun `test that offline label shows if the chat is 1to1 and the contacts status is offline`() {
        initComposeRuleContent(
            ChatUiState(
                isGroup = false,
                userChatStatus = UserChatStatus.Offline
            )
        )
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.offline_status))
            .assertIsDisplayed()
    }

    @Test
    fun `test that first message header is shown`() {
        initComposeRuleContent(ChatUiState())
        composeTestRule.onNodeWithText(R.string.chat_chatroom_first_message_header_mega_info_text)
            .assertExists()
        composeTestRule.onNodeWithText(R.string.title_mega_confidentiality_empty_screen)
            .assertExists()
        composeTestRule.onNodeWithText(R.string.mega_confidentiality_empty_screen).assertExists()
        composeTestRule.onNodeWithText(R.string.title_mega_confidentiality_empty_screen)
            .assertExists()
        composeTestRule.onNodeWithText(R.string.chat_chatroom_first_message_header_authenticity_info_text)
            .assertExists()

    }

    @Test
    fun `test that last green label shows if the chat is 1to1 and the contacts last green is not null`() {
        val lastGreen = initComposeRuleContentWithLastGreen(
            ChatUiState(
                isGroup = false,
                userLastGreen = 123456
            )
        )
        composeTestRule.onNodeWithText(lastGreen).assertIsDisplayed()
    }

    @Test
    fun `test that number of participants shows if the chat is a group and does not have a custom title`() {
        val count = 5
        initComposeRuleContent(
            ChatUiState(
                isGroup = true,
                hasCustomTitle = false,
                participantsCount = count.toLong()
            )
        )
        composeTestRule.onNodeWithPlural(R.plurals.subtitle_of_group_chat, count)
            .assertExists()
    }

    @Test
    fun `test that number of participants shows if the chat is in preview mode`() {
        val count = 5
        initComposeRuleContent(
            ChatUiState(
                isPreviewMode = true,
                participantsCount = count.toLong()
            )
        )
        composeTestRule.onNodeWithPlural(R.plurals.subtitle_of_group_chat, count)
            .assertExists()
    }

    private fun initComposeRuleContent(state: ChatUiState) {
        composeTestRule.setContent {
            ChatView(
                uiState = state,
                onBackPressed = {},
                onMenuActionPressed = actionPressed
            )
        }
    }

    private fun initComposeRuleContentWithLastGreen(state: ChatUiState): String {
        var lastGreen = "last green"
        composeTestRule.setContent {
            ChatView(
                uiState = state,
                onBackPressed = {},
                onMenuActionPressed = actionPressed
            )
            lastGreen = getLastSeenString(lastGreen = state.userLastGreen) ?: ""
        }
        return lastGreen
    }
}