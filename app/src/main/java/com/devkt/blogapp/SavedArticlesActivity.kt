package com.devkt.blogapp

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.devkt.blogapp.Model.BlogItemModel
import com.devkt.blogapp.adapter.BlogAdapter
import com.devkt.blogapp.databinding.ActivitySavedArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedArticlesActivity : AppCompatActivity() {
    private val binding: ActivitySavedArticlesBinding by    lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }
    private val savedBlogsArticle = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        blogAdapter = BlogAdapter(savedBlogsArticle.filter { it.isSaved }.toMutableList())
        val recyclerView = binding.savedArticleRecycler
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users").child(userId).child("savePosts")
            userReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(postSnapshot in snapshot.children){
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as Boolean
                        if (postId != null && isSaved) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)
                                if(blogItem != null){
                                    savedBlogsArticle.add(blogItem)
                                    launch(Dispatchers.Main) {
                                        blogAdapter.updateData(savedBlogsArticle)
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference = FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            val blogData = dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        }
        catch (e:Exception){
            null
        }
    }
}