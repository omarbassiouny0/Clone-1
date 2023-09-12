package mega.privacy.android.app.presentation.notification.model.extensions

import android.content.Context
import mega.privacy.android.app.R
import mega.privacy.android.domain.entity.ContactAlert
import mega.privacy.android.domain.entity.CustomAlert
import mega.privacy.android.domain.entity.IncomingShareAlert
import mega.privacy.android.domain.entity.ScheduledMeetingAlert
import mega.privacy.android.domain.entity.UserAlert
import java.util.Locale

/**
 * Section title
 *
 */
internal fun UserAlert.sectionTitle(): (Context) -> String = when (this) {
    is ContactAlert -> { context ->
        context.getString(R.string.section_contacts)
    }
    is IncomingShareAlert -> { context ->
        context.getString(R.string.title_incoming_shares_explorer)
    }
    is ScheduledMeetingAlert -> { context ->
        context.getString(R.string.chat_tab_meetings_title)
    }
    is CustomAlert -> { _ ->
        heading ?: ""
    }
    else -> { _ -> "" }
}