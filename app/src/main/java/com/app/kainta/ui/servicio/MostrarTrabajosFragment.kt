package com.app.kainta.ui.servicio

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.EditarTrabajoAdapter
import com.app.kainta.adaptadores.MostrarTrabajosAdapter
import com.app.kainta.databinding.FragmentEditarServicioBinding
import com.app.kainta.databinding.FragmentMostrarTrabajosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MostrarTrabajosFragment : Fragment() {
    private var _binding: FragmentMostrarTrabajosBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var jsonUsuario : JSONObject
    private lateinit var jsonTrabajos : JSONArray
    private lateinit var adaptador : MostrarTrabajosAdapter
    private lateinit var servicio : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMostrarTrabajosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Trabajos realizados"


        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        servicio = arguments?.getString("servicioNombre").toString().lowercase()
        jsonUsuario = JSONObject(arguments?.getString("usuario") as String)
        jsonTrabajos = JSONArray()

        setup()


        return root
    }

    @SuppressLint("SetTextI18n")
    private fun setup() {

        val txtServicio = servicio.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }

        binding.txtTitulo.text = "Servicio de $txtServicio"

        if(jsonUsuario.getString("email") == user.currentUser?.email)
            binding.btnSolicitarServicio.visibility = View.GONE

        binding.btnSolicitarServicio.setOnClickListener {

            val bundle = Bundle()
            jsonUsuario.put("servicio", servicio)
            bundle.putString("jsonUsuario", jsonUsuario.toString())

            findNavController().navigate(R.id.action_mostrarTrabajosFragment_to_solicitarServicioFragment, bundle)

        }

        var jsonTrabajo = JSONObject()

        try{
            db.collection("usuario").document(jsonUsuario.getString("email"))
                .collection("servicios").document(servicio)
                .collection("trabajos")
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
                        for ( document in documents.result){
                            jsonTrabajo = JSONObject(document.data)
                            jsonTrabajos.put(jsonTrabajo)
                        }

                    //Adaptador
                    adaptador = MostrarTrabajosAdapter(
                        binding.root.context,
                        R.layout.adapter_mostrar_trabajos,
                        jsonTrabajos, object : MostrarTrabajosAdapter.OnItemClickListener {
                            override fun onItemClick(item: JSONObject) {
                                val bundle = Bundle()
                                jsonUsuario.put("servicio", servicio)
                                bundle.putString("jsonUsuario", jsonUsuario.toString())
                                bundle.putString("jsonTrabajo", item.toString())
                                findNavController().navigate(
                                    R.id.action_mostrarTrabajosFragment_to_mostrarTrabajoFragment,
                                    bundle
                                )
                             }
                        })

                    binding.recyclerTrabajos.adapter = adaptador
                    binding.recyclerTrabajos.layoutManager = LinearLayoutManager(requireContext())
                    binding.progressBar.visibility = View.GONE
                    binding.layoutPrincipal.visibility = View.VISIBLE
                }

        }catch(e:Exception){
            Toast.makeText(
                context,
                "No hay registros de servicios",
                Toast.LENGTH_SHORT
            ).show()}
    }






}