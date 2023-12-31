package mega.privacy.android.domain.entity

import mega.privacy.android.domain.entity.shares.AccessPermission

/**
 * Share data
 *
 * @property user
 * @property nodeHandle
 * @property access
 * @property timeStamp
 * @property isPending
 * @param isVerified
 */
data class ShareData(
    val user: String?,
    val nodeHandle: Long,
    val access: AccessPermission,
    val timeStamp: Long,
    val isPending: Boolean,
    val isVerified: Boolean,
)