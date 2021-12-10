package com.app.kainta.ui.home.servicios

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.app.kainta.R
import com.app.kainta.databinding.ActivityServiciosRequeridosBinding

class ServiciosRequeridosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiciosRequeridosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiciosRequeridosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.elevation = 0F

        binding.btnBack.setOnClickListener {
            finish()
        }

        setup()


    }

    private fun setup(){
    }


}