package com.app.kainta.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.ServicioActivity
import com.app.kainta.adaptadores.DestacadoFragmentAdapter
import com.app.kainta.adaptadores.GeneralAdapter
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentDestacadoBinding
import com.app.kainta.mvc.UsuarioServicioViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList


class DestacadoFragment : Fragment() {

    private var _binding: FragmentDestacadoBinding? = null
    private lateinit var jsonServicios: JSONArray
    private lateinit var jsonArrayCopia : JSONArray
    private lateinit var adaptador : PerfilServiciosAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var model : UsuarioServicioViewModel
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDestacadoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        db = Firebase.firestore

        model = ViewModelProvider(requireActivity()).get(UsuarioServicioViewModel::class.java)

        setup()

        return binding.root
    }

    private fun setup() {

        jsonServicios = JSONArray()
        var jsonServicio = JSONObject()
        var listServicios = ArrayList<String>()


        db.collection("servicios").orderBy("busqueda", Query.Direction.DESCENDING).limit(5)
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    for(servicio in it.result.documents)
                    listServicios.add(servicio.data?.get("nombre") as String)
                    //Adaptador
                    adaptador = PerfilServiciosAdapter(binding.root.context,
                        R.layout.adapter_general,
                        listServicios,
                        object : PerfilServiciosAdapter.OnItemClickListener {
                            override fun onItemClick(item: String) {
                                model.mldUsuarioServicio.postValue(item)
                                //Abrir activity Servicio
                                activity?.let { act ->
                                    val servicioIntent = Intent(
                                        act,
                                        ServicioActivity::class.java
                                    ).apply {
                                        putExtra("usuario", item.toString())
                                    }
                                    act.startActivity(servicioIntent)
                                }
                            }
                        })

                    binding.recyclerView.adapter = adaptador
                    binding.recyclerView.layoutManager =
                        LinearLayoutManager(requireContext())



                }else{
                    Toast.makeText(context, "Error al cargar destacados", Toast.LENGTH_SHORT).show()
                }
            }

    }
}