package mega.privacy.android.app.usecase.data

import nz.mega.sdk.MegaNode

/**
 * Data object that encapsulates an item representing a Mega Node with extra information.
 *
 * @property node               MegaNode itself
 * @property infoText           Node information preformatted text
 * @property hasReadAccess      Flag indicating if current user can read the node.
 * @property hasReadWriteAccess Flag indicating if current user can read and write the node.
 * @property hasFullAccess      Flag indicating if current user has full permissions over the node.
 * @property hasOwnerAccess     Flag indicating if current user is the owner of the node.
 * @property isFromIncoming     Flag indicating if node is an Incoming node.
 * @property isFromRubbishBin   Flag indicating if node is a child of Rubbish bin node.
 * @property isFromInbox        Flag indicating if node is a child of Inbox node.
 * @property isFromRoot         Flag indicating if node is a child of Root node.
 * @property isAvailableOffline Flag indicating if node is available offline.
 * @property hasVersions        Flag indicating if node has versions.
 */
data class MegaNodeItem constructor(
    val name: String,
    val handle: Long,
    val infoText: String,
    val hasReadAccess: Boolean,
    val hasReadWriteAccess: Boolean,
    val hasFullAccess: Boolean,
    val hasOwnerAccess: Boolean,
    val isFromIncoming: Boolean,
    val isFromRubbishBin: Boolean,
    val isFromInbox: Boolean,
    val isFromRoot: Boolean,
    val hasVersions: Boolean,
    val isAvailableOffline: Boolean,
    val node: MegaNode? = null
)
