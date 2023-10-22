package com.example.bookstore.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.MyApplication
import com.example.bookstore.activites.PdfDetailActivity
import com.example.bookstore.activites.PdfEditActivity
import com.example.bookstore.databinding.RowPdfAdminBinding
import com.example.bookstore.filters.FilterPdfAdmin
import com.example.bookstore.modles.ModelPdf

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    private var context: Context

    public var pdfArrayList: ArrayList<ModelPdf>
    private val filterList: ArrayList<ModelPdf>

    private lateinit var binding: RowPdfAdminBinding

   private var filter: FilterPdfAdmin? = null

    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //to see the data form the ui and handle it
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //to load the rest of the data

        //category id
        MyApplication.loadCategory(categoryId = categoryId, holder.categoryTv)

        //page number bass null don't need || thumbnail
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl, title, holder.pdfView, holder.progressBar, null
        )
        //size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //edit or delete book | pdf
        holder.moreBtn.setOnClickListener {
            moreOptionesDialog(model,holder)
        }

        //handler for the item clicked
        holder.itemView.setOnClickListener {
            //to redirect for the book details
            val intent=Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptionesDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //the options we want to show in the dialog
        val options = arrayOf("Edit","Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){
                dialog,position ->
                //item clicked handler
                if (position==0){
                    //send the admin to edit activity
                    val intent = Intent(context , PdfEditActivity::class.java)
                    intent.putExtra("bookId",bookId) // we bass the book id here
                    context.startActivity(intent)

                }
                else if(position==1){
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }

    override fun getFilter(): Filter {

        if (filter == null) {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //ui views from the row pdf admin

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn

    }

}