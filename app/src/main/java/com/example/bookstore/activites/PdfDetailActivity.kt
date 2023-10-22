package com.example.bookstore.activites

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookstore.Constants
import com.example.bookstore.MyApplication
import com.example.bookstore.R
import com.example.bookstore.adapters.AdapterComment
import com.example.bookstore.databinding.ActivityPdfDetailBinding
import com.example.bookstore.databinding.DialogCommentAddBinding
import com.example.bookstore.modles.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream


class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    // book Id
    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""

    private var isMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var commentArrayList: ArrayList<ModelComment>

    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book from the intent
        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser !=null){
            checkIsFavorite()
        }

        //increment book view
        MyApplication.incrementBookViewCount(bookId)

        loadBookDeatils()
        showComments()

        //back btn
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //read book btn
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId);
            startActivity(intent)
        }

        //download book btn
        binding.downloadBookBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()

            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted")
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        // handler for the fev btn
        binding.favoriteBtn.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "You're not logged in ", Toast.LENGTH_SHORT).show()
            } else {
                if (isMyFavorite){
                    MyApplication.removeFromFavorite(this,bookId)
                }
                else{
                    addToFavorite()
                }

            }
        }

        //handler for comment btn
        binding.addCommentBtn.setOnClickListener {
            //make sure that user is logged in
            if (firebaseAuth.currentUser == null){
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()
            }
            else{
                addCommentDialog()
            }
        }
    }

    private fun showComments() {
        //init arraylist
        commentArrayList = ArrayList()

        //db path
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelComment::class.java)
                        commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@PdfDetailActivity,commentArrayList)

                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var comment =""
    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this,R.style.CustomeDialog)
        builder.setView(commentAddBinding.root)

        //create and show the alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handler for dismiss dialog
        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }

        //handler for add comment
        commentAddBinding.submitBtn.setOnClickListener {
            //get data
            comment = commentAddBinding.commentEt.text.toString().trim()
            //validate
            if (comment.isEmpty()){
                Toast.makeText(this, "Enter Comment..", Toast.LENGTH_SHORT).show()
            }
            else{
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        progressDialog.setMessage("Adding Comment")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener { 
                progressDialog.dismiss()
                Toast.makeText(this, "Comment added..", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add comment due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "inCreate: STORAGE PERMISSION is granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun downloadBook() {
        Log.d(TAG, "downloadBook: Downloading Book")
        progressDialog.setMessage("Downloading Book")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { byets ->
                Log.d(TAG, "downloadBook: Book downloaded")
                saveToDownloadsFolder(byets)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download book due ${e.message}")
                Toast.makeText(this, "Failed to download book due ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }

    }

    private fun saveToDownloadsFolder(byets: ByteArray?) {
        Log.d(TAG, "saveToDownloadsFolder: saving downloded book")
        val nameWithExtention = "$bookTitle.pdf"
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs()//to create folder if not created

            val filePath = downloadsFolder.path + "/" + nameWithExtention

            val out = FileOutputStream(filePath)
            out.write(byets)
            out.close()
            Toast.makeText(this, "Saved to Downloads Folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadsFolder: Saved to Downloads Folder")
            progressDialog.dismiss()
            incrementDownloadCount()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsFolder: Failed to save due to ${e.message}")
            Toast.makeText(this, "Failed to save book due ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count : $downloadsCount")

                    //get the download count
                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }
                    //increment the count
                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: new Downloads Count : $newDownloadCount")

                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //update the increment to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incremented")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: Failed to increment due to ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookDeatils() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get the data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //date format
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    // load pdf category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    //load pdf thumbnail
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )
                    //load pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //set the data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Checking if book is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isMyFavorite = snapshot.exists()
                    if (isMyFavorite){
                        Log.d(TAG, "onDataChange: available in favorite")
                        //we draw the top icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_filled_white,0,0)
                        binding.favoriteBtn.text ="Remove Favorite"
                    }
                    else{
                        Log.d(TAG, "onDataChange: not available in favorite")
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_border_white,0,0)
                        binding.favoriteBtn.text = "Add to Favorite"
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: Adding to fev")
        val timestamp = System.currentTimeMillis()

        val hashMap=HashMap<String,Any>()
        hashMap["bookId"]= bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Adding to fev")
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                Log.d(TAG, "addToFavorite: Failed to add due to ${e.message}")
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }


}