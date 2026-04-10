package com.zash60.zrec.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.zash60.zrec.data.model.VideoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository for accessing recorded video files from MediaStore.
 */
class VideoRepository(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * The folder name where Zrec saves recordings.
     */
    private val relativePath = "Movies/Zrec"

    /**
     * Returns a Flow that emits the list of Zrec recordings, ordered by date descending.
     */
    fun getVideos(): Flow<List<VideoFile>> = flow {
        val videos = queryVideos()
        emit(videos)
    }.flowOn(Dispatchers.IO)

    /**
     * Returns a single video by its MediaStore ID.
     */
    fun getVideoById(id: Long): Flow<VideoFile?> = flow {
        val uri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
        )
        val video = queryVideo(uri)
        emit(video)
    }.flowOn(Dispatchers.IO)

    /**
     * Deletes a video from MediaStore and the filesystem.
     */
    fun deleteVideo(videoId: Long): Boolean {
        val uri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId
        )
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Scoped storage: use MediaStore delete with pending intent
                // For simplicity, we use contentResolver.delete which triggers system dialog
                contentResolver.delete(uri, null, null) > 0
            } else {
                contentResolver.delete(uri, null, null) > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun queryVideos(): List<VideoFile> {
        val videos = mutableListOf<VideoFile>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.RELATIVE_PATH,
        )

        val selection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%Zrec%")
        val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
            val widthColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                videos.add(
                    VideoFile(
                        id = id,
                        uri = uri,
                        displayName = it.getString(nameColumn),
                        durationMs = it.getLong(durationColumn),
                        sizeBytes = it.getLong(sizeColumn),
                        dateTakenMs = it.getLong(dateColumn),
                        width = it.getInt(widthColumn),
                        height = it.getInt(heightColumn),
                    )
                )
            }
        }

        return videos
    }

    private fun queryVideo(uri: android.net.Uri): VideoFile? {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                val widthColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

                val id = it.getLong(idColumn)
                return VideoFile(
                    id = id,
                    uri = uri,
                    displayName = it.getString(nameColumn),
                    durationMs = it.getLong(durationColumn),
                    sizeBytes = it.getLong(sizeColumn),
                    dateTakenMs = it.getLong(dateColumn),
                    width = it.getInt(widthColumn),
                    height = it.getInt(heightColumn),
                )
            }
        }
        return null
    }
}
