package mega.privacy.android.app.uploadFolder.list.data

import java.io.Serializable

/**
 * Data class containing all the required info to start the upload of a folder's item.
 *
 * @property absolutePath   The absolute path of the file to upload.
 * @property name           The name of the file to upload.
 * @property size           The size of the file to upload.
 * @property lastModified   The last modified date of the file to upload.
 * @property parentHandle   The parent handle of the node in which the file has to be uploaded.
 * @property renameName     The rename name if the file has to be renamed after uploading, null otherwise.
 */
data class UploadFolderResult(
    val absolutePath: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val parentHandle: Long?,
    val renameName: String? = null
) : Serializable
