package com.devkt.blogapp.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devkt.blogapp.R
import com.devkt.blogapp.SignInAndRegistrationActivity
import com.devkt.blogapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private val binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
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
        binding.loginbtn.setOnClickListener {
            val intent = Intent(this, SignInAndRegistrationActivity::class.java)
            intent.putExtra("action","login")
            startActivity(intent)

        }
        binding.registerbtn.setOnClickListener {
            val intent = Intent(this, SignInAndRegistrationActivity::class.java)
            intent.putExtra("action","register")
            startActivity(intent)
        }
    }
}