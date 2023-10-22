package com.example.bookstore

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bookstore.adapters.AdapterPdfUser
import com.example.bookstore.databinding.FragmentBooksUserBinding
import com.example.bookstore.modles.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BooksUserFragment : Fragment {
    //binding from the fragment
    private lateinit var binding:FragmentBooksUserBinding
    public companion object{
        private const val TAG= "BOOKS_USER_TAG"

        public fun newInstance(categoryId:String,category:String,uid:String):BooksUserFragment{
            val fragment =BooksUserFragment()
            //put the data to bundle intent
            val args = Bundle()
            args.putString("categoryId",categoryId)
            args.putString("category",category)
            args.putString("uid",uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId =""
    private var category =""
    private var uid =""

    private lateinit var pdfArrayList:ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get the arguments that we passed
        val args = arguments
        if (args !=null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding =  FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        //load the pdf according the category
        Log.d(TAG, "onCreateView: Category:$category")
        if (category == "All"){
            //load all books
            loadAllBooks()
        }
        else if (category == "Most Viewed"){
            //load most viewed books
            loadMostViewedDownloadedBooks("viewsCount", download = false)
        }
        else if (category == "Most Downloaded"){
            //load the most downloaded
            loadMostViewedDownloadedBooks("downloadsCount", download = true)
        }
        else{
            //load selected category
            loadCategorizedBooks()
        }
        //search
        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    adapterPdfUser.filter.filter(s)
                }
                catch (e:Exception){
                    Log.d(TAG, "onTextChanged: SEARCH EXCPTION: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        return  binding.root
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add the data
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                //set adapter
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun loadMostViewedDownloadedBooks(orderBy: String,download:Boolean) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)// load 10 the most viewed or downloaded books
            .addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //add the data
                    pdfArrayList.add(model!!)
                }
                if(download){
                    pdfArrayList.sortByDescending {
                        it.downloadsCount
                    }
                }else{
                    pdfArrayList.sortByDescending {
                        it.viewsCount
                    }
                }

                //setup adapter
                adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                //set adapter
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)// load 10 the most viewed downloaded books
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before adding
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add the data
                        pdfArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!,pdfArrayList)
                    //set adapter
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}