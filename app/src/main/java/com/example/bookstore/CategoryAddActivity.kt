package com.example.bookstore

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookstore.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // firebase init
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        //back btn handler
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //submit btn handler
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {

        category = binding.categoryEt.text.toString().trim()

        //data validation
        if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category .. ", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //firebase db hashmap
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //adding category to firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Category Added Successfully..", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}