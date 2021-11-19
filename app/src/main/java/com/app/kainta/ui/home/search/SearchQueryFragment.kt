package com.app.kainta.ui.home.search

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.ServicioActivity
import com.app.kainta.adaptadores.GeneralAdapter
import com.app.kainta.databinding.FragmentSearchQueryBinding
import com.app.kainta.mvc.QueryServicioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject

class SearchQueryFragment : Fragment() {


    private var _binding: FragmentSearchQueryBinding? = null
    private var jsonUsuarios: JSONArray = JSONArray()
    private var listCorreos: ArrayList<String> = ArrayList()
    private lateinit var adaptador: GeneralAdapter
    private lateinit var user: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var model: QueryServicioViewModel
    private lateinit var servicio: String
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawer_Layout: DrawerLayout
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchQueryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setHasOptionsMenu(true)

        model = ViewModelProvider(requireActivity()).get(QueryServicioViewModel::class.java)
        servicio = model.mldQueryServicio.value.toString()
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbarSearch)
        binding.toolbarSearch.title = "Buscando $servicio"


        drawer_Layout = binding.drawerLayout

        toggle =
            ActionBarDrawerToggle(requireActivity(), drawer_Layout, R.string.open, R.string.close)

        drawer_Layout.addDrawerListener(toggle)
        toggle.syncState()

        (activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(true)


        user = Firebase.auth
        db = Firebase.firestore

        setup()

        return binding.root
    }

    private fun setup() {

        var jsonUsuario: JSONObject = JSONObject()

        try {
            db.collection("servicios").document(servicio)
                .collection("usuario")
                .get()
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        (e as FirebaseAuthException).message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnCompleteListener { documents ->
                    if (documents.isSuccessful) {
                        for (document in documents.result) {
                            listCorreos.add(document.data["correo"] as String)
                        }
                        for (correo in listCorreos)
                            db.collection("usuario").document(correo)
                                .get()
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        (e as FirebaseAuthException).message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnCompleteListener { usuarios ->
                                    if (usuarios.isSuccessful) {
                                        for (usuario in usuarios.result) {
                                            jsonUsuario = JSONObject(usuario.data)
                                            jsonUsuarios.put(jsonUsuario)
                                        }

                                        //Adaptador
                                        adaptador = GeneralAdapter(binding.root.context,
                                            R.layout.adapter_general,
                                            jsonUsuarios, object : GeneralAdapter.OnItemClickListener {
                                                override fun onItemClick(servicio: JSONObject?) {

                                                    //Abrir activity Servicio
                                                    activity?.let{
                                                        val servicioIntent = Intent(it, ServicioActivity::class.java).apply {
                                                            putExtra("jsonServicio", servicio.toString())
                                                        }
                                                        it.startActivity(servicioIntent)
                                                    }

                                                }
                                            })

                                        binding.recyclerSearch.adapter = adaptador
                                        binding.recyclerSearch.layoutManager =
                                            LinearLayoutManager(requireContext())

                                        binding.progressBar.visibility = View.GONE
                                        binding.layout.visibility = View.VISIBLE
                                    }
                                }
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No hay registros de servicios",
                Toast.LENGTH_SHORT

            ).show()
        }
    }


    //Funcion para remplazar un fragmento
    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_mainn, fragment)
        fragmentTransaction.commit()
    }

    //SE INFLAN LOS ITEMS DEL TOOLBAR
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_search, menu)
        val search = menu.findItem(R.id.nav_search_search)
        val searchView: SearchView = search?.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.queryHint = "Buscar"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                //  vmlGeneral.mldGeneral.postValue(query)
                //replaceFragment(GeneralFragment(),"Buscando "+query)
                model.mldQueryServicio.value = query
                replaceFragment(SearchQueryFragment())

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.nav_inicio_add)
        item.isVisible = isHidden
    }


}
