package com.gym.feature.workout.video

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VideoRecordingHelper {

    const val MAX_VIDEO_DURATION_SECONDS = 60

    /**
     * Создает файл в директории Movies и возвращает защищенный Content URI для камеры.
     */
    fun createVideoFileUri(context: Context, setId: Long): Pair<File, Uri> {
        val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: File(context.filesDir, "Movies").apply { mkdirs() }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val videoFile = File(moviesDir, "SET_${setId}_${timeStamp}.mp4")

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
        return Pair(videoFile, uri)
    }

    /**
     * Создает Intent для системной камеры с ограничением длительности в 60 секунд.
     */
    fun createCaptureVideoIntent(outputUri: Uri): Intent {
        return Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_VIDEO_DURATION_SECONDS)
            putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1) // Высокое качество
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
