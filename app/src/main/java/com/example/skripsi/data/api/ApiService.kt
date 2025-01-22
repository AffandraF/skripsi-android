package com.example.skripsi.data.api

import com.example.skripsi.data.model.ClassificationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("classify") // Endpoint API Anda
    fun classifyImage(
        @Part("user_id") userId: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<ClassificationResponse>
}
