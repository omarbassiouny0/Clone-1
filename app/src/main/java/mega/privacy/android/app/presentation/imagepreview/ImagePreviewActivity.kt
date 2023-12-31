package mega.privacy.android.app.presentation.imagepreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mega.privacy.android.app.BaseActivity
import mega.privacy.android.app.R
import mega.privacy.android.app.activities.contract.SelectFolderToCopyActivityContract
import mega.privacy.android.app.activities.contract.SelectFolderToMoveActivityContract
import mega.privacy.android.app.components.attacher.MegaAttacher
import mega.privacy.android.app.components.saver.NodeSaver
import mega.privacy.android.app.main.dialog.rubbishbin.ConfirmMoveToRubbishBinDialogFragment
import mega.privacy.android.app.modalbottomsheet.ModalBottomSheetUtil
import mega.privacy.android.app.modalbottomsheet.nodelabel.NodeLabelBottomSheetDialogFragment
import mega.privacy.android.app.presentation.extensions.isDarkMode
import mega.privacy.android.app.presentation.fileinfo.FileInfoActivity
import mega.privacy.android.app.presentation.imagepreview.ImagePreviewViewModel.Companion.FETCHER_PARAMS
import mega.privacy.android.app.presentation.imagepreview.ImagePreviewViewModel.Companion.IMAGE_NODE_FETCHER_SOURCE
import mega.privacy.android.app.presentation.imagepreview.ImagePreviewViewModel.Companion.PARAMS_CURRENT_IMAGE_NODE_ID_VALUE
import mega.privacy.android.app.presentation.imagepreview.model.ImagePreviewFetcherSource
import mega.privacy.android.app.presentation.imagepreview.model.ImagePreviewState
import mega.privacy.android.app.presentation.imagepreview.slideshow.SlideshowActivity
import mega.privacy.android.app.presentation.imagepreview.view.ImagePreviewScreen
import mega.privacy.android.app.utils.AlertsAndWarnings
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.app.utils.LinksUtil
import mega.privacy.android.app.utils.MegaNodeDialogUtil
import mega.privacy.android.app.utils.permission.PermissionUtils
import mega.privacy.android.core.ui.theme.AndroidTheme
import mega.privacy.android.domain.entity.ThemeMode
import mega.privacy.android.domain.entity.node.ImageNode
import mega.privacy.android.domain.usecase.GetThemeMode
import nz.mega.sdk.MegaApiJava
import nz.mega.sdk.MegaNode
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class ImagePreviewActivity : BaseActivity() {

    @Inject
    lateinit var getThemeMode: GetThemeMode

    @Inject
    lateinit var imagePreviewVideoLauncher: ImagePreviewVideoLauncher
    private val selectMoveFolderLauncher: ActivityResultLauncher<LongArray> =
        registerForActivityResult(
            SelectFolderToMoveActivityContract(),
            ::handleMoveFolderResult,
        )
    private val selectCopyFolderLauncher: ActivityResultLauncher<LongArray> =
        registerForActivityResult(
            SelectFolderToCopyActivityContract(),
            ::handleCopyFolderResult,
        )
    private val viewModel: ImagePreviewViewModel by viewModels()
    private val nodeSaver: NodeSaver by lazy {
        NodeSaver(
            activityLauncher = this,
            permissionRequester = this,
            snackbarShower = this,
            confirmDialogShower = AlertsAndWarnings.showSaveToDeviceConfirmDialog(this),
        )
    }
    private val nodeAttacher: MegaAttacher by lazy { MegaAttacher(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            nodeSaver.restoreState(savedInstanceState)
            nodeAttacher.restoreState(savedInstanceState)
        }
        setContent {
            val themeMode by getThemeMode().collectAsStateWithLifecycle(initialValue = ThemeMode.System)
            AndroidTheme(isDark = themeMode.isDarkMode()) {
                ImagePreviewScreen(
                    onClickBack = ::finish,
                    onClickVideoPlay = ::playVideo,
                    onClickSlideshow = ::playSlideshow,
                    onClickInfo = ::checkInfo,
                    onClickFavourite = ::favouriteNode,
                    onClickLabel = ::handleLabel,
                    onClickOpenWith = ::handleOpenWith,
                    onClickSaveToDevice = ::saveNodeToDevice,
                    onSwitchAvailableOffline = ::setAvailableOffline,
                    onClickGetLink = ::getNodeLink,
                    onClickSendTo = ::sendNodeToChat,
                    onClickShare = ::shareNode,
                    onClickRename = ::renameNode,
                    onClickMove = ::moveNode,
                    onClickCopy = ::copyNode,
                    onClickMoveToRubbishBin = ::moveNodeToRubbishBin,
                )
            }
        }
        setupFlow()
    }

    private fun setupFlow() {
        viewModel.state
            .onEach(::handleState)
            .flowWithLifecycle(lifecycle, minActiveState = Lifecycle.State.RESUMED)
            .launchIn(lifecycleScope)
    }

    private fun handleState(state: ImagePreviewState) {
        manageCopyMoveException(state.copyMoveException)
    }

    private fun handleMoveFolderResult(result: Pair<LongArray, Long>?) {
        result ?: return
        val (handles, toHandle) = result

        val targetHandle = handles.firstOrNull()
        if (targetHandle == null || targetHandle == MegaApiJava.INVALID_HANDLE || toHandle == MegaApiJava.INVALID_HANDLE) return

        viewModel.moveNode(
            context = this,
            moveHandle = targetHandle,
            toHandle = toHandle,
        )
    }

    private fun handleCopyFolderResult(result: Pair<LongArray, Long>?) {
        result ?: return
        val (handles, toHandle) = result

        val targetHandle = handles.firstOrNull()
        if (targetHandle == null || targetHandle == MegaApiJava.INVALID_HANDLE || toHandle == MegaApiJava.INVALID_HANDLE) return

        viewModel.copyNode(
            context = this,
            copyHandle = targetHandle,
            toHandle = toHandle,
        )
    }

    private fun checkInfo(imageNode: ImageNode) {
        val intent = Intent(this, FileInfoActivity::class.java).apply {
            putExtra(Constants.HANDLE, imageNode.id.longValue)
            putExtra(Constants.NAME, imageNode.name)
        }
        startActivity(intent)
    }

    private fun favouriteNode(imageNode: ImageNode) {
        viewModel.favouriteNode(imageNode)
    }

    private fun handleLabel(imageNode: ImageNode) {
        NodeLabelBottomSheetDialogFragment.newInstance(imageNode.id.longValue)
            .show(supportFragmentManager, TAG)
    }

    private fun handleOpenWith(imageNode: ImageNode) {
        ModalBottomSheetUtil.openWith(this, MegaNode.unserialize(imageNode.serializedData))
    }

    private fun saveNodeToDevice(imageNode: ImageNode) {
        viewModel.executeTransfer(transferMessage = getString(R.string.resume_paused_transfers_text)) {
            saveNode(MegaNode.unserialize(imageNode.serializedData))
        }
    }

    private fun setAvailableOffline(checked: Boolean, imageNode: ImageNode) {
        viewModel.setNodeAvailableOffline(
            activity = WeakReference(this@ImagePreviewActivity),
            setOffline = checked,
            imageNode = imageNode
        )
    }

    private fun sendNodeToChat(imageNode: ImageNode) {
        nodeAttacher.attachNode(imageNode.id.longValue)
    }

    private fun getNodeLink(imageNode: ImageNode) {
        LinksUtil.showGetLinkActivity(this, imageNode.id.longValue)
    }

    private fun shareNode(imageNode: ImageNode) {
        viewModel.shareImageNode(context = this, imageNode = imageNode)
    }

    private fun renameNode(imageNode: ImageNode) {
        val node = MegaNode.unserialize(imageNode.serializedData)
        MegaNodeDialogUtil.showRenameNodeDialog(this, node, this, null)
    }

    private fun moveNode(imageNode: ImageNode) {
        selectMoveFolderLauncher.launch(longArrayOf(imageNode.id.longValue))
    }

    private fun copyNode(imageNode: ImageNode) {
        selectCopyFolderLauncher.launch(longArrayOf(imageNode.id.longValue))
    }

    private fun moveNodeToRubbishBin(imageNode: ImageNode) {
        ConfirmMoveToRubbishBinDialogFragment.newInstance(listOf(imageNode.id.longValue))
            .show(
                supportFragmentManager,
                ConfirmMoveToRubbishBinDialogFragment.TAG
            )
    }

    private fun playSlideshow() {
        val intent = Intent(this, SlideshowActivity::class.java)
        intent.putExtras(intent.extras ?: Bundle())
        startActivity(intent)
    }

    private fun playVideo(imageNode: ImageNode) {
        lifecycleScope.launch {
            imagePreviewVideoLauncher.launchVideoScreen(
                imageNode = imageNode,
                context = this@ImagePreviewActivity,
            )
        }
    }

    private fun saveNode(node: MegaNode) {
        PermissionUtils.checkNotificationsPermission(this)
        nodeSaver.saveNode(
            node,
            highPriority = false,
            isFolderLink = node.isForeign,
            fromMediaViewer = true,
            needSerialize = true,
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nodeSaver.saveState(outState)
        nodeAttacher.saveState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        intent ?: return
        if (nodeSaver.handleActivityResult(this, requestCode, resultCode, intent)) {
            return
        }
        if (nodeAttacher.handleActivityResult(requestCode, resultCode, intent, this)) {
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        nodeSaver.handleRequestPermissionsResult(requestCode)
    }

    override fun onDestroy() {
        nodeSaver.destroy()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ImagePreviewActivity"

        fun createIntent(
            context: Context,
            imageSource: ImagePreviewFetcherSource,
            currentImageNodeId: Long,
            params: Map<String, Any> = mapOf(),
        ): Intent {
            return Intent(context, ImagePreviewActivity::class.java).apply {
                putExtra(IMAGE_NODE_FETCHER_SOURCE, imageSource)
                putExtra(PARAMS_CURRENT_IMAGE_NODE_ID_VALUE, currentImageNodeId)
                putExtra(FETCHER_PARAMS, bundleOf(*params.toList().toTypedArray()))
            }
        }
    }
}