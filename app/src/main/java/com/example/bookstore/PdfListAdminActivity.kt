package com.example.bookstore

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    private var categoryId = ""
    private var category = ""

    //arraylist to hold the books
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // to get the intent from the adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set pdf or books category
        binding.subTitleTv.text = category

        //load books
        loadPdfList()

        //search
        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    adapterPdfAdmin
                    adapterPdfAdmin.filter!!.filter(s)
                }
                catch (e:Exception){
                    Log.d(TAG,"onTextChanged:${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPdfList() {
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear the list before adding
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        //get the date
                        val model = ds.getValue(ModelPdf::class.java)
                        //add them to list
                        if (model !=null){
                            pdfArrayList.add(model)
                            Log.d(TAG,"onDataChange:${model.title} ${model.categoryId}")
                        }

                    }
                    //adapter setup
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity,pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}