package mega.privacy.android.app.main.model

import mega.privacy.android.domain.entity.Feature

/**
 * State for Add Contact
 * @property isContactVerificationWarningEnabled contact verification flag is enabled or not
 * @property enabledFeatureFlags Set of enabled feature flags
 */
data class AddContactState(
    val isContactVerificationWarningEnabled: Boolean = false,
    val enabledFeatureFlags: Set<Feature> = emptySet(),
)