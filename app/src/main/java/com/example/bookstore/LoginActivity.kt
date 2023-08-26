package com.example.bookstore

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookstore.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase init auth
        firebaseAuth = FirebaseAuth.getInstance()

        //progressdialog init
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.noaccTv.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            validateDate()
        }
    }

    private  var email = ""
    private  var password= ""

    private fun validateDate() {
        //Data inputs
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //Data validate
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid email format...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this,"Enter Password...",Toast.LENGTH_SHORT).show()
        }
        else {
            loginUser()
        }
    }

    private fun loginUser() {
        // login to the firebase
        progressDialog.setMessage("Logging In...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"Login failed due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        //Check the user type in firebase
        progressDialog.setMessage("Checking User..")

         val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) { // to get the user type
                    progressDialog.dismiss()

                    val userType = snapshot.child("userType").value
                    if (userType== "user"){
                        startActivity(Intent(this@LoginActivity,DashboardUserActivity::class.java))
                        finish()
                    }
                    else if (userType == "admin"){
                        startActivity(Intent(this@LoginActivity,DashboardAdminActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}