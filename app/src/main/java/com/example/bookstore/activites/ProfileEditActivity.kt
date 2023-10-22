package com.example.bookstore.activites

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookstore.R
import com.example.bookstore.databinding.ActivityProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri:Uri? =null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileIv.setOnClickListener {
            showImageAttachMenu()

        }
        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var  name = ""
    private fun validateData() {
        name = binding.nameEt.text.toString().trim()

        //validate data
        if (name.isEmpty()){
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        }
        else{
            //name is entered
            if (imageUri == null){
                //update without image
                updateProfile("")
            }
            else{
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        //image path and name
        val filePathAndName = "ProfileImages/"+ firebaseAuth.uid

        //storage
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener {taskSnapshot->
                val uriTask:Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating profile..")
        
        //update to db
        val hashMap:HashMap<String,Any> = HashMap()
        hashMap["name"] = "$name"
        if (imageUri !=null){
            hashMap["profileImage"] = uploadedImageUrl
        }
        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener { 
                progressDialog.dismiss()
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e-> 
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        //dp reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    //set data
                    binding.nameEt.setText(name)


                    //set image
                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }
                    catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showImageAttachMenu(){
        val popupMenu = PopupMenu(this,binding.profileIv)
        popupMenu.menu.add(Menu.NONE,0,0,"Camera")
        popupMenu.menu.add(Menu.NONE,1,1,"Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val id = item.itemId
            if (id==0){
                //if camera clicked
                pickImageCamera()
            }
            else if (id==1){
                //if gallery clicked
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageCamera() {
        //to pick the image
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        //intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                //imageUri = data!!.data
                
                //set the image view
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                //cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set the image view
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                //cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}