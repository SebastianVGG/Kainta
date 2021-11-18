package com.app.kainta.ui.perfil.servicios

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.EditarTrabajoAdapter
import com.app.kainta.adaptadores.ServiciosImagesAdapter
import com.app.kainta.databinding.FragmentEditarServicioBinding
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


class EditarTrabajoFragment : Fragment() {
    private var _binding: FragmentEditarServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var jsonTrabajo : JSONObject
    private lateinit var adaptador : ServiciosImagesAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditarServicioBinding.inflate(inflater, container, false)
        val root: View = binding.root


        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        if(arguments?.isEmpty == false)
            jsonTrabajo = JSONObject(arguments?.getString("jsonTrabajo").toString())

        setup()


        return root
    }

    private fun setup() {

        var jsonTrabajo = JSONObject()

        try{
            db.collection("usuario").document(user.currentUser?.email!!)
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
                    adaptador = ServiciosImagesAdapter(
                        binding.root.context,
                        R.layout.adapter_editar_trabajo,
                        jsonTrabajos, object : ServiciosImagesAdapter.OnItemClickListener {
                            override fun onItemClick(item: JSONObject?, editar: Boolean) {

                                if(editar){
                                    val bundle = Bundle()
                                    bundle.putString("jsonTrabajo", item.toString())
                                    findNavController().navigate(R.id.action_editarServicioFragment_to_editarTrabajoFragment, bundle)
                                }else{
                                    db.collection("usuario").document(user.currentUser?.email!!)
                                        .collection("servicios").document(servicio)
                                        .collection("trabajos").document(item!!.getString("titulo")).delete()
                                        .addOnSuccessListener {
                                            showAlert("Correcto", "Se eliminó el trabajo")
                                        }
                                        .addOnFailureListener { e -> showAlert("Error", (e as FirebaseAuthException).message.toString()) }
                                }
                            }
                        })

                    binding.recyclerTrabajos.adapter = adaptador
                    binding.recyclerTrabajos.layoutManager = LinearLayoutManager(requireContext())
                }

        }catch(e:Exception){
            Toast.makeText(
                context,
                "No hay registros de servicios",
                Toast.LENGTH_SHORT
            ).show()}

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