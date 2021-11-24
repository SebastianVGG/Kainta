package com.app.kainta.ui.home.servicios

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.kainta.databinding.ActivityServiciosRequeridosBinding

class ServiciosRequeridosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiciosRequeridosBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiciosRequeridosBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}