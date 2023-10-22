package com.example.bookstore.filters

import android.widget.Filter
import com.example.bookstore.adapters.AdapterPdfUser
import com.example.bookstore.modles.ModelPdf


class FilterPdfUser:Filter {
    //array to hold what we want to search about
    var filterList:ArrayList<ModelPdf>
    //adapter to handel the filter or the search
    var adapterPdfUser: AdapterPdfUser

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        //value to be searched should not be empty
        if (constraint !=null&&constraint.isNotEmpty()){
            //not null or empty
            constraint = constraint.toString().lowercase()
            val filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                //validate
                if (filterList[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values =filteredModels
        }
        else{
            results.count =filterList.size
            results.values= filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfUser.notifyDataSetChanged()
    }
}