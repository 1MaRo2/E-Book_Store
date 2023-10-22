package com.example.bookstore.activites

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookstore.MyApplication
import com.example.bookstore.R
import com.example.bookstore.adapters.AdapterPdfFavorite
import com.example.bookstore.databinding.ActivityProfileBinding
import com.example.bookstore.modles.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    //array list to hold the books
    private lateinit var booksArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //rest to def
        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteBookCountTv.text = "N/A"
        binding.accountTypeTv.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!!")
        progressDialog.setCanceledOnTouchOutside(false)


        loadUserInfo()
        loadFavoriteBooks()

        //back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileEditeBtn.setOnClickListener {
            startActivity(Intent(this,ProfileEditActivity::class.java))
        }
        binding.accountStatusTv.setOnClickListener {
            if (firebaseUser.isEmailVerified){
                Toast.makeText(this, "Already verified!!", Toast.LENGTH_SHORT).show()
            }
            else{
                emailVerificationDialog()
            }
        }
    }

    private fun emailVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("Are you sure you want to send verification to this ${firebaseUser.email}")
            .setPositiveButton("SEND"){d,e->
                sendEmailVerification()

            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
            .show()
    }

    private fun sendEmailVerification() {
        progressDialog.setMessage("Sending email verfication to ${firebaseUser.email}")
        progressDialog.show()

        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Email sent please check your email ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        //check if user is verfied or not
        if(firebaseUser.isEmailVerified){
            binding.accountStatusTv.text = "Verified"

        }
        else{
            binding.accountStatusTv.text = "Not Verified"
        }


        //dp reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert time stamp to date format
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    //set image
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }
                    catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadFavoriteBooks(){
        booksArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()
                    for (ds in snapshot.children){//gets the book id
                        val bookId = "${ds.child("bookId").value}"

                        //set to model
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        //add to model list
                        booksArrayList.add(modelPdf)
                    }

                    binding.favoriteBookCountTv.text ="${ booksArrayList.size}"

                    //setup the adapter
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity,booksArrayList)
                    binding.favoriteRv.adapter = adapterPdfFavorite

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}