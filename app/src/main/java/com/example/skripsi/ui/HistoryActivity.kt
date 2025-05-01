package com.example.skripsi.ui

import com.example.skripsi.viewmodel.HistoryViewModel
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsi.databinding.ActivityHistoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = HistoryAdapter(emptyList(), viewModel)

        viewModel.historyItems.observe(this@HistoryActivity) { historyList ->
            (binding.rvHistory.adapter as HistoryAdapter).updateData(historyList)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchHistory()
    }
}
