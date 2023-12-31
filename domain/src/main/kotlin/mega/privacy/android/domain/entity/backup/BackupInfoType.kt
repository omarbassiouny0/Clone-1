package mega.privacy.android.domain.entity.backup

/**
 * Enum class representing the different Backup Types mapped from nz.mega.sdk.MegaBackupInfo.type
 */
enum class BackupInfoType {
    /**
     * Represents an Invalid Backup Type
     */
    INVALID,

    /**
     * Represents a Two-Way Backup Type
     */
    TWO_WAY_SYNC,

    /**
     * Represents a Backup Type for Uploads only
     */
    UP_SYNC,

    /**
     * Represents a Backup Type for Downloads only
     */
    DOWN_SYNC,

    /**
     * Represents the Backup Type for the Primary Folder in Camera Uploads
     * in the SDK
     */
    CAMERA_UPLOADS,

    /**
     * Represents the Backup Type for the Secondary Folder (Media Uploads) in Camera Uploads
     */
    MEDIA_UPLOADS,

    /**
     * Represents the Backup Type for a Backup Folder made by a Non-Mobile Device
     */
    BACKUP_UPLOAD,
}