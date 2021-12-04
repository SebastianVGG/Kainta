package com.app.kainta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.app.kainta.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.elevation = 0F
        binding.btnBack.setOnClickListener {
            finish()
        }

        setup()


    }
    private fun setup(){
        val fromHome = intent.getBooleanExtra("fromHome", false)
            if(fromHome){
                findNavController(R.id.nav_host_fragment_content_perfil).navigate(R.id.action_perfilFragment_to_configServiciosFragment)
        }
    }

}