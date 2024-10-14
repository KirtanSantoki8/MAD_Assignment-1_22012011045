package com.devkt.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devkt.blogapp.Model.BlogItemModel
import com.devkt.blogapp.Model.UserData
import com.devkt.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addBlogBtn.setOnClickListener {
            val blogTitle = binding.blogTitle.editText?.text.toString().trim()
            val blogDescription = binding.blogDescription.editText?.text.toString().trim()
            if (blogTitle.isEmpty() || blogDescription.isEmpty()) {
                Toast.makeText(this, "Plese Fill All the Fields", Toast.LENGTH_SHORT).show()
            }
            val user: FirebaseUser? = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val userName = user.displayName ?: "Anonymous"
                val userImageUrl = user.photoUrl ?: ""

                userReference.child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData = snapshot.getValue(UserData::class.java)
                            if (userData != null) {
                                val userNameFromDb = userData.name
                                val userImageUrlFromDb = userData.profileImage
                                val currentDate = SimpleDateFormat("dd-MM-yyyy").format(Date())
                                val blogItem = BlogItemModel(
                                    blogTitle,
                                    userNameFromDb,
                                    currentDate,
                                    blogDescription,
                                    0,
                                    userImageUrlFromDb,
                                    postId = null,
                                    userId
                                )
                                val key = databaseReference.push().key
                                if (key != null) {
                                    blogItem.postId = key
                                    val blogRef = databaseReference.child(key)
                                    blogRef.setValue(blogItem).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            startActivity(Intent(this@AddArticleActivity, MainActivity::class.java))
                                        } else {
                                            Toast.makeText(
                                                this@AddArticleActivity,
                                                "Fail to add blog",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
        }
    }
}