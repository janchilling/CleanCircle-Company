package com.cleancirclecompany.wasteworker.activities.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.cleancirclecompany.wasteworker.R
import com.cleancirclecompany.wasteworker.databinding.ActivityLoginPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Workers")

        binding.btnLogin.setOnClickListener {
            val email = binding.userName.text.toString()
            val password = binding.userPASS.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            // Check if user exists in database and has the correct account type
                            val currentUser = FirebaseAuth.getInstance().currentUser

                            if (currentUser != null) {
                                showLoginSuccessMessage()
                                val userQuery = databaseReference.orderByChild("email").equalTo(email)
                                Log.i("TAG", userQuery.toString())
                            } else {
                                // Current user is null
                                Toast.makeText(this@LoginPageActivity, "User not found", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Login failed
                            showInvalidCredentialsMessage()
//                            Toast.makeText(this@LoginPageActivity, "Details Incorrect", Toast.LENGTH_SHORT).show()
                        }

                    }
            }
        }
    }

    private fun showLoginSuccessMessage() {
        val successDialogLogin = Dialog(this)
        successDialogLogin.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialogLogin.setCancelable(false)
        successDialogLogin.setContentView(R.layout.login_success_dialog)

        val buttonOk: Button = successDialogLogin.findViewById(R.id.buttonOk)

        buttonOk.setOnClickListener {
            successDialogLogin.dismiss() // Dismiss the success dialog
            val intent = Intent(this@LoginPageActivity, WorkerHomeActivity::class.java)
            startActivity(intent)
        }
        // Show the success dialog
        successDialogLogin.show()
    }

    private fun showInvalidCredentialsMessage() {
        val failedDialog = Dialog(this)
        failedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        failedDialog.setCancelable(false)
        failedDialog.setContentView(R.layout.login_failed_dialog)

        val buttonFailed : Button = failedDialog.findViewById(R.id.buttonFailed)

        buttonFailed.setOnClickListener {
            failedDialog.dismiss()
        }
        failedDialog.show()
    }
}