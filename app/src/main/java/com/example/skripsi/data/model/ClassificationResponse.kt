package com.example.skripsi.data.model

data class ClassificationResponse(
    val disease: String,
    val accuracy: Double,
    val recommendations: String
)
