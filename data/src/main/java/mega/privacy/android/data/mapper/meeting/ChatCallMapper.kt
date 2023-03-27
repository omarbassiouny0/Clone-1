package mega.privacy.android.data.mapper.meeting

import mega.privacy.android.data.mapper.HandleListMapper
import mega.privacy.android.domain.entity.chat.ChatCall
import mega.privacy.android.domain.entity.meeting.CallCompositionChanges
import mega.privacy.android.domain.entity.meeting.ChatCallChanges
import mega.privacy.android.domain.entity.meeting.EndCallReason
import mega.privacy.android.domain.entity.meeting.NetworkQualityType
import mega.privacy.android.domain.entity.meeting.TermCodeType
import nz.mega.sdk.MegaChatCall
import javax.inject.Inject

/**
 * Call mapper
 */
internal class ChatCallMapper @Inject constructor(
    private val handleListMapper: HandleListMapper,
    private val megaChatCallStatusMapper: MegaChatCallStatusMapper,
) {
    /**
     * Invoke
     *
     * @param megaChatCall
     * @return [ChatCall]
     */
    operator fun invoke(megaChatCall: MegaChatCall): ChatCall = ChatCall(
        callId = megaChatCall.callId,
        chatId = megaChatCall.chatid,
        status = megaChatCallStatusMapper(megaChatCall.status),
        caller = megaChatCall.caller,
        duration = megaChatCall.duration,
        numParticipants = megaChatCall.numParticipants,
        changes = callChanges[megaChatCall.changes],
        endCallReason = endCallReason[megaChatCall.endCallReason],
        callCompositionChange = callCompositionChanges[megaChatCall.callCompositionChange],
        peeridCallCompositionChange = megaChatCall.peeridCallCompositionChange,
        peerIdParticipants = handleListMapper(megaChatCall.peeridParticipants),
        moderators = handleListMapper(megaChatCall.moderators),
        sessionsClientid = handleListMapper(megaChatCall.sessionsClientid),
        networkQuality = networkQuality[megaChatCall.networkQuality],
        termCode = termCode[megaChatCall.termCode],
        initialTimestamp = megaChatCall.initialTimeStamp,
        finalTimestamp = megaChatCall.finalTimeStamp,
        isAudioDetected = megaChatCall.isAudioDetected,
        isIgnored = megaChatCall.isIgnored,
        isIncoming = megaChatCall.isIncoming,
        isOnHold = megaChatCall.isOnHold,
        isOutgoing = megaChatCall.isOutgoing,
        isOwnClientCaller = megaChatCall.isOwnClientCaller,
        isOwnModerator = megaChatCall.isOwnModerator,
        isRinging = megaChatCall.isRinging,
        isSpeakAllow = megaChatCall.isSpeakAllow,
        hasLocalAudio = megaChatCall.hasLocalAudio(),
        hasLocalVideo = megaChatCall.hasLocalVideo(),
        hasRequestSpeak = megaChatCall.hasRequestSpeak(),
    )

    companion object {

        internal val endCallReason = mapOf(
            MegaChatCall.END_CALL_REASON_INVALID to EndCallReason.Invalid,
            MegaChatCall.END_CALL_REASON_ENDED to EndCallReason.Ended,
            MegaChatCall.END_CALL_REASON_REJECTED to EndCallReason.Rejected,
            MegaChatCall.END_CALL_REASON_NO_ANSWER to EndCallReason.NoAnswer,
            MegaChatCall.END_CALL_REASON_FAILED to EndCallReason.Failed,
            MegaChatCall.END_CALL_REASON_CANCELLED to EndCallReason.Cancelled,
            MegaChatCall.END_CALL_REASON_BY_MODERATOR to EndCallReason.ByModerator
        )

        internal val callCompositionChanges = mapOf(
            MegaChatCall.PEER_REMOVED to CallCompositionChanges.Removed,
            MegaChatCall.NO_COMPOSITION_CHANGE to CallCompositionChanges.NoChange,
            MegaChatCall.PEER_ADDED to CallCompositionChanges.Added,
        )

        internal val termCode = mapOf(
            MegaChatCall.TERM_CODE_INVALID to TermCodeType.Invalid,
            MegaChatCall.TERM_CODE_HANGUP to TermCodeType.Hangup,
            MegaChatCall.TERM_CODE_TOO_MANY_PARTICIPANTS to TermCodeType.TooManyParticipants,
        )

        internal val networkQuality = mapOf(
            MegaChatCall.NETWORK_QUALITY_BAD to NetworkQualityType.Bad,
            MegaChatCall.NETWORK_QUALITY_GOOD to NetworkQualityType.Good,
        )

        internal val callChanges = mapOf(
            MegaChatCall.CHANGE_TYPE_STATUS to ChatCallChanges.Status,
            MegaChatCall.CHANGE_TYPE_LOCAL_AVFLAGS to ChatCallChanges.LocalAVFlags,
            MegaChatCall.CHANGE_TYPE_RINGING_STATUS to ChatCallChanges.RingingStatus,
            MegaChatCall.CHANGE_TYPE_CALL_COMPOSITION to ChatCallChanges.CallComposition,
            MegaChatCall.CHANGE_TYPE_CALL_ON_HOLD to ChatCallChanges.OnHold,
            MegaChatCall.CHANGE_TYPE_CALL_SPEAK to ChatCallChanges.Speaker,
            MegaChatCall.CHANGE_TYPE_AUDIO_LEVEL to ChatCallChanges.AudioLevel,
            MegaChatCall.CHANGE_TYPE_NETWORK_QUALITY to ChatCallChanges.NetworkQuality,
            MegaChatCall.CHANGE_TYPE_OUTGOING_RINGING_STOP to ChatCallChanges.OutgoingRingingStop,
        )
    }
}