package com.example.base44.dataClass.api

import com.google.gson.annotations.SerializedName

data class FileUploadResponse(
    @SerializedName("file_url") val fileUrl: String? = null,
    val filename: String? = null
)

// Wrapper for simple responses often used in deletes or updates
data class SimpleResponse(
    val status: String? = null,
    val message: String? = null
)
