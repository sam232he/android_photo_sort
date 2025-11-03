package com.photoorganizer

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.photoorganizer.adapter.PhotoAdapter
import com.photoorganizer.databinding.ActivityMainBinding
import com.photoorganizer.model.PhotoItem
import com.photoorganizer.utils.PhotoUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var photoAdapter: PhotoAdapter
    private var selectedPhotos = mutableListOf<PhotoItem>()
    private var birthDate: Date? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadPhotos()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
        }
    }
    
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val photos = uris.map { uri ->
                PhotoItem(
                    uri = uri,
                    name = getFileName(uri),
                    date = getPhotoDate(uri),
                    isSelected = false
                )
            }
            selectedPhotos.addAll(photos)
            updatePhotoList()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        checkPermissions()
    }
    
    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(selectedPhotos) { photo, isSelected ->
            photo.isSelected = isSelected
            updateOrganizeButton()
        }
        
        binding.photosRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = photoAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.birthDateInput.setOnClickListener {
            showDatePicker()
        }
        
        binding.selectPhotosButton.setOnClickListener {
            photoPickerLauncher.launch("image/*")
        }
        
        binding.selectAllButton.setOnClickListener {
            val allSelected = selectedPhotos.all { it.isSelected }
            selectedPhotos.forEach { it.isSelected = !allSelected }
            photoAdapter.notifyDataSetChanged()
            updateSelectAllButton()
            updateOrganizeButton()
        }
        
        binding.organizeButton.setOnClickListener {
            organizePhotos()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time
                birthDate = selectedDate
                binding.birthDateInput.setText(dateFormat.format(selectedDate))
                updateOrganizeButton()
            },
            year, month, day
        )
        datePickerDialog.show()
    }
    
    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            loadPhotos()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }
    
    private fun loadPhotos() {
        // This method can be used to load existing photos from gallery
        // For now, we'll rely on the photo picker
    }
    
    private fun updatePhotoList() {
        photoAdapter.notifyDataSetChanged()
        updatePhotoCount()
        updateSelectAllButton()
        updateOrganizeButton()
    }
    
    private fun updatePhotoCount() {
        val count = selectedPhotos.size
        binding.photoCountText.text = if (count > 0) {
            getString(R.string.photos_selected, count)
        } else {
            getString(R.string.no_photos_selected)
        }
    }
    
    private fun updateSelectAllButton() {
        val allSelected = selectedPhotos.isNotEmpty() && selectedPhotos.all { it.isSelected }
        binding.selectAllButton.text = if (allSelected) {
            getString(R.string.deselect_all)
        } else {
            getString(R.string.select_all)
        }
    }
    
    private fun updateOrganizeButton() {
        val hasPhotos = selectedPhotos.any { it.isSelected }
        val hasBirthDate = birthDate != null
        binding.organizeButton.isEnabled = hasPhotos && hasBirthDate
    }
    
    private fun organizePhotos() {
        if (birthDate == null) {
            Toast.makeText(this, "Please select birth date first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val selectedPhotosToOrganize = selectedPhotos.filter { it.isSelected }
        if (selectedPhotosToOrganize.isEmpty()) {
            Toast.makeText(this, "Please select photos to organize", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val organizedCount = PhotoUtils.organizePhotosByAge(
                this,
                selectedPhotosToOrganize,
                birthDate!!
            )
            
            Toast.makeText(
                this,
                getString(R.string.organization_complete),
                Toast.LENGTH_LONG
            ).show()
            
            // Clear selected photos after successful organization
            selectedPhotos.clear()
            photoAdapter.notifyDataSetChanged()
            updatePhotoCount()
            updateSelectAllButton()
            updateOrganizeButton()
            
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.organization_failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun getFileName(uri: android.net.Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                it.getString(nameIndex)
            } else {
                "Unknown"
            }
        } ?: "Unknown"
    }
    
    private fun getPhotoDate(uri: android.net.Uri): Date {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val dateIndex = it.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            if (it.moveToFirst() && dateIndex >= 0) {
                val timestamp = it.getLong(dateIndex)
                if (timestamp > 0) {
                    Date(timestamp)
                } else {
                    Date()
                }
            } else {
                Date()
            }
        } ?: Date()
    }
}

