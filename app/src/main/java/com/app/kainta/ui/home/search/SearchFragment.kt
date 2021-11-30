package com.app.kainta.ui.home.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import com.app.kainta.R
import com.app.kainta.adaptadores.GeneralAdapter
import com.app.kainta.databinding.FragmentSearchBinding
import org.json.JSONArray
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.app.kainta.mvc.UsuarioServicioViewModel
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.app.kainta.ServicioActivity
import com.app.kainta.adaptadores.ListaServiciosAdapter
import com.app.kainta.adaptadores.ServicioVistaAdapter
import com.app.kainta.mvc.RecomendadoToSearchViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.lang.reflect.Array
import java.util.*
import kotlin.collections.ArrayList
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var jsonUsuarios: JSONArray
    private lateinit var listCorreos: ArrayList<String>
    private lateinit var adaptador: GeneralAdapter
    private lateinit var adaptadorServicios : ServicioVistaAdapter
    private lateinit var user: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var model : UsuarioServicioViewModel
    private lateinit var viewModel : RecomendadoToSearchViewModel
    private lateinit var toggle : ActionBarDrawerToggle
    private lateinit var drawer_Layout : DrawerLayout
    private val binding get() = _binding!!

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setHasOptionsMenu(true)

        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbarSearch)
        binding.toolbarSearch.title = ""

        drawer_Layout = binding.drawerLayout

        toggle = ActionBarDrawerToggle(requireActivity(), drawer_Layout, R.string.open, R.string.close)

        drawer_Layout.addDrawerListener(toggle)
        toggle.syncState()
        binding.toolbarSearch.elevation = 0F
        (activity as AppCompatActivity?)!!.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        user = Firebase.auth
        db = Firebase.firestore

        model = ViewModelProvider(requireActivity()).get(UsuarioServicioViewModel::class.java)

        viewModel = ViewModelProvider(requireActivity()).get(RecomendadoToSearchViewModel::class.java)

        viewModel.getData().observe(viewLifecycleOwner,{
            if(it != null)
                searchServicios(viewModel.getData().value.toString().lowercase())
        })

        val arrayServicios = resources.getStringArray(R.array.spinner_servicios)

        val adapterAutoComplete = ArrayAdapter(binding.root.context, android.R.layout.simple_list_item_1, arrayServicios)
        val auto : AutoCompleteTextView = binding.autoComplete

        auto.threshold = 1
        auto.setAdapter(adapterAutoComplete)
        auto.setOnItemClickListener { parent, view, position, id ->

            val servicio = adapterAutoComplete.getItem(position)?.lowercase().toString()
            if(servicio.isNotEmpty())
            searchServicios(servicio)
        }

        auto.setOnKeyListener { v, keyCode, event ->
            if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val servicio = auto.text.toString().lowercase()
                if(servicio.isNotEmpty())
                searchServicios(servicio)
            }
            false

        }

        setup()

        return binding.root


    }

    private fun searchServicios(servicio: String) {
        binding.recyclerListaServicios.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerServicios.visibility = View.GONE
        binding.btnListaServicios.visibility = View.GONE
        binding.txtResultado.visibility = View.GONE

        listCorreos = ArrayList()
        jsonUsuarios = JSONArray()
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
                        if (!documents.result.isEmpty) {
                            for (document in documents.result) {
                                listCorreos.add(document.data["correo"] as String)
                            }
                            for (correo in listCorreos) {

                                db.collection("usuario").document(correo)
                                    .get()
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            (e as FirebaseAuthException).message.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnCompleteListener { usuario ->
                                        if (usuario.isSuccessful) {
                                            jsonUsuario = JSONObject(usuario.result.data)
                                            jsonUsuario.put("servicio", servicio)
                                            jsonUsuarios.put(jsonUsuario)

                                            if (jsonUsuarios.length() == listCorreos.size) {
                                                //Adaptador
                                                adaptador = GeneralAdapter(binding.root.context,
                                                    R.layout.adapter_general,
                                                    jsonUsuarios,
                                                    object : GeneralAdapter.OnItemClickListener {
                                                        override fun onItemClick(usuario: JSONObject?) {
                                                            //Abrir activity Servicio
                                                            activity?.let {
                                                                val servicioIntent = Intent(
                                                                    it,
                                                                    ServicioActivity::class.java
                                                                ).apply {
                                                                    putExtra(
                                                                        "usuario",
                                                                        usuario.toString()
                                                                    )
                                                                }
                                                                it.startActivity(servicioIntent)
                                                            }

                                                        }
                                                    })

                                                binding.recyclerServicios.adapter = adaptador
                                                binding.recyclerServicios.layoutManager =
                                                    LinearLayoutManager(binding.root.context)

                                                binding.progressBar.visibility = View.GONE
                                                binding.recyclerServicios.visibility = View.VISIBLE
                                                binding.btnListaServicios.visibility = View.VISIBLE
                                            }
                                        }
                                    }
                            }
                            db.collection("servicios").document(servicio)
                                .get().addOnSuccessListener {
                                    db.collection("servicios").document(servicio)
                                        .set(
                                            mapOf(
                                                "buscado" to (it.data?.get("buscado") as Long + 1)
                                            ), SetOptions.merge()
                                        )
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "No se agrego el contador",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                        } else {
                            binding.txtResultado.text =
                                "No se encontraron resultados del servicio $servicio"
                            binding.progressBar.visibility = View.GONE
                            binding.txtResultado.visibility = View.VISIBLE
                            binding.recyclerServicios.visibility = View.GONE
                            binding.btnListaServicios.visibility = View.VISIBLE

                            var jsonServicio = JSONObject()
                            val jsonServicios = JSONArray()

                            db.collection("servicios").get().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    for (servicio in it.result.documents) {
                                        jsonServicio = JSONObject(servicio.data)
                                        jsonServicio.put("search", true)
                                        jsonServicios.put(jsonServicio)
                                    }
                                    //Adaptador

                                    adaptadorServicios = ServicioVistaAdapter(
                                        binding.root.context,
                                        R.layout.adapter_servicio_vista,
                                        jsonServicios,
                                        object : ServicioVistaAdapter.OnItemClickListener {
                                            override fun onItemClick(jsonServicio: JSONObject) {
                                                searchServicios(
                                                    jsonServicio.getString("nombre").lowercase()
                                                )
                                            }
                                        })

                                    binding.recyclerListaServicios.adapter = adaptadorServicios
                                    binding.recyclerListaServicios.layoutManager =
                                        LinearLayoutManager(requireContext())
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error al cargar destacados",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                db.collection("servicios").document(servicio)
                                    .get().addOnCompleteListener {
                                        if (it.result.exists())
                                            db.collection("servicios").document(servicio)
                                                .set(
                                                    mapOf(
                                                        "buscado" to (it.result.data?.get("buscado") as Long + 1)
                                                    ), SetOptions.merge()
                                                )
                                    }

                            }
                        }


                    }
                }
            }catch (e: Exception) {
            Toast.makeText(
                context,
                "No hay registros de servicios",
                Toast.LENGTH_SHORT

            ).show()
        }
    }

    private fun setup() {

        binding.btnLimpiar.visibility = View.INVISIBLE

        binding.autoComplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.autoComplete.text.toString() == "")
                    binding.btnLimpiar.visibility = View.INVISIBLE
                else
                    binding.btnLimpiar.visibility = View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnLimpiar.setOnClickListener {
            binding.autoComplete.setText("")
        }

        binding.btnListaServicios.setOnClickListener {
            binding.recyclerServicios.visibility = View.GONE
            binding.btnListaServicios.visibility = View.GONE
            binding.recyclerListaServicios.visibility = View.VISIBLE
            binding.txtResultado.visibility = View.GONE
        }


        //Adaptador

        var jsonServicio = JSONObject()
        val jsonServicios = JSONArray()

        db.collection("servicios").get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (servicio in it.result.documents) {
                    jsonServicio = JSONObject(servicio.data)
                    jsonServicio.put("search", true)
                    jsonServicios.put(jsonServicio)
                }
                //Adaptador

                adaptadorServicios = ServicioVistaAdapter(
                    binding.root.context,
                    R.layout.adapter_servicio_vista,
                    jsonServicios,
                    object : ServicioVistaAdapter.OnItemClickListener {
                        override fun onItemClick(jsonServicio: JSONObject) {
                            searchServicios(
                                jsonServicio.getString("nombre").lowercase()
                            )
                        }
                    })

                binding.recyclerListaServicios.adapter = adaptadorServicios
                binding.recyclerListaServicios.layoutManager =
                    LinearLayoutManager(requireContext())
            } else
                Toast.makeText(
                    context,
                    "Error al cargar destacados",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }




    /*
    //SE INFLAN LOS ITEMS DEL TOOLBAR
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_search, menu)
        val search = menu.findItem(R.id.nav_search_search)
        val searchView : SearchView = search?.actionView as SearchView
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

    }*/

    override fun onPrepareOptionsMenu(menu: Menu){
        super.onPrepareOptionsMenu(menu)
       /* val item = menu.findItem(R.id.nav_inicio_add)
        item.isVisible = isHidden*/
    }

}




