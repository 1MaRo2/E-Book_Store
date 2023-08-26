package com.example.bookstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookstore.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // firebase init
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){// not logged in go to main screen
           binding.subTitleTv.text = "Not Logged In"
        }
        else{

            val email = firebaseUser.email

            binding.subTitleTv.text = email
        }
    }
}