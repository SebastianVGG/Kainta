package com.app.kainta.ui.servicio

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.kainta.R
import com.app.kainta.adaptadores.PerfilServiciosAdapter
import com.app.kainta.databinding.FragmentServicioBinding
import com.app.kainta.databinding.FragmentSolicitarServicioBinding
import com.app.kainta.mvc.UsuarioServicioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject
import java.lang.Exception


class SolicitarServicioFragment : Fragment() {
    private var _binding: FragmentSolicitarServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePicker: ImageView
    private lateinit var user: FirebaseAuth
    private lateinit var nombre: String
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var jsonUsuario: JSONObject
    private lateinit var model: UsuarioServicioViewModel
    private lateinit var serviciosArray: ArrayList<String>
    private lateinit var adaptador: PerfilServiciosAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSolicitarServicioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        jsonUsuario = JSONObject(arguments?.getString("jsonUsuario").toString())

        setup()

        return root
    }

    private fun setup() {

        binding.txtServicio.text = "Solicitar el servicio de: ${jsonUsuario.getString("servicio").uppercase()}"



        binding.btnSoliciarServicio.setOnClickListener {

            enviarNotificacion()



        }

    }

    private fun enviarNotificacion() {
        val token : String = jsonUsuario.getString("token")
        val keyMessaging = getString(R.string.default_key_messaging)

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        val jsonNotificacion = JSONObject()

        try {

            json.put("to" , token)
            jsonNotificacion.put("titulo", "Se solicita su servicio de: ${jsonUsuario.getString("servicio").uppercase()}")
            jsonNotificacion.put("detalle", "Lo solicita: ${nombre.uppercase()}")

            json.put("data", jsonNotificacion)

            val url = "https://fcm.googleapis.com/fcm/send"

            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url,
                json,
                Response.Listener { response: JSONObject? ->
                    Log.d(
                        "MUR",
                        "onResponse: "
                    )
                },
                Response.ErrorListener { error: VolleyError ->
                    Log.d(
                        "MUR",
                        "onError: " + error.networkResponse
                    )
                }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=$keyMessaging"
                    return header
                }
            }


            myrequest.add(request)

        } catch (e: Exception) {
        }
    }

}