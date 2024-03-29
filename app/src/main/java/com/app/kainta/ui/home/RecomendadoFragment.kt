package com.app.kainta.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.ServicioVistaAdapter
import com.app.kainta.databinding.FragmentRecomendadoBinding
import com.app.kainta.mvc.RecomendadoToSearchViewModel
import com.app.kainta.ui.home.search.SearchFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject


class RecomendadoFragment : Fragment() {
    private var _binding: FragmentRecomendadoBinding? = null
    private lateinit var jsonServicios: JSONArray
    private lateinit var adaptador: ServicioVistaAdapter
    private lateinit var user: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var model: RecomendadoToSearchViewModel
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawer_Layout: DrawerLayout
    private val binding get() = _binding!!

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecomendadoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        model = ViewModelProvider(requireActivity()).get(RecomendadoToSearchViewModel::class.java)

        binding.progressBar.visibility = View.VISIBLE

        setup()

        return binding.root


    }

    private fun setup() {

        binding.swiperRefresh.setOnRefreshListener {
            cargarInformacion()
            binding.swiperRefresh.isRefreshing = false
        }

        cargarInformacion()

    }

    private fun cargarInformacion() {
        jsonServicios = JSONArray()
        var jsonServicio = JSONObject()
        val jsonServicios = JSONArray()


        db.collection("servicios").orderBy("buscado" , Query.Direction.DESCENDING).limit(5)
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    for(servicio in it.result.documents){
                        jsonServicio = JSONObject(servicio.data)
                        jsonServicios.put(jsonServicio)
                    }
                    //Adaptador
                    adaptador = ServicioVistaAdapter(binding.root.context,
                        R.layout.adapter_servicio_vista,
                        jsonServicios,
                        object : ServicioVistaAdapter.OnItemClickListener {
                            override fun onItemClick(jsonServicio: JSONObject) {
                                model.setData(jsonServicio.getString("nombre").toString())

                            }
                        })

                    binding.recyclerView.adapter = adaptador
                    binding.recyclerView.layoutManager =
                        LinearLayoutManager(requireContext())
                    binding.progressBar.visibility = View.GONE
                    binding.swiperRefresh.visibility= View.VISIBLE

                }else{
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error al cargar destacados", Toast.LENGTH_SHORT).show()
                }
            }
    }

}