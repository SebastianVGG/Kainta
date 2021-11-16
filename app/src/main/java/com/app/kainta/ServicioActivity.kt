package com.app.kainta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.app.kainta.databinding.ActivityServicioBinding
import org.json.JSONObject
import java.lang.Exception

class ServicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServicioBinding
    private lateinit var jsonServicio : JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarServicio)
        binding.toolbarServicio.title = "Serviciooooooooooooooooooooo"


        binding.toolbarServicio.setNavigationOnClickListener{
            onBackPressed()
        }

        try{
            jsonServicio = JSONObject(intent.getStringExtra("jsonServicio"))
        }catch (e : Exception){""}



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_servicio, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var itemView = item.itemId

        when(itemView){
            R.id.nav_back -> Toast.makeText(this, "Nav back", Toast.LENGTH_SHORT).show()
        }


        return false
    }
}