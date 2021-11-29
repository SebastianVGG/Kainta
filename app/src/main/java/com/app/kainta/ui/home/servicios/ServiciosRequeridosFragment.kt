package com.app.kainta.ui.home.servicios

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.adaptadores.ServiciosRSAdapter
import com.app.kainta.databinding.FragmentConfigDireccionesBinding
import com.app.kainta.databinding.FragmentServiciosRequeridosBinding
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
import java.util.*

class ServiciosRequeridosFragment : Fragment() {

        private var _binding: FragmentServiciosRequeridosBinding? = null
        private val binding get() = _binding!!
    private lateinit var adaptador : ServiciosRSAdapter
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentServiciosRequeridosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        setup()


        return root
    }

    private fun setup(){

        val jsons = JSONArray()
        var jsonData : JSONObject = JSONObject()

        val docRef = db.collection("usuario").document(user.currentUser?.email!!).collection("servicios_requeridos")
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
                                val bundle = Bundle()
                                item?.put("requeridos", true)
                                item?.get("fecha").let {
                                    try {
                                        bundle.putLong("fecha", (it as Date).time)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                bundle.putString("jsonServicio", item.toString())
                                findNavController().navigate(R.id.action_serviciosRequeridosFragment_to_mostrarServicioRSFragment, bundle)
                            }
                        })

                    binding.recyclerRequeridos.adapter = adaptador
                    binding.recyclerRequeridos.layoutManager = LinearLayoutManager(requireContext())


                }else{
                    showAlert("Error", "No cuenta con servicios requeridos")
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
        builder.setPositiveButton("Aceptar", null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

}