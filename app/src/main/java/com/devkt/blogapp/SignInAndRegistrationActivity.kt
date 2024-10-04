package com.devkt.blogapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devkt.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.google.firebase.auth.FirebaseAuth

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
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

        val action = intent.getStringExtra("action")
        if(action == "login"){
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
        }
        else if(action == "register"){
            binding.loginbtn.isEnabled = false
            binding.loginbtn.alpha = 0.5f
            binding.registerbtn.setOnClickListener {
                val register_name = binding.registerUserName.text.toString()
                val register_email = binding.registerUserEmail.text.toString()
                val register_password = binding.registerUserPassword.text.toString()
                if (register_name.isEmpty() || register_email.isEmpty() || register_password.isEmpty()){
                    Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                }
                else{
                    auth.createUserWithEmailAndPassword(register_email,register_password)
                        .addOnCompleteListener{ task ->
                            if (task.isSuccessful){

                            }
                            else{

                            }
                        }
                }
            }
        }
    }
}