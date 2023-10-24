package com.cleancirclecompany.wasteworker.activities


import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.cleancirclecompany.wasteworker.R
import com.cleancirclecompany.wasteworker.databinding.ActivityWorkerHomeBinding
import com.google.firebase.auth.FirebaseAuth

class WorkerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.logoutWorker.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Log Out")
            builder.setMessage("Are you sure you want to log out?")

            builder.setPositiveButton("Yes") { dialog, which ->
                firebaseAuth.signOut()
                Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginPageActivity::class.java)
                startActivity(intent)
            }

            builder.setNegativeButton("No") { dialog, which ->
                // Do nothing
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        binding.profileButtonWorkerHome.setOnClickListener {
            val intent = Intent(this, WorkerViewProfileActivity::class.java)
            Toast.makeText(this, "Welcome to the profile page!", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

        val wastageTypeSpinner = binding.spinner
        val wastageTypeAdapter = ArrayAdapter.createFromResource(
            this, R.array.waste_type, android.R.layout.simple_spinner_item
        )

        wastageTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        wastageTypeSpinner.adapter = wastageTypeAdapter
        wastageTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val wasteType = parent?.getItemAtPosition(position)
                if (wasteType != null) {
                    Toast.makeText(this@WorkerHomeActivity, "Waste Type: $wasteType", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.btnShowMap.setOnClickListener {
            val selectedWastageType = wastageTypeSpinner.selectedItem.toString()
            val intent = Intent(this, RetrieveMapsActivity::class.java)
            intent.putExtra("wasteType", selectedWastageType)
            startActivity(intent)
        }
    }
}