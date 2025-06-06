package com.example.skripsi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skripsi.data.model.HistoryItem
import com.example.skripsi.data.repository.HistoryRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _historyItems = MutableLiveData<List<HistoryItem>>()
    val historyItems: LiveData<List<HistoryItem>> get() = _historyItems

    private val storageRef = FirebaseStorage.getInstance().reference

    fun fetchHistory() {
        viewModelScope.launch {
            historyRepository.fetchUserHistory { historyList ->
                _historyItems.postValue(historyList)
            }
        }
    }

    fun getImageUrl(path: String, callback: (String) -> Unit) {
        storageRef.child(path).downloadUrl.addOnSuccessListener { uri ->
            callback(uri.toString())
        }.addOnFailureListener {
            callback("")
        }
    }
}
