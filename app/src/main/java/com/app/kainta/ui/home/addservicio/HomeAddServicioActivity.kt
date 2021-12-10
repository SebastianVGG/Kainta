package com.app.kainta.ui.home.addservicio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.app.kainta.R
import com.app.kainta.databinding.ActivityHomeAddServicioBinding
import com.app.kainta.databinding.ActivityPerfilBinding
import com.app.kainta.mvc.FromHomeViewModel
import com.app.kainta.mvc.UsuarioServicioViewModel

class HomeAddServicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeAddServicioBinding
    private lateinit var model : FromHomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAddServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.elevation = 0F

        binding.btnBack.setOnClickListener {
            finish()
        }

        val fromHome = intent.getBooleanExtra("fromHome", false)
        model = ViewModelProvider(this).get(FromHomeViewModel::class.java)
        model.mldFromHome.value = fromHome

    }
}