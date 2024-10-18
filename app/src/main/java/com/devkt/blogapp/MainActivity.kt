package com.devkt.blogapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devkt.blogapp.Model.BlogItemModel
import com.devkt.blogapp.adapter.BlogAdapter
import com.devkt.blogapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth
//    private lateinit var blogAdapter: BlogAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.saveArticleBtn.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }
        binding.profileImg.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
//        blogAdapter = BlogAdapter(this,)
        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "blogs"
            )
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserProfileImage(userId)
        }
        val recyclerView = binding.blogRecyclerView
        val blogAdapter = BlogAdapter(blogItems)
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (snapshot in snapshot.children) {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        blogItems.add(blogItem)
                    }
                    blogItems.reverse()
                    blogAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog load failed", Toast.LENGTH_SHORT).show()
            }
        })
        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference =
            FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "users"
            ).child(userId)
        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if (profileImageUrl != null) {
                    Glide.with(this@MainActivity).load(profileImageUrl).into(binding.profileImg)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading Image", Toast.LENGTH_SHORT).show()
            }
        })
    }
}