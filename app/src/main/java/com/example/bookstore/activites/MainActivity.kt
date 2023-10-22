package com.example.bookstore.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookstore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

// login handler
        binding.longinBtn.setOnClickListener {
             startActivity(Intent(this, LoginActivity::class.java))
        }
// skip login handler
        binding.skipBtn.setOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }
    }
}