package com.cleancirclecompany.wasteworker.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.cleancirclecompany.wasteworker.R
import com.cleancirclecompany.wasteworker.databinding.ActivityWorkerHomeBinding

class WorkerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkerHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                val wastageType = parent?.getItemAtPosition(position)
                Toast.makeText(this@WorkerHomeActivity,"Wastage Type : $wastageType",Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.btnShowMap.setOnClickListener {
            val selectedWastageType = wastageTypeSpinner.selectedItem.toString()
            val intent = Intent(this, RetrieveMapsActivity::class.java)
            intent.putExtra("wastageType", selectedWastageType)
            startActivity(intent)
        }
    }
}