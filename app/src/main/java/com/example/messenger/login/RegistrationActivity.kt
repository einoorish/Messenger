package com.example.messenger.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.messenger.R
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val TAG: String = "RegistrationActivity"

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        btn_register.setOnClickListener{
            performRegister()
        }

        tv_already_have_an_account.setOnClickListener {
            var intent = Intent (this, LoginActivity::class.java)
            startActivity(intent)
        }

        photo_btn.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.type ="image/*"
            startActivityForResult(intent, 0)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data!=null){
            Log.d(TAG, "photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            photo_circle.setImageBitmap(bitmap)

            photo_btn.alpha = 0f
        }
    }

    private fun performRegister(){

        val email = et_email.text.toString()
        val password = et_password.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(baseContext, "You have to fill the fields", Toast.LENGTH_SHORT).show()
        }else{

            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "createUserWithEmail:success"+user!!.uid)

                    uploadImageToFirebaseStorage()
                    // updateUI(user)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", it.exception)
                    Toast.makeText(baseContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri == null) return
        var filename = UUID.randomUUID().toString()
        var ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d(TAG, "photo uploaded")

            ref.downloadUrl.addOnSuccessListener {
                // it.toString() - photo url

                saveUserToFirebase(it.toString())

            }
        }
    }

    private fun saveUserToFirebase(imageUrl:String){
        val ref = FirebaseDatabase.getInstance().getReference("/users/${auth.currentUser!!.uid}")
        val user = User(auth.currentUser!!.uid, et_name.text.toString(), imageUrl)

        ref.setValue(user).addOnSuccessListener {
            Log.d(TAG, "new user added to database")
        }.addOnFailureListener {
            Log.d(TAG, it.message.toString())
        }

    }




}
