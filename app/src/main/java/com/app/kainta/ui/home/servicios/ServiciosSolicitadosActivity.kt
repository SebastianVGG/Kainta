package com.app.kainta.ui.home.servicios

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.kainta.databinding.ActivityServiciosSolicitadosBinding

class ServiciosSolicitadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiciosSolicitadosBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiciosSolicitadosBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}