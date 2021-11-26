package com.app.kainta.ui.home.servicios

import android.os.Bundle
import android.text.format.DateFormat.format
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.ServiciosRSAdapter
import com.app.kainta.databinding.FragmentServiciosSolicitadosBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.*
import android.text.format.DateUtils





class ServiciosSolicitadosFragment : Fragment() {
    private var _binding: FragmentServiciosSolicitadosBinding? = null
    private val binding get() = _binding!!
    private lateinit var adaptador : ServiciosRSAdapter
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentServiciosSolicitadosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        setup()


        return root
    }

    private fun setup(){

        val jsons = JSONArray()
        var jsonData : JSONObject = JSONObject()

        //Se obtienen las direcciones del usuario
        val docRef = db.collection("usuario").document(user.currentUser?.email!!).collection("servicios_solicitados")
        docRef.get()
            .addOnCompleteListener { documents ->
                if (!documents.result.isEmpty){
                    for (document in documents.result) {
                        jsonData = JSONObject(document.data)
                        val timestamp : Timestamp =
                            document.data["fecha"] as Timestamp

                        jsonData.put("fecha", timestamp.toDate())
                        jsons.put(jsonData)
                    }

                    //Adaptador
                    adaptador = ServiciosRSAdapter(
                        binding.root.context,
                        R.layout.adapter_servicios_r_s,
                        jsons, object : ServiciosRSAdapter.OnItemClickListener {
                            override fun onItemClick(item: JSONObject?) {



                            }

                        })

                    binding.recyclerSolicitados.adapter = adaptador
                    binding.recyclerSolicitados.layoutManager = LinearLayoutManager(requireContext())


                }else{
                    showAlert("Error", "No cuenta con servicios solicitados")
                }

            }
            .addOnFailureListener {
                showAlert("Error", (it as FirebaseAuthException).message.toString())
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