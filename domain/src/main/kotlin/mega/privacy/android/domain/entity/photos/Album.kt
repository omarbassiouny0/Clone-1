package mega.privacy.android.domain.entity.photos

/**
 * Entity album
 *
 * @constructor Create empty Entity album
 */
sealed interface Album {
    /**
     * Favourite
     */
    object FavouriteAlbum : Album

    /**
     * Gif
     */
    object GifAlbum : Album

    /**
     * Raw
     */
    object RawAlbum : Album

    /**
     * User album
     *
     * @property id
     * @property title
     * @property cover
     * @property creationTime
     * @property modificationTime
     * @property isExported
     */
    data class UserAlbum(
        val id: AlbumId,
        val title: String,
        val cover: Photo?,
        val creationTime: Long,
        val modificationTime: Long,
        val isExported: Boolean,
    ) : Album
}
