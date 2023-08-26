package com.example.bookstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.bookstore.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //array to hold the categories
    private lateinit var categoryArrayList:ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase init
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(object :TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //to call the user search
                try {
                    adapterCategory.filter.filter(s)
                }
                catch (e:Exception){

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        //logout handler
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //add category handler btn
        binding.addCateBtn.setOnClickListener {
            startActivity(Intent(this,CategoryAddActivity::class.java))
        }

        // add pdf handler btn
        binding.addPdfFab.setOnClickListener{
            startActivity(Intent(this,PdfAddActivity::class.java))
        }
    }

    private fun loadCategories() {
       //array init
        categoryArrayList = ArrayList()

        //call all the categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear the list before adding the data to it
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    //get the data as the model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add the data
                    categoryArrayList.add(model!!)
                }
                adapterCategory = AdapterCategory(this@DashboardAdminActivity,categoryArrayList)

                binding.categoryRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){// not logged in go to main screen
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        else{

            val email = firebaseUser.email

            binding.subTitleTv.text = email
        }
    }
}