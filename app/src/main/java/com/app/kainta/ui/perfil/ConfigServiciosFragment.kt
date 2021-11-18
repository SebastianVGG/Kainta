package com.app.kainta.ui.perfil

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentConfigServiciosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject


class ConfigServiciosFragment : Fragment() {
    private var _binding: FragmentConfigServiciosBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var serviciosArray : ArrayList<String>
    private lateinit var adaptador : PerfilServiciosAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfigServiciosBinding.inflate(inflater, container, false)
        val root: View = binding.root


        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        serviciosArray = ArrayList()


        setup()


        return root
    }

    private fun setup() {

        try{
            db.collection("usuario").document(user.currentUser?.email!!)
                .collection("servicios")
                .get()
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        (e as FirebaseAuthException).message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnCompleteListener { documents ->
                    if(documents.isSuccessful)
                    if(!documents.result.isEmpty){
                        for ( document in documents.result){
                            serviciosArray.add(document.data["nombre"] as String)
                            println(document.data["nombre"] as String)
                        }

                        //Adaptador
                        adaptador = PerfilServiciosAdapter(
                            binding.root.context,
                            R.layout.adapter_perfil_servicios,
                            serviciosArray, object : PerfilServiciosAdapter.OnItemClickListener {
                                @SuppressLint("ResourceType")
                                override fun onItemClick(servicioNombre: String) {

                                        val bundle = Bundle()
                                        bundle.putString("servicioNombre", servicioNombre)
                                        findNavController().navigate(R.id.action_configServiciosFragment_to_editarServicioFragment, bundle)
                                }
                            })

                        binding.recyclerServicios.adapter = adaptador
                        binding.recyclerServicios.layoutManager = LinearLayoutManager(requireContext())

                        binding.progressBar.visibility = View.GONE
                        binding.layout.visibility = View.VISIBLE
                        binding.btnAddServicio.visibility =
                            View.VISIBLE

                    }else{
                        binding.progressBar.visibility = View.GONE
                        binding.layout.visibility = View.VISIBLE
                        binding.btnAddServicio.visibility =
                            View.VISIBLE
                    }
                }
        }catch(e:Exception){Toast.makeText(
            context,
            "No hay registros de servicios",
            Toast.LENGTH_SHORT

        ).show()}



        binding.btnAddServicio.setOnClickListener {
            findNavController().navigate(R.id.action_configServiciosFragment_to_addServicioFragment)
        }

    }


}