package mega.privacy.android.core.ui.controls.sheets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import mega.privacy.android.core.R
import mega.privacy.android.core.ui.controls.lists.MenuActionHeader
import mega.privacy.android.core.ui.controls.lists.MenuActionListTile
import mega.privacy.android.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.core.ui.theme.AndroidTheme
import mega.privacy.android.core.ui.theme.MegaTheme
import mega.privacy.android.core.ui.theme.extensions.textColorPrimary

/**
 * BottomSheet
 *
 * @param modalSheetState state of [ModalBottomSheetLayout]
 * @param sheetHeader header composable for the bottom sheet
 * @param content scaffold/layout in which bottom sheet is shown
 * @param headerDividerPadding header divider padding
 * @param sheetBody list of composable which will be included in sheet content below sheet header
 * @param scrimColor when bottom sheet displayed this color will overlay on background content
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheet(
    modalSheetState: ModalBottomSheetState,
    sheetHeader: @Composable () -> Unit,
    sheetBody: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    headerDividerPadding: Dp = 16.dp,
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
    content: (@Composable () -> Unit)? = null,
) {
    BottomSheet(
        modalSheetState = modalSheetState,
        sheetBody = {
            sheetHeader()
            Divider(Modifier.padding(start = headerDividerPadding))
            sheetBody()
        },
        modifier = modifier,
        scrimColor = scrimColor,
        content = content
    )
}

/**
 * BottomSheet
 *
 * @param modalSheetState state of [ModalBottomSheetLayout]
 * @param content scaffold/layout in which bottom sheet is shown
 * @param sheetBody list of composable which will be included in sheet content below sheet header
 * @param scrimColor when bottom sheet displayed this color will overlay on background content
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheet(
    modalSheetState: ModalBottomSheetState,
    sheetBody: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
    content: (@Composable () -> Unit)? = null,
) {
    val roundedCornerRadius by animateDpAsState(
        if (modalSheetState.currentValue == ModalBottomSheetValue.Expanded) 0.dp else 12.dp,
        label = "rounded corner radius animation"
    )

    val navigationBarHeight = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val statusBarColor = remember {
        derivedStateOf {
            val alpha = when (modalSheetState.targetValue) {
                ModalBottomSheetValue.Hidden -> 1f - modalSheetState.progress
                ModalBottomSheetValue.Expanded -> 1f
                ModalBottomSheetValue.HalfExpanded -> {
                    if (modalSheetState.currentValue == ModalBottomSheetValue.Hidden) {
                        modalSheetState.progress
                    } else {
                        1f
                    }
                }
            }
            Color.Black.copy(alpha = 0.32f * alpha)
        }
    }
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(
        statusBarColor.value,
        MegaTheme.colors.isLight
    )

    ModalBottomSheetLayout(
        modifier = modifier.padding(bottom = navigationBarHeight),
        sheetShape = RoundedCornerShape(
            topStart = roundedCornerRadius,
            topEnd = roundedCornerRadius
        ),
        sheetState = modalSheetState,
        scrimColor = scrimColor,
        sheetContent = { sheetBody() },
    ) {
        content?.invoke()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@CombinedThemePreviews
@Composable
private fun BottomSheetPreview() {
    val coroutineScope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.HalfExpanded,
        skipHalfExpanded = false,
    )
    AndroidTheme(isDark = isSystemInDarkTheme()) {
        BottomSheet(
            modalSheetState = modalSheetState,
            sheetHeader = {
                MenuActionHeader(text = "Header")
            },
            sheetBody = {
                LazyColumn {
                    items(100) {
                        MenuActionListTile(
                            text = "title $it",
                            icon = painterResource(id = R.drawable.ic_folder_list)
                        )
                    }
                }
            }) {
            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color.Yellow),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            modalSheetState.show()
                        }
                    }) {
                        Text(
                            text = "Show modal sheet",
                            style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.textColorPrimary),
                        )
                    }
                }
            }
        }
    }
}

