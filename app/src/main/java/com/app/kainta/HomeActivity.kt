package com.app.kainta

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.app.kainta.adaptadores.VPAdapter
import com.app.kainta.databinding.ActivityHomeBinding
import com.app.kainta.models.InformacionPersonalModel
import com.app.kainta.ui.InformacionPersonalViewModel
import com.app.kainta.ui.home.search.SearchFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var infoPersonalViewModel: InformacionPersonalViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    //private lateinit var vmlGeneral: General_ViewModel
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var drawer_Layout : DrawerLayout
    private lateinit var adapter : VPAdapter
    private val searchFragment = SearchFragment()
    lateinit var close_home: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //SE CREA LA TOOLBAR--------------------
        setSupportActionBar(binding.toolbar)


        //VIEW PAGE PARA LA TAB---------------------------

        adapter = VPAdapter(supportFragmentManager, lifecycle)
        val vp2 = binding.viewpager
        vp2.adapter = adapter
        //View page selected
        TabLayoutMediator(binding.tablayout, vp2){tab,position->
            when(position){
                0->{
                    tab.text = "Destacado"
                }
                1->{
                    tab.text = "Recomendado"
                }
                2->{
                    tab.text = "Nuevo"
                }
            }
        }.attach()


        //DRAWER NAVIGATION-------------------------------------

        //Header del drawer navigation
        val viewNavHeader = LayoutInflater.from(this).inflate(R.layout.nav_header_home, null)

        val nav_view = findViewById<NavigationView>(R.id.nav_View)
        drawer_Layout = findViewById(R.id.drawerLayout)

        toggle = ActionBarDrawerToggle(this, drawer_Layout, R.string.open, R.string.close)

        drawer_Layout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //DRAWER NAVIGATION SELECTED
        nav_view.setNavigationItemSelectedListener {

            it.isChecked = true

            when(it.itemId){

               /* R.id.nav_home -> replaceFragment(HomeFragment(), it.title.toString())
                R.id.nav_perfil -> replaceFragment(PerfilFragment(), it.title.toString())
                R.id.nav_configuracion -> replaceFragment(ConfiguracionFragment(), it.title.toString())*/

                R.id.nav_configuracion ->{
                    openActivity(ConfiguracionActivity())
                }
                R.id.nav_perfil ->{
                    openActivity(PerfilActivity())
                }

            }
            true
        }



        //NAVIGATION BOTTOM-------------------------------------------------

        binding.navbottonView.setOnItemSelectedListener {
            when(it.itemId){

                R.id.navigation_home ->{
                    binding.tablayout.visibility = View.VISIBLE
                    binding.viewpager.visibility = View.VISIBLE
                    binding.toolbar.visibility = View.VISIBLE
                }

                R.id.navigation_search -> {

                    replaceFragment(searchFragment)
                    binding.toolbar.visibility = View.GONE
                    binding.tablayout.visibility = View.GONE
                    binding.viewpager.visibility = View.GONE

                }

            }

            return@setOnItemSelectedListener true
        }



        //SETUP----------------------------------------

        infoPersonalViewModel = ViewModelProvider(this).get(InformacionPersonalViewModel::class.java)
       // vmlGeneral  = ViewModelProvider(this).get(General_ViewModel::class.java)

        val email = intent.getStringExtra("email")
        val provider = intent.getStringExtra("provider")

        setup(email ?: "",provider ?: "",  viewNavHeader)

        //Guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()


    }

    //SETUP
    private fun setup(email: String, provider: String, viewNavHeader: View) {

        val textEmail = viewNavHeader.findViewById<TextView>(R.id.navHeaderCorreo)

        val informacionPersonal = InformacionPersonalModel(
            email,
            provider
        )
        infoPersonalViewModel.mldInfoPersonal.value = informacionPersonal

        textEmail.text = email




    }

    //Funcion para remplazar un fragmento
   private fun replaceFragment(fragment: Fragment){

       val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_mainn, fragment)
        fragmentTransaction.commit()
    }

    //Funcion para abrir un activity
    private fun openActivity(activity: Activity){
        val activityIntent = Intent(this, activity::class.java)
        startActivity(activityIntent)
    }

    //Funcion para inflar los items del toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      menuInflater.inflate(R.menu.menu_fragment_inicio,menu)

        return super.onCreateOptionsMenu(menu)
    }

    //Funcion para item seleccionado ---------NO IMPORTA
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){

            return true
        }

        return super.onOptionsItemSelected(item)
    }

}