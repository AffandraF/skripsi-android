package com.example.skripsi.data.repository

import com.example.skripsi.data.model.HistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import javax.inject.Inject

class HistoryRepository @Inject constructor() {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("history")
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun fetchUserHistory(callback: (List<HistoryItem>) -> Unit) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return callback(emptyList())

        databaseRef.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<HistoryItem>()
                for (data in snapshot.children) {
                    val item = data.getValue(HistoryItem::class.java)
                    item?.let { items.add(it) }
                }
                callback(items)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}
