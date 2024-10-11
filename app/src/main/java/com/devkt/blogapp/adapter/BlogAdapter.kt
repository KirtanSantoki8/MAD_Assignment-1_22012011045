package com.devkt.blogapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devkt.blogapp.Model.BlogItemModel
import com.devkt.blogapp.R
import com.devkt.blogapp.ReadMoreActivity
import com.devkt.blogapp.databinding.BlogItemsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemsBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId ?: ""
            val context = binding.root.context
            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context).load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likeCount.text = blogItemModel.likeCount.toString()
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }
            val postLikeRef = databaseReference.child("blogs").child(postId).child("likes")
            val currentUserLiked = currentUser?.uid?.let { uid ->
                postLikeRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            binding.likeBtn.setImageResource(R.drawable.like_red)
                        } else {
                            binding.likeBtn.setImageResource(R.drawable.like_black)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
            binding.likeBtn.setOnClickListener {
                if (currentUser != null) {
                    handleLikedButtenClick(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "Login First", Toast.LENGTH_SHORT).show()
                }
            }
            val userReference = databaseReference.child("users").child(currentUser?.uid ?: "")
            val postSaveReference = userReference.child("savePosts").child(postId)
            postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.postSaveBtn.setImageResource(R.drawable.save_red_fill)
                    } else {
                        binding.postSaveBtn.setImageResource(R.drawable.save_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            binding.postSaveBtn.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtenClick(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "Login First", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLikedButtenClick(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemsBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).removeValue()
                                blogItemModel.likedBy?.remove(currentUser.uid)
                                updateLikedButtonImage(binding, false)
                                val newLikeCount = blogItemModel.likeCount - 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Log.e("LikeClick", "Fail to unlike $e")
                            }
                    } else {
                        userReference.child("likes").child(postId).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).setValue(true)
                                blogItemModel.likedBy?.add(currentUser.uid)
                                updateLikedButtonImage(binding, true)
                                val newLikeCount = blogItemModel.likeCount + 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Log.e("LikeClick", "Fail to like $e")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun updateLikedButtonImage(binding: BlogItemsBinding, liked: Boolean) {
        if (liked) {
            binding.likeBtn.setImageResource(R.drawable.like_black)
        } else {
            binding.likeBtn.setImageResource(R.drawable.like_red)
        }
    }

    private fun handleSaveButtenClick(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemsBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("savePosts").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("savePosts").child(postId).removeValue()
                            .addOnSuccessListener {
                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = false
                                notifyDataSetChanged()
                                val context = binding.root.context
                                Toast.makeText(context, "Post UnSaved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(context, "Post UnSaved Failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        binding.postSaveBtn.setImageResource(R.drawable.save_red)
                    } else {
                        userReference.child("savePosts").child(postId).setValue(true)
                            .addOnSuccessListener {
                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = true
                                notifyDataSetChanged()
                                val context = binding.root.context
                                Toast.makeText(context, "Post Saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(context, "Post Saved Fail", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        binding.postSaveBtn.setImageResource(R.drawable.save_red_fill)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    fun updateData(savedBlogsArticle: List<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogsArticle)
        notifyDataSetChanged()
    }
}