package com.example.skripsi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.model.ClassificationResponse
import com.example.skripsi.data.repository.ClassificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ClassificationViewModel @Inject constructor(
    private val classificationRepository: ClassificationRepository
) : ViewModel() {

    private val _classificationResult = MutableLiveData<ClassificationResponse?>()
    val classificationResult: LiveData<ClassificationResponse?> get() = _classificationResult

    fun classifyImage(userId: String, imageFile: File) {
        viewModelScope.launch {
            val result = classificationRepository.classifyImage(userId, imageFile)
            _classificationResult.postValue(result)
        }
    }

}