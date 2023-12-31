package mega.privacy.android.app.presentation.node.model.menuaction

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import mega.privacy.android.app.R
import mega.privacy.android.core.ui.model.MenuActionWithIcon
import javax.inject.Inject

/**
 * General share menu action
 */
class ShareMenuAction @Inject constructor() : MenuActionWithIcon {

    @Composable
    override fun getDescription() = stringResource(id = R.string.general_share)

    @Composable
    override fun getIconPainter() = painterResource(id = R.drawable.ic_social_share_white)

    override val orderInCategory = 180

    override val testTag: String = "menu_action:share"
}