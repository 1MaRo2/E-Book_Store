package com.example.bookstore.filters

import android.widget.Filter
import com.example.bookstore.adapters.AdapterPdfAdmin
import com.example.bookstore.modles.ModelPdf


//filter and search(pdf) from the RcView
class FilterPdfAdmin : Filter {
    //array list  for the search
    var filterList: ArrayList<ModelPdf>

    //adapter to filter
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint//the value we got from the search
        val result = FilterResults()
        //PDFView
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                if (filterList[i].title.lowercase().contains(constraint))
                { // the searched value is similar to the value in the list
                    filteredModels.add(filterList[i])
                }
            }
            result.count = filteredModels.size
            result.values = filteredModels
        }
        else{ // the value is not in the search null or empty
            result.count = filterList.size
            result.values = filterList
        }
        return result
    }


    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply the filter search changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        //show the changes
        adapterPdfAdmin.notifyDataSetChanged()
    }

}