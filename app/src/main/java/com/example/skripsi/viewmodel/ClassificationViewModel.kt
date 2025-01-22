package com.example.skripsi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.skripsi.data.model.ClassificationResponse
import com.example.skripsi.repository.ClassificationRepository
import java.io.File

class ClassificationViewModel : ViewModel() {

    private val _classificationResult = MutableLiveData<ClassificationResponse?>()
    val classificationResult: LiveData<ClassificationResponse?> get() = _classificationResult

    private val classificationRepository = ClassificationRepository()

    fun classifyImage(userId: String, imageFile: File) {
        classificationRepository.classifyImage(userId, imageFile) { response ->
            _classificationResult.value = response
        }
    }

}
