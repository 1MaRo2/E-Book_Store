package com.example.bookstore

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //to get the pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    Log.d(TAG, "loadPdfSize: Got metadata")
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size in Bytes$bytes")

                    //convert
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)}MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)}KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)}bytes"
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")

                }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_THUMBNNAIL_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfSize: Size in Bytes$bytes")

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)


                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG,"loadPdfFromUrlSinglePage: Pages:$nbPages")
                            progressBar.visibility = View.INVISIBLE
                            // to set the pages nums
                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"
                            }
                        }

                        .load()
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")

                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            //load the categories from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //getter of the category
                        val category = "${snapshot.child("category").value}"

                        //setter of the category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }

        fun deleteBook(context :Context,bookId:String,bookUrl:String,bookTitle:String){
            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleting....")

            val progressDialog= ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")
            val storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from storage")
                    Log.d(TAG, "deleteBook: Deleting from db now..")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted..", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: Deleted from db ")
                        }
                        .addOnFailureListener{e->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context,"Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()

                        }
                }
                .addOnFailureListener{e->
                    Log.d(TAG, "deleteBook: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context,"Failed to delete due to ${e.message}",Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementBookViewCount(bookId:String){
            //get book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == ""||viewsCount=="null"){
                            viewsCount = "0";
                        }
                        // increment views count
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db
                        val hashMap = HashMap<String,Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                } )
        }

    }

}