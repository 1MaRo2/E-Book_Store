package com.example.bookstore.activites

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {


    private lateinit var binding: ActivityForgotPasswordBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateDate()
        }
    }

    private var email = ""
    private fun validateDate() {
        email = binding.emailEt.text.toString().trim()

        if (email.isEmpty()){
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email pattern", Toast.LENGTH_SHORT).show()
        }
        else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        progressDialog.setMessage("Sending password reset email to  $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Email sent to \n$email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()
                
            }
    }
}