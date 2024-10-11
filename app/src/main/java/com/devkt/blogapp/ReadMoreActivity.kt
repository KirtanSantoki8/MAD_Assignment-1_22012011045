package com.devkt.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.devkt.blogapp.Model.BlogItemModel
import com.devkt.blogapp.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private val binding : ActivityReadMoreBinding by lazy {
        ActivityReadMoreBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")
        if (blogs != null) {
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.userName
            binding.date.text = blogs.date
            binding.blogDescription.text = blogs.post
            val userImageUrl = blogs.profileImage
            Glide.with(this)
                .load(userImageUrl)
                .into(binding.profileImage)
        }
        else{
            Toast.makeText(this, "Failed to load blog.", Toast.LENGTH_SHORT).show()
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}