package com.app.kainta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.app.kainta.databinding.ActivityServicioBinding
import com.app.kainta.mvc.UsuarioServicioViewModel
import org.json.JSONObject
import java.lang.Exception

class ServicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServicioBinding
    private lateinit var jsonServicio : JSONObject
    private lateinit var model : UsuarioServicioViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.elevation = 0F

        binding.btnBack.setOnClickListener {
            finish()
        }

        model = ViewModelProvider(this).get(UsuarioServicioViewModel::class.java)
        model.mldUsuarioServicio.value = intent.getStringExtra("usuario")

    }

}