package com.photoorganizer.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.photoorganizer.model.PhotoItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PhotoUtils {
    
    fun organizePhotosByAge(
        context: Context,
        photos: List<PhotoItem>,
        birthDate: Date
    ): Int {
        var organizedCount = 0
        
        for (photo in photos) {
            try {
                val ageInMonths = calculateAgeInMonths(photo.date, birthDate)
                val folderName = getAgeFolderName(ageInMonths)
                
                // Create folder if it doesn't exist
                val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Son_Photos/$folderName")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                
                // Copy photo to the appropriate folder
                val fileName = getFileName(photo.uri, context.contentResolver)
                val destinationFile = File(folder, fileName)
                
                copyPhotoToFolder(photo.uri, destinationFile, context.contentResolver)
                organizedCount++
                
            } catch (e: Exception) {
                e.printStackTrace()
                // Continue with other photos even if one fails
            }
        }
        
        return organizedCount
    }
    
    private fun calculateAgeInMonths(photoDate: Date, birthDate: Date): Int {
        val calendar = Calendar.getInstance()
        val photoCalendar = Calendar.getInstance().apply { time = photoDate }
        val birthCalendar = Calendar.getInstance().apply { time = birthDate }
        
        val yearDiff = photoCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        val monthDiff = photoCalendar.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
        
        return yearDiff * 12 + monthDiff
    }
    
    private fun getAgeFolderName(ageInMonths: Int): String {
        return when {
            ageInMonths < 0 -> "before_birth"
            ageInMonths == 0 -> "newborn"
            ageInMonths == 1 -> "1st_month"
            ageInMonths == 2 -> "2nd_month"
            ageInMonths == 3 -> "3rd_month"
            ageInMonths < 12 -> "${ageInMonths}th_month"
            else -> {
                val years = ageInMonths / 12
                val months = ageInMonths % 12
                when {
                    years == 1 && months == 0 -> "1st_year"
                    years == 1 && months == 1 -> "1st_year_1st_month"
                    years == 1 && months == 2 -> "1st_year_2nd_month"
                    years == 1 && months == 3 -> "1st_year_3rd_month"
                    years == 1 -> "1st_year_${months}th_month"
                    years == 2 && months == 0 -> "2nd_year"
                    years == 2 && months == 1 -> "2nd_year_1st_month"
                    years == 2 && months == 2 -> "2nd_year_2nd_month"
                    years == 2 && months == 3 -> "2nd_year_3rd_month"
                    years == 2 -> "2nd_year_${months}th_month"
                    else -> "${years}th_year_${months}th_month"
                }
            }
        }
    }
    
    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                it.getString(nameIndex)
            } else {
                "photo_${System.currentTimeMillis()}.jpg"
            }
        } ?: "photo_${System.currentTimeMillis()}.jpg"
    }
    
    private fun copyPhotoToFolder(uri: Uri, destinationFile: File, contentResolver: ContentResolver) {
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(destinationFile)
        
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        // Add to media store
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, destinationFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Son_Photos/${destinationFile.parentFile?.name}")
        }
        
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}
