package com.example.bookstore.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.filters.FilterPdfUser
import com.example.bookstore.MyApplication
import com.example.bookstore.activites.PdfDetailActivity
import com.example.bookstore.databinding.RowPdfAdminBinding
import com.example.bookstore.modles.ModelPdf

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {
    //context
    private var context: Context

    //arraylist to hold the books
    public var pdfArrayList: ArrayList<ModelPdf>
    //arraylist to hold filtered pdfs
    public var filterList:ArrayList<ModelPdf>

    //view binding
    private lateinit var binding: RowPdfAdminBinding

    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        //inflate and bind from the rowopdfuser xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        //get the date then set it and handle the btns
        //get date
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //time convert
        val date = MyApplication.formatTimeStamp(timestamp)

        //set the data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)
        MyApplication.loadCategory(categoryId, holder.categoryTy)
        MyApplication.loadPdfSize(url, title, holder.sizeTv)

       //open pdf details page
       holder.itemView.setOnClickListener {
           val intent = Intent(context, PdfDetailActivity::class.java)
           intent.putExtra("bookId",bookId)
           context.startActivity(intent)
       }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // return the size of the list
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfUser(filterList,this)
        }
        return filter as FilterPdfUser
    }


    //the holder for row pdf user class
    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTy = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv


    }



}