package com.example.bookstore.activites

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase init auth
        firebaseAuth = FirebaseAuth.getInstance()

        //progressdialog init
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }
    var name =""
     var email =""
     var password =""

    private fun validateData() {
        // data inputs
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwrodEt.text.toString().trim()
        val cPassword = binding.cPasswrodEt.text.toString().trim()

        //data validate
        if (name.isEmpty()){ // name is empty
            Toast.makeText(this,"Enter your name..",Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){//invalid email
            Toast.makeText(this,"Invalid Email Pattern..",Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){//password is empty
            Toast.makeText(this,"Enter Password..",Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty()){//password is empty
            Toast.makeText(this,"Confirm Password..",Toast.LENGTH_SHORT).show()
        }
        else if (password != cPassword){
            Toast.makeText(this,"Password dose not match..",Toast.LENGTH_SHORT).show()
        }
        else{
            createUserAccount()
        }
    }

    private fun createUserAccount() { // create user in firebase

        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { // account created and now can see the info in the firebase
                updateUserInfo()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to create an Account due to.. ${e.message}",Toast.LENGTH_SHORT).show()
            }

    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Saving User Info...")

        val timestamp = System.currentTimeMillis()

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"]=""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Account created...",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to save an Account info due to.. ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}