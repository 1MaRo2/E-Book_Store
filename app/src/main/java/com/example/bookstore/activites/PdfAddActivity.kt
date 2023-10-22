package com.example.bookstore.activites

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.databinding.ActivityPdfAddBinding
import com.example.bookstore.modles.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PdfAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //pdf holder in categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri for the pdf
    private var pdfUri: Uri? = null

    //tags
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase init
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.attatchPdfBtn.setOnClickListener {
            pdfPickIntent()
        }
        binding.submitBtn.setOnClickListener {
            valdataData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun valdataData() {
        Log.d(TAG, "validateData: validating data")
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()
        //data validate
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick PDF...", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Books/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadedPdfInfoToDb(uploadedPdfUrl, timestamp)

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadedPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading PDF Info...")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"]="$uid"
        hashMap["id"]="$timestamp"
        hashMap["title"]="$title"
        hashMap["description"]="$description"
        hashMap["categoryId"]="$selectedCategoryId"
        hashMap["url"]="$uploadedPdfUrl"
        hashMap["timestamp"]=timestamp
        hashMap["viewsCount"]=0
        hashMap["downloadsCount"]=0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"uploadedPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener {e->
                Log.d(TAG, "uploadedPdfInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }


    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading Pdf Categories")

        //init for the list
        categoryArrayList = ArrayList()

        //to load the Pdf categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category} ")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing Pdf category pick dialog")

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->

                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                // to show the category in the ui
                binding.categoryTv.text = selectedCategoryTitle
                Log.d(TAG, "categoryPickDialog: Selected CategoryID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected CategoryTitle: $selectedCategoryTitle ")
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: Starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Pick Cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }

        }
    )
}