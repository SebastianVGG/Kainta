package com.app.kainta.ui.search

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import com.app.kainta.R
import com.app.kainta.adaptadores.GeneralAdapter
import com.app.kainta.databinding.FragmentSearchBinding
import org.json.JSONArray
import android.view.MenuInflater
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.app.kainta.HomeActivity


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var jsonArray : JSONArray
    private lateinit var jsonArrayCopia : JSONArray
    private lateinit var adaptador : GeneralAdapter
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var drawer_Layout : DrawerLayout
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setHasOptionsMenu(true)

        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbarSearch)
        binding.toolbarSearch.title = "Search fragment"


        drawer_Layout = binding.drawerLayout

        toggle = ActionBarDrawerToggle(requireActivity(), drawer_Layout, R.string.open, R.string.close)

        drawer_Layout.addDrawerListener(toggle)
        toggle.syncState()

        (activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(true)


        return binding.root


    }


    //SE INFLAN LOS ITEMS DEL TOOLBAR
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_search, menu)
        /*val search = menu.findItem(R.id.nav_fragment_search)
        val searchView : SearchView = search?.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.queryHint = "Buscar"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //  vmlGeneral.mldGeneral.postValue(query)
                //replaceFragment(GeneralFragment(),"Buscando "+query)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //adaptador.filter.filter(newText)
                return false
            }
        })*/

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id to handle item clicks
        val id = item!!.itemId
        //handle item clicks
        if (id == R.id.nav_search_search){
            //do your action here, im just showing toast
            Toast.makeText(activity, "Settings", Toast.LENGTH_SHORT).show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu){
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.nav_inicio_add)
        item.isVisible = isHidden
    }



}




