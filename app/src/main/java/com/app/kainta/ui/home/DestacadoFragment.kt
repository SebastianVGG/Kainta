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
import com.app.kainta.adaptadores.HomeAdapter
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentDestacadoBinding
import com.app.kainta.mvc.UsuarioServicioViewModel
import com.google.firebase.auth.FirebaseAuthException
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
    private lateinit var listCorreos: ArrayList<String>
    private lateinit var adaptador : GeneralAdapter
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

        binding.swiperRefresh.setOnRefreshListener {
            cargarInformacion()
            binding.swiperRefresh.isRefreshing = false
        }

        cargarInformacion()


    }

    private fun cargarInformacion() {
        jsonServicios = JSONArray()
        listCorreos = ArrayList()
        var jsonServicio = JSONObject()
        var jsonUsuario = JSONObject()
        val jsonUsuarios = JSONArray()

        db.collection("valorados").orderBy("valoracion" , Query.Direction.DESCENDING).limit(5)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if(!it.result.isEmpty){
                        for (document in it.result.documents)
                            listCorreos.add(document.data?.get("correo") as String)

                        for (i in 0 until listCorreos.size) {

                            db.collection("usuario").document(listCorreos[i])
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
                                        jsonUsuarios.put(jsonUsuario)

                                        if (jsonUsuarios.length() == listCorreos.size) {
                                            //Adaptador
                                            adaptador = GeneralAdapter(binding.root.context,
                                                R.layout.adapter_general,
                                                jsonUsuarios,
                                                object : GeneralAdapter.OnItemClickListener {
                                                    override fun onItemClick(usuario: JSONObject?) {
                                                        //Abrir activity Servicio
                                                        activity?.let { frActivity ->
                                                            val servicioIntent = Intent(
                                                                frActivity,
                                                                ServicioActivity::class.java
                                                            ).apply {
                                                                putExtra("usuario", usuario.toString())
                                                            }
                                                            frActivity.startActivity(servicioIntent)
                                                        }

                                                    }
                                                })

                                            binding.recyclerView.adapter = adaptador
                                            binding.recyclerView.layoutManager =
                                                LinearLayoutManager(requireContext())
                                        }
                                    }else{
                                        Toast.makeText(context, "Error al cargar destacados", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                        }
                    }

                }else{
                    Toast.makeText(context, "Error al cargar destacados", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

}