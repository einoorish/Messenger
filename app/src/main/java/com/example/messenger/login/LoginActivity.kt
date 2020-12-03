package com.example.messenger.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.messenger.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.et_email
import kotlinx.android.synthetic.main.activity_login.et_password

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_tv_already_have_an_account.setOnClickListener{
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        btn_login.setOnClickListener {

            val email = et_email.text.toString()
            val password = et_password.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(baseContext, "You have to fill the fields", Toast.LENGTH_SHORT).show()
            } else {

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener {
                    Toast.makeText(baseContext, "Successful log in.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(baseContext, "Wrong credentials.", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}
