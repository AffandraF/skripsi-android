import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.skripsi.data.repository.HistoryRepository
import com.google.firebase.database.*
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class HistoryViewModel : ViewModel() {
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("History")
    private val storageRef = FirebaseStorage.getInstance().reference
    private val _historyItems = MutableLiveData<List<HistoryRepository>>()
    val historyItems: LiveData<List<HistoryRepository>> get() = _historyItems

    fun fetchHistory() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<HistoryRepository>()
                for (data in snapshot.children) {
                    val item = data.getValue(HistoryRepository::class.java)
                    if (item != null) {
                        items.add(item)
                    }
                }
                _historyItems.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun getImageUrl(path: String, callback: (String) -> Unit) {
        storageRef.child(path).downloadUrl.addOnSuccessListener { uri ->
            callback(uri.toString())
        }.addOnFailureListener {
            callback("") // Handle failure
        }
    }
}
