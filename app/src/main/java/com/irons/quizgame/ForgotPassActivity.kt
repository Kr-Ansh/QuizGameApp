package com.irons.quizgame

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.irons.quizgame.databinding.ActivityForgotPassBinding

class ForgotPassActivity : AppCompatActivity() {

    lateinit var forgotPassBinding: ActivityForgotPassBinding

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        forgotPassBinding = ActivityForgotPassBinding.inflate(layoutInflater)
        setContentView(forgotPassBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        forgotPassBinding.btnReset.setOnClickListener {

            val email = forgotPassBinding.etForgotPassEmail.text.toString()
            forgotPassBinding.btnReset.isClickable = false

            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->

                if(task.isSuccessful) {

                    finish()
                    forgotPassBinding.btnReset.isClickable = true
                    Toast.makeText(applicationContext, "Please check your email", Toast.LENGTH_SHORT).show()
                } else {

                    forgotPassBinding.btnReset.isClickable = true
                    Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}