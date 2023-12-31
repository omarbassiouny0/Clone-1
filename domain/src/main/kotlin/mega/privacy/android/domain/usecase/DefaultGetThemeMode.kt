package mega.privacy.android.domain.usecase

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mega.privacy.android.domain.entity.ThemeMode
import mega.privacy.android.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Default implementation of [GetThemeMode]
 *
 * @property settingsRepository
 */
class DefaultGetThemeMode @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : GetThemeMode {
    override fun invoke() =
        settingsRepository.monitorStringPreference(
            key = KEY_APPEARANCE_COLOR_THEME,
            defaultValue = ThemeMode.DEFAULT.name
        ).map { it?.let { asTheme(it) } ?: ThemeMode.DEFAULT }
            .distinctUntilChanged()

    private fun asTheme(preference: String) = ThemeMode.values()
        .find { it.name.equals(preference, true) }
}

const val KEY_APPEARANCE_COLOR_THEME = "settings_appearance_color_theme"