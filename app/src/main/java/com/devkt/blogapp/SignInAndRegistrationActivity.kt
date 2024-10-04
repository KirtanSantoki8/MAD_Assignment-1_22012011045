package com.devkt.blogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.service.autofill.UserData
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devkt.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.devkt.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://blog-app-1f5b8-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        val action = intent.getStringExtra("action")
        if (action == "login") {
            binding.editTextTextEmailAddress.visibility = View.VISIBLE
            binding.editTextTextPassword.visibility = View.VISIBLE
            binding.loginbtn.visibility = View.VISIBLE

            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f
            binding.registerbtn.isEnabled = false
            binding.registerbtn.alpha = 0.5f
            binding.registerUserName.visibility = View.GONE
            binding.registerUserEmail.visibility = View.GONE
            binding.registerUserPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE

            binding.loginbtn.setOnClickListener {
                val loginEmail = binding.editTextTextEmailAddress.text.toString()
                val loginPassword = binding.editTextTextPassword.text.toString()
                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All Details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        } else if (action == "register") {
            binding.loginbtn.isEnabled = false
            binding.loginbtn.alpha = 0.5f
            binding.registerbtn.setOnClickListener {
                val register_name = binding.registerUserName.text.toString()
                val register_email = binding.registerUserEmail.text.toString()
                val register_password = binding.registerUserPassword.text.toString()
                if (register_name.isEmpty() || register_email.isEmpty() || register_password.isEmpty()) {
                    Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(register_email, register_password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                auth.signOut()
                                user?.let {
                                    val userReference = database.getReference("users")
                                    val userId = user.uid
                                    val userData = com.devkt.blogapp.Model.UserData(
                                        register_name,
                                        register_email
                                    )
                                    userReference.child(userId).setValue(userData)
                                    val storageReference =
                                        storage.reference.child("profile_images/$userId.jpg")
                                    storageReference.putFile(imageUri!!)
                                    Toast.makeText(
                                        this,
                                        "Registered Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, WelcomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            }
        }
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null)
            imageUri = data.data
        Glide.with(this).load(imageUri)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.registerUserImage)
    }
}