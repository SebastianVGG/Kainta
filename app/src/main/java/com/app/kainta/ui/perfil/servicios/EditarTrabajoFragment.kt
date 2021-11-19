package com.app.kainta.ui.perfil.servicios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.ServiciosImagesAdapter
import com.app.kainta.adaptadores.ServiciosImagesURLAdapter
import com.app.kainta.databinding.FragmentEditarServicioBinding
import com.app.kainta.databinding.FragmentEditarTrabajoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject


class EditarTrabajoFragment : Fragment() {
    private var _binding: FragmentEditarTrabajoBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var jsonTrabajo : JSONObject
    private var listURL : ArrayList<String> = ArrayList()
    private lateinit var servicio : String
    private lateinit var adaptador : ServiciosImagesURLAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditarTrabajoBinding.inflate(inflater, container, false)
        val root: View = binding.root


        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        jsonTrabajo = JSONObject(arguments?.getString("jsonTrabajo").toString())
        servicio = arguments?.getString("servicio").toString()


        setup()


        return root
    }

    private fun setup() {

        for (i in 0 until jsonTrabajo.length()-2)
            listURL.add(jsonTrabajo.getString("url$i"))

            //Adaptador
            adaptador = ServiciosImagesURLAdapter(
                binding.root.context,
                R.layout.adapter_servicios_images,
                listURL, object : ServiciosImagesURLAdapter.OnItemClickListener {
                    override fun onItemClick(url: String) {
                        Toast.makeText(context, "Hiciste click", Toast.LENGTH_SHORT).show()
                    }
                })

        binding.recyclerImages.adapter = adaptador
        binding.recyclerImages.layoutManager = GridLayoutManager(context, 3)

            binding.editTitulo.setText(jsonTrabajo.getString("titulo").toString())
            binding.editDescripcion.setText(jsonTrabajo.getString("descripcion").toString())

    }

    private fun showAlert(titulo : String,mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar") { _,_ ->

        }
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }


}