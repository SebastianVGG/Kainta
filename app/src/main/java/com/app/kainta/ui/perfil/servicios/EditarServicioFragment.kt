package com.app.kainta.ui.perfil.servicios

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.EditarTrabajoAdapter
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
import com.google.firebase.FirebaseException


class EditarServicioFragment : Fragment() {
    private var _binding: FragmentEditarServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var eliminarTrabajo : Boolean = true
    private lateinit var jsonTrabajos : JSONArray
    private lateinit var adaptador : EditarTrabajoAdapter
    private lateinit var servicio : String
    private lateinit var dialogLoading : Dialog
    private lateinit var dialogAlert : Dialog

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

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Editando el servicio ${servicio.uppercase()}"

        jsonTrabajos = JSONArray()

        setup()


        return root
    }

    private fun setup() {

        binding.progressBar.visibility = View.VISIBLE

        inicializarLoading()


        agregarTrabajos()

        binding.btnEliminarServicio.setOnClickListener {
            dialogLoading.show()
            eliminarServicio(true)
        }


        }

    private fun agregarTrabajos() {

        var jsonTrabajo = JSONObject()
        jsonTrabajos = JSONArray()

        try{
            db.collection("usuario").document(user.currentUser?.email!!)
                .collection("servicios").document(servicio)
                .collection("trabajos")
                .get()
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
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

                                dialogLoading.show()

                                val storageRef = storage.reference

                                for (i in 0 until (item?.length()?.minus(3) ?: 0)){
                                    val spaceRef =
                                        storageRef.child("usuario/${(user.currentUser?.email ?: "")}/servicios/" +
                                                "$servicio/trabajos/" +
                                                "${item!!.getString("id")}/${item.getString("titulo")}$i")

                                    spaceRef.delete()
                                        .addOnFailureListener {
                                            dialogLoading.dismiss()
                                            showAlert("Error", (it as FirebaseAuthException).message.toString()) }
                                }

                                db.collection("usuario").document(user.currentUser?.email!!)
                                    .collection("servicios").document(servicio)
                                    .collection("trabajos").document(item!!.getString("id")).delete()
                                    .addOnSuccessListener {
                                        db.collection("usuario").document(user.currentUser?.email!!)
                                            .collection("servicios").document(servicio)
                                            .collection("trabajos").get().addOnSuccessListener {
                                                if(it.isEmpty)
                                                    eliminarServicio(false)
                                                else{
                                                    dialogLoading.dismiss()
                                                    eliminarTrabajo = true
                                                    showAlert("Correcto", "Se eliminó el trabajo")
                                                }
                                            }

                                    }
                                    .addOnFailureListener { e ->
                                        dialogLoading.dismiss()
                                        showAlert("Error", (e as FirebaseAuthException).message.toString()) }
                            }
                        })

                    binding.recyclerTrabajos.adapter = adaptador
                    binding.recyclerTrabajos.layoutManager = LinearLayoutManager(requireContext())

                    binding.progressBar.visibility = View.GONE
                    binding.layoutPrincipal.visibility = View.VISIBLE

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
            ).show()
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun eliminarServicio(fromBtn : Boolean){

        if(fromBtn){
            val storageRef = storage.reference

            db.collection("usuario").document(user.currentUser?.email!!)
                .collection("servicios").document(servicio)
                .collection("trabajos")
                .get().addOnCompleteListener { trabajos ->

                    if(trabajos.isSuccessful){

                        if(!trabajos.result.isEmpty){

                            for(trabajo in trabajos.result){

                                for (i in 0 until (trabajo?.data?.size?.minus(3) ?: 0)){

                                    storageRef.child("usuario/${(user.currentUser?.email ?: "")}/servicios/" +
                                            "$servicio/trabajos/" +
                                            "${trabajo.id}/${trabajo.get("titulo")}$i")

                                        .delete().addOnFailureListener {
                                            dialogLoading.dismiss()
                                            showAlert("Error", (it as FirebaseException).message.toString())
                                        }
                                }

                                db.collection("usuario").document(user.currentUser?.email!!)
                                    .collection("servicios").document(servicio)
                                    .collection("trabajos").document(trabajo.id).delete()

                            }

                            db.collection("usuario").document(user.currentUser?.email!!)
                                .collection("servicios").document(servicio).delete().addOnSuccessListener {

                                    db.collection("servicios").document(servicio)
                                        .collection("usuario").document(user.currentUser?.email!!).delete().addOnSuccessListener {

                                            val ref = db.collection("servicioN")
                                            ref.whereEqualTo("correo", user.currentUser?.email!!.toString()).whereEqualTo("servicio", servicio.lowercase())
                                                .get().addOnCompleteListener {

                                                    for(document in it.result)
                                                        ref.document(document.id).delete()

                                                    dialogLoading.dismiss()
                                                    showAlert("Correcto", "Se eliminó el servicio")
                                                    eliminarTrabajo = false

                                                }

                                        }
                                }

                        }else{
                            db.collection("usuario").document(user.currentUser?.email!!)
                                .collection("servicios").document(servicio).delete().addOnSuccessListener {
                                    db.collection("servicios").document(servicio)
                                        .collection("usuario").document(user.currentUser?.email!!).delete().addOnSuccessListener {
                                            val ref = db.collection("servicioN")
                                            ref.whereEqualTo("correo", user.currentUser?.email!!.toString()).whereEqualTo("servicio", servicio.lowercase())
                                                .get().addOnCompleteListener {
                                                    for(document in it.result)
                                                        ref.document(document.id).delete()
                                                    dialogLoading.dismiss()
                                                    showAlert("Correcto", "Se eliminó el servicio")
                                                    eliminarTrabajo = false
                                                }
                                        }
                                }

                        }

                    }else{
                        dialogLoading.dismiss()
                        showAlert("Error", (trabajos.exception as FirebaseException).message.toString())
                    }

                }
        }else{
            db.collection("usuario").document(user.currentUser?.email!!)
                .collection("servicios").document(servicio)
                .collection("trabajos")
                .get().addOnCompleteListener { trabajos ->

                    if(trabajos.isSuccessful){

                        if(!trabajos.result.isEmpty){

                            for(trabajo in trabajos.result){
                                db.collection("usuario").document(user.currentUser?.email!!)
                                    .collection("servicios").document(servicio)
                                    .collection("trabajos").document(trabajo.id).delete()
                            }
                            db.collection("usuario").document(user.currentUser?.email!!)
                                .collection("servicios").document(servicio).delete().addOnSuccessListener {

                                    db.collection("servicios").document(servicio)
                                        .collection("usuario").document(user.currentUser?.email!!).delete().addOnSuccessListener {

                                            val ref = db.collection("servicioN")
                                            ref.whereEqualTo("correo", user.currentUser?.email!!.toString()).whereEqualTo("servicio", servicio.lowercase())
                                                .get().addOnCompleteListener {

                                                    for(document in it.result)
                                                        ref.document(document.id).delete()
                                                    dialogLoading.dismiss()
                                                    showAlert("Correcto", "Se eliminó el servicio")
                                                    eliminarTrabajo = false

                                                }

                                        }
                                }

                        }else{
                            db.collection("usuario").document(user.currentUser?.email!!)
                                .collection("servicios").document(servicio).delete().addOnSuccessListener {
                                    db.collection("servicios").document(servicio)
                                        .collection("usuario").document(user.currentUser?.email!!).delete().addOnSuccessListener {
                                            val ref = db.collection("servicioN")
                                            ref.whereEqualTo("correo", user.currentUser?.email!!.toString()).whereEqualTo("servicio", servicio.lowercase())
                                                .get().addOnCompleteListener {
                                                    for(document in it.result)
                                                        ref.document(document.id).delete()
                                                    dialogLoading.dismiss()
                                                    showAlert("Correcto", "Se eliminó el servicio")
                                                    eliminarTrabajo = false
                                                }
                                        }
                                }

                        }

                    }else{
                        showAlert("Error", (trabajos.exception as FirebaseException).message.toString())
                    }

                }
        }
    }


    private fun showAlert(titulo: String, mensaje: String) {

        dialogAlert = Dialog(requireContext())

        dialogAlert.setContentView(R.layout.dialog_alert)

        dialogAlert.findViewById<TextView>(R.id.txtTitulo).text = titulo
        dialogAlert.findViewById<TextView>(R.id.txtMensaje).text = mensaje
        dialogAlert.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialogAlert.dismiss()
        }
        dialogAlert.findViewById<Button>(R.id.btnAceptar).setOnClickListener {
            dialogAlert.dismiss()
        }
        dialogAlert.setOnDismissListener {
            if(eliminarTrabajo)
                agregarTrabajos()
            else
                activity?.onBackPressed()
        }

        if(dialogAlert.window!=null)
            dialogAlert.window?.setBackgroundDrawable(ColorDrawable(0))

        dialogAlert.show()
    }

    private fun inicializarLoading() {
        dialogLoading = Dialog(requireContext())
        dialogLoading.setContentView(R.layout.dialog_loading)
        dialogLoading.setCancelable(false)
        if(dialogLoading.window!=null)
            dialogLoading.window?.setBackgroundDrawable(ColorDrawable(0))
    }



}