package com.gamaruzi.cifras.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Helpers em torno do PdfRenderer nativo do Android. Custo zero, sem deps.
// Render é caro (descomprime imagens); por isso roda em IO e limita a
// PDF_MAX_PAGES páginas — cifrário inteiro não vira problema.
object PdfPageRenderer {

    suspend fun render(
        context: Context,
        uri: Uri,
        widthPx: Int,
        maxPages: Int = SizeLimits.PDF_MAX_PAGES,
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val pfd: ParcelFileDescriptor = context.contentResolver
            .openFileDescriptor(uri, "r") ?: return@withContext emptyList()

        pfd.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                val total = renderer.pageCount.coerceAtMost(maxPages)
                (0 until total).map { i ->
                    renderer.openPage(i).use { page ->
                        val ratio = page.height.toFloat() / page.width.toFloat()
                        val heightPx = (widthPx * ratio).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        }
    }
}
