package com.photoorganizer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.photoorganizer.R
import com.photoorganizer.databinding.ItemPhotoBinding
import com.photoorganizer.model.PhotoItem
import java.text.SimpleDateFormat
import java.util.*

class PhotoAdapter(
    private val photos: List<PhotoItem>,
    private val onPhotoSelected: (PhotoItem, Boolean) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    class PhotoViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: PhotoItem, onPhotoSelected: (PhotoItem, Boolean) -> Unit, dateFormat: SimpleDateFormat) {
            binding.apply {
                photoName.text = photo.name
                photoDate.text = dateFormat.format(photo.date)
                photoCheckbox.isChecked = photo.isSelected
                
                // Load image using Glide
                Glide.with(photoThumbnail.context)
                    .load(photo.uri)
                    .placeholder(R.color.light_gray)
                    .error(R.color.light_gray)
                    .into(photoThumbnail)
                
                photoCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    onPhotoSelected(photo, isChecked)
                }
                
                root.setOnClickListener {
                    photoCheckbox.isChecked = !photoCheckbox.isChecked
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position], onPhotoSelected, dateFormat)
    }
    
    override fun getItemCount(): Int = photos.size
}
