package com.app.kainta.ui.perfil.servicios

import android.annotation.SuppressLint
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
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.adaptadores.EditarTrabajoAdapter
import com.app.kainta.adaptadores.PerfilServiciosAdapter
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
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot

import com.google.firebase.firestore.QuerySnapshot

import com.google.android.gms.tasks.OnCompleteListener







class EditarServicioFragment : Fragment() {
    private var _binding: FragmentEditarServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var jsonTrabajos : JSONArray
    private lateinit var adaptador : EditarTrabajoAdapter
    private lateinit var servicio : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditarServicioBinding.inflate(inflater, container, false)
        val root: View = binding.root


        user = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        servicio = arguments?.getString("servicioNombre").toString().lowercase()

        jsonTrabajos = JSONArray()

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
                    adaptador = EditarTrabajoAdapter(
                        binding.root.context,
                        R.layout.adapter_editar_trabajo,
                        jsonTrabajos, object : EditarTrabajoAdapter.OnItemClickListener {
                            override fun onItemClick(item: JSONObject?, editar: Boolean) {

                                   /* val bundle = Bundle()
                                    bundle.putString("jsonTrabajo", item.toString())
                                    bundle.putString("servicio", servicio)
                                    findNavController().navigate(R.id.action_editarServicioFragment_to_editarTrabajoFragment, bundle)
                                    */

                                    val storageRef = storage.reference

                                    for (i in 0 until (item?.length()?.minus(2) ?: 0)){
                                        val spaceRef =
                                            storageRef.child("usuario/${(user.currentUser?.email ?: "")}/servicios/" +
                                                    "$servicio/trabajos/" +
                                                    "${item!!.getString("titulo")}/${item.getString("titulo")}$i")

                                            spaceRef.delete()
                                        .addOnFailureListener {
                                            showAlert("Error", (it as FirebaseAuthException).message.toString()) }
                                    }

                                    db.collection("usuario").document(user.currentUser?.email!!)
                                        .collection("servicios").document(servicio)
                                        .collection("trabajos").document(item!!.getString("titulo")).delete()
                                        .addOnSuccessListener {
                                            db.collection("usuario").document(user.currentUser?.email!!)
                                                .collection("servicios").document(servicio)
                                                .collection("trabajos").get().addOnSuccessListener {
                                                    if(it.isEmpty)
                                                        eliminarServicio()
                                                }
                                            showAlert("Correcto", "Se eliminó el trabajo")
                                        }
                                        .addOnFailureListener { e -> showAlert("Error", (e as FirebaseAuthException).message.toString()) }
                                }
                        })

                    binding.recyclerTrabajos.adapter = adaptador
                    binding.recyclerTrabajos.layoutManager = LinearLayoutManager(requireContext())

                    binding.btnAgregarTrabajo.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("servicio",servicio.lowercase())
                        bundle.putBoolean("nuevo",false)
                        findNavController().navigate(R.id.action_editarServicioFragment_to_addTrabajoFragment, bundle)

                    }
                }

        }catch(e:Exception){
            Toast.makeText(
            context,
            "No hay registros de servicios",
            Toast.LENGTH_SHORT
        ).show()}

        binding.btnEliminarServicio.setOnClickListener {

            db.collection("usuario").document(user.currentUser?.email!!)
                .collection("servicios").document(servicio)
                .delete()
                .addOnSuccessListener {db.collection("servicios").document(servicio)
                    .collection("usuario").document(user.currentUser?.email!!)
                    .delete()
                    .addOnSuccessListener {
                        showAlert("Correcto", "Se eliminó el servicio")
                        requireActivity().onBackPressedDispatcher
                    }
                    }.addOnFailureListener { e -> showAlert("Error", (e as FirebaseAuthException).message.toString()) }
                }


        }


    private fun eliminarServicio(){
        db.collection("usuario").document(user.currentUser?.email!!)
            .collection("servicios").document(servicio).delete().addOnSuccessListener {
                db.collection("servicios").document(servicio)
                    .collection("usuario").document(user.currentUser?.email!!).delete().addOnSuccessListener {
                       val ref = db.collection("servicioN")
                        ref.whereEqualTo("correo", user.currentUser?.email!!.toString())
                        .get().addOnCompleteListener {
                            for(document in it.result)
                                ref.document(document.id).delete()
                        }
                    }
            }

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