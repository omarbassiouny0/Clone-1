package mega.privacy.android.data.repository.files

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import com.shockwave.pdfium.PdfiumCore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import mega.privacy.android.data.constant.CacheFolderConstant
import mega.privacy.android.data.constant.FileConstant
import mega.privacy.android.data.gateway.CacheGateway
import mega.privacy.android.data.gateway.api.MegaApiGateway
import mega.privacy.android.domain.qualifier.IoDispatcher
import mega.privacy.android.domain.repository.files.PdfRepository
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Pdf repository impl.
 *
 * @property context
 * @property ioDispatcher
 */
class PdfRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val megaApi: MegaApiGateway,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val cacheGateway: CacheGateway,
) : PdfRepository {

    override suspend fun createThumbnail(nodeHandle: Long, localFile: File) =
        createThumbnailOrPreview(nodeHandle = nodeHandle, localFile = localFile, isPreview = false)

    override suspend fun createPreview(nodeHandle: Long, localFile: File) =
        createThumbnailOrPreview(nodeHandle = nodeHandle, localFile = localFile, isPreview = true)

    private suspend fun createThumbnailOrPreview(
        nodeHandle: Long,
        localFile: File,
        isPreview: Boolean,
    ) = withContext(ioDispatcher) {
        val pdfiumCore = PdfiumCore(context)
        val pageNumber = 0
        val fileName = megaApi.handleToBase64(nodeHandle) + FileConstant.JPG_EXTENSION
        val file = if (isPreview) {
            cacheGateway.getCacheFile(CacheFolderConstant.PREVIEW_FOLDER, fileName)
        } else {
            cacheGateway.getCacheFile(CacheFolderConstant.THUMBNAIL_FOLDER, fileName)
        } ?: return@withContext null

        val out = FileOutputStream(file)
        val pdfDocument = pdfiumCore.newDocument(
            ParcelFileDescriptor.open(localFile, ParcelFileDescriptor.MODE_READ_ONLY)
        )

        try {
            pdfiumCore.openPage(pdfDocument, pageNumber)
            val width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber)
            val height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNumber, 0, 0, width, height)
            val resizedBitmap = if (isPreview) {
                val resize = if (width > height) 1000f / width else 1000f / height
                val resizeWidth = (width * resize).toInt()
                val resizeHeight = (height * resize).toInt()
                Bitmap.createScaledBitmap(bitmap, resizeWidth, resizeHeight, false)
            } else {
                Bitmap.createScaledBitmap(bitmap, 200, 200, false)
            }
            val result = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)

            if (result) {
                Timber.d("Compress OK!")
                return@withContext file.absolutePath
            } else {
                Timber.w("Not Compress")
                return@withContext null
            }
        } finally {
            pdfiumCore.closeDocument(pdfDocument)
            out.close()
        }
    }
}