package com.example.skripsi.repository

import android.util.Log
import com.example.skripsi.data.model.ClassificationResponse
import com.example.skripsi.data.api.ApiService
import com.example.skripsi.data.api.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ClassificationRepository {

    private val apiService: ApiService = RetrofitInstance.getApiService()

    fun classifyImage(userId: String, imageFile: File, callback: (ClassificationResponse?) -> Unit) {
        val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("imageFile", imageFile.name, requestBody)
        val userIdBody: RequestBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

        apiService.classifyImage(userIdBody, multipartBody).enqueue(object : Callback<ClassificationResponse> {
            override fun onResponse(call: Call<ClassificationResponse>, response: Response<ClassificationResponse>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    Log.e("API_ERROR", "Error: ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<ClassificationResponse>, t: Throwable) {
                Log.e("API_ERROR", "Failure: ${t.message}")
                callback(null)
            }
        })
    }
}
