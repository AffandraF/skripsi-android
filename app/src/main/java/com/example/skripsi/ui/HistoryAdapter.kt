package com.example.skripsi.ui

import com.example.skripsi.viewmodel.HistoryViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skripsi.R
import com.example.skripsi.data.model.HistoryItem
import com.example.skripsi.databinding.ItemHistoryBinding

class HistoryAdapter(
    private var historyList: List<HistoryItem>,
    private val viewModel: HistoryViewModel
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(historyItem: HistoryItem) {
            binding.tvDiseaseName.text = historyItem.diseaseName
            binding.tvConfidence.text = historyItem.confidence
            binding.tvDate.text = historyItem.date

            viewModel.getImageUrl(historyItem.imagePath) { imageUrl ->
                Glide.with(binding.imageDisease.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(binding.imageDisease)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount() = historyList.size

    // Fungsi untuk update data
    fun updateData(newList: List<HistoryItem>) {
        historyList = newList
        notifyDataSetChanged()
    }
}
