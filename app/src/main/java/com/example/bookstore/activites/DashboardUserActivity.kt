package com.example.bookstore.activites

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookstore.BooksUserFragment
import com.example.bookstore.databinding.ActivityDashboardUserBinding
import com.example.bookstore.modles.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // firebase init
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        //init list
        categoryArrayList = ArrayList()

        //load the categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()
                //add data to models
                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded", 1, "")

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                //add to viewpageradapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    )
                ,modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    )
                    ,modelMostViewed.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}"
                    )
                    ,modelMostDownloaded.category
                )

                //refresh the list
                viewPagerAdapter.notifyDataSetChanged()

                //load from db
                for (ds in snapshot.children){
                    //get data from db
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to vpa
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        )
                        ,model.category
                    )
                    //refresh list again
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })



        //adapter setup
        viewPager.adapter = viewPagerAdapter

    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        //hols the list of the fragments
        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()

        //list of titles of categories
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            //add the fragment (will be passed as parameter in the list)
            fragmentList.add(fragment)
            //title will be passed as parameter
            fragmentTitleList.add(title)

        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {// not logged in go to main screen
            binding.subTitleTv.text = "Not Logged In"
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        } else {

            val email = firebaseUser.email

            binding.subTitleTv.text = email

            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }
}