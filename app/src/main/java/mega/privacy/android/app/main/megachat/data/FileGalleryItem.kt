package mega.privacy.android.app.main.megachat.data

import android.graphics.Bitmap
import androidx.recyclerview.widget.DiffUtil

/**
 * View item that represents a Contact Request at UI level.
 *
 * @property id         File id
 * @property title       File title
 * @property fileUri    File URI
 * @property dateAdded  Date added
 * @property thumbnail  Image thumbnail
 */
data class FileGalleryItem constructor(
    val id: Long,
    val title: String? = null,
    var fileUri: String? = null,
    var dateAdded: String? = null,
    var thumbnail: Bitmap? = null
) {

    class DiffCallback : DiffUtil.ItemCallback<FileGalleryItem>() {

        override fun areItemsTheSame(oldItem: FileGalleryItem, newItem: FileGalleryItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: FileGalleryItem,
            newItem: FileGalleryItem
        ): Boolean =
            oldItem == newItem
    }
}