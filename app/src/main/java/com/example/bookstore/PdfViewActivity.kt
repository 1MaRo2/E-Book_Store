package com.example.bookstore

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityPdfViewBinding
    //book Id
    var bookId =""
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get the books form  firebase db
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf url from db")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL:$pdfUrl")
                    //load the books
                    loadBookFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookFromUrl(pdfUrl:String) {
        Log.d(TAG, "loadBookFromUrl: Get Pdf from firebase storage using URl")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG, "loadBookFromUrl: pdf got from url")

                //to the load the pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)// to make the user swipe vertical || read vertical
                    .onPageChange{ page , pageCount->
                        val currentPage = page +1 // it start from 0 
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError {t->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError{page, t->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener{e->
                Log.d(TAG, "loadBookFromUrl: Failed to get pdf due ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}