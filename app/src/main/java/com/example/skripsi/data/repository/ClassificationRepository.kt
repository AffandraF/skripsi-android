package com.example.skripsi.data.repository

import com.example.skripsi.data.api.ApiService
import com.example.skripsi.data.model.ClassificationResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ClassificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun classifyImage(userId: String, imageFile: File): ClassificationResponse? {
        return try {
            val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("imageFile", imageFile.name, requestBody)
            val userIdBody: RequestBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

            return try {
                apiService.classifyImage(userIdBody, multipartBody)
            } catch (e: Exception) {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
