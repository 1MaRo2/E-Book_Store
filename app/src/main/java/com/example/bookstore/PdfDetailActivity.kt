package com.example.bookstore

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookstore.databinding.ActivityPdfDetailBinding
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

    private lateinit var progressDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book from the intent
        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //increment book view
        MyApplication.incrementBookViewCount(bookId)

        loadBookDeatils()

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
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()

            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted")
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
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
            .addOnSuccessListener {byets->
                Log.d(TAG, "downloadBook: Book downloaded")
                saveToDownloadsFolder(byets)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download book due ${e.message}")
                Toast.makeText(this, "Failed to download book due ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun saveToDownloadsFolder(byets: ByteArray?) {
        Log.d(TAG, "saveToDownloadsFolder: saving downloded book")
        val nameWithExtention = "$bookTitle.pdf"
        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs()//to create folder if not created

            val filePath = downloadsFolder.path + "/" + nameWithExtention

            val out = FileOutputStream(filePath)
            out.write(byets)
            out.close()
            Toast.makeText(this, "Saved to Downloads Folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadsFolder: Saved to Downloads Folder")
            progressDialog.dismiss()
            incrementDownloadCount()
        }
        catch (e:Exception){
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsFolder: Failed to save due to ${e.message}")
            Toast.makeText(this, "Failed to save book due ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count : $downloadsCount")

                    //get the download count
                    if (downloadsCount == "" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }
                    //increment the count
                    val newDownloadCount:Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: new Downloads Count : $newDownloadCount")

                    val hashMap:HashMap<String,Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //update the increment to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incremented")
                        }
                        .addOnFailureListener {e->
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
}