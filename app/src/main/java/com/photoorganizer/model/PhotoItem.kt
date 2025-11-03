package com.photoorganizer.model

import android.net.Uri
import java.util.Date

data class PhotoItem(
    val uri: Uri,
    val name: String,
    val date: Date,
    var isSelected: Boolean = false
)

