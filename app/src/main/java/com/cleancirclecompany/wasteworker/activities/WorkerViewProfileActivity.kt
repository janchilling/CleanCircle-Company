package com.cleancirclecompany.wasteworker.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.cleancirclecompany.wasteworker.activities.models.WorkerModel
import com.cleancirclecompany.wasteworker.databinding.ActivityWorkerViewProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkerViewProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerViewProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and get the current user
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        binding.btnBackHome.setOnClickListener {
            val intent = Intent(this, WorkerHomeActivity::class.java)
            startActivity(intent)
        }

        if (currentUser != null){
            val useremail = currentUser.email
            val dbRef = FirebaseDatabase.getInstance().getReference("Workers")
            dbRef.orderByChild("email").equalTo(useremail).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        //get the user data
                        val user = snapshot.children.first().getValue(WorkerModel::class.java)
                        //set the user data to view activity
                        binding.userNameprofile.text = user?.fullName
                        binding.userEmailprofile.text = user?.email
                        binding.userNICprofile.text = user?.nic
                        binding.userADDprofile.text = user?.address
                        binding.userTELEprofile.text = user?.phone
                    }else{
                        Toast.makeText(applicationContext,"User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //handle the error
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            //user is not logged in, so redirect to the login activity
            val intent = Intent(this, LoginPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}