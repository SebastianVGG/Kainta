package com.app.kainta.ui.home.servicios

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.kainta.R
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.databinding.FragmentEditarDireccionBinding
import com.app.kainta.databinding.FragmentMostrarServicioRSBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.util.*

class MostrarServicioRSFragment : Fragment() {
    private var _binding: FragmentMostrarServicioRSBinding? = null
    private val binding get() = _binding!!
    private var jsonServicio : JSONObject = JSONObject()
    private var jsonUsuario : JSONObject = JSONObject()
    private var fecha : Long = 0
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMostrarServicioRSBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        jsonServicio = JSONObject(arguments?.getString("jsonServicio").toString())
        fecha = arguments?.getLong("fecha")!!

        setup()

        return root
    }

    private fun setup(){

        if(jsonServicio.getString("estado") != "pendiente"){

            binding.btnCancelar.visibility = View.GONE
            binding.btnAceptar.visibility = View.GONE

        }

        //Requeridos son los servicios que te han solicitados

        //Solicitados son los servicios que tú has solicitado

        if(jsonServicio.has("requeridos"))
            binding.btnCancelar.text = "Rechazar"
        else
            binding.btnAceptar.visibility = View.GONE

        llenarInformacion()

        binding.btnAceptar.setOnClickListener {

            db.collection("usuario").document(jsonServicio.getString("correo"))
                .get().addOnCompleteListener {
                    if(it.isSuccessful){
                        jsonUsuario = JSONObject(it.result.data)
                        cambiarEstado(0)
                    }else {
                        showAlert("Error", (it.exception as FirebaseException).message.toString())
                    }
                }

        }

        binding.btnCancelar.setOnClickListener {

            if(binding.btnCancelar.text == "Rechazar"){

                db.collection("usuario").document(jsonServicio.getString("correo"))
                    .get().addOnCompleteListener {
                        if(it.isSuccessful){
                            jsonUsuario = JSONObject(it.result.data)
                            cambiarEstado(1)
                        }else {
                            showAlert("Error", (it.exception as FirebaseException).message.toString())
                        }
                    }

            }else{
                db.collection("usuario").document(jsonServicio.getString("correo"))
                    .get().addOnCompleteListener {
                        if(it.isSuccessful){
                            jsonUsuario = JSONObject(it.result.data)
                            cambiarEstado(2)
                        }else {
                            showAlert("Error", (it.exception as FirebaseException).message.toString())
                        }
                    }
            }

        }

    }

    private fun cambiarEstado(aux : Int) {

        when(aux){

            0 -> {       db.collection("usuario").document(user.currentUser!!.email!!)
                .collection("servicios_requeridos").document(jsonServicio.getString("id"))
                .set(mapOf(
                    "estado" to "aceptado"
                ), SetOptions.merge()).addOnCompleteListener {
                    if(it.isSuccessful){
                        db.collection("usuario").document(jsonUsuario.getString("email"))
                            .collection("servicios_solicitados").document(jsonServicio.getString("id_solicitados"))
                            .set(mapOf(
                                "estado" to "aceptado"
                            ), SetOptions.merge()).addOnCompleteListener {
                                if(it.isSuccessful){
                                    enviarNotificacionAceptar()

                                    showAlert("Se aceptó el servicio", """
                                        Se aceptó el servicio de ${jsonServicio.getString("servicio").uppercase()}.
                                        Acude en tiempo y en forma a la dirección y fecha asignada.
                                    """.trimMargin())

                                }else{
                                    showAlert("Error", (it.exception as FirebaseException).message.toString())
                                }
                            }
                    }else{
                        showAlert("Error", (it.exception as FirebaseException).message.toString())
                    }
                }}

            1 -> {db.collection("usuario").document(user.currentUser!!.email!!)
                .collection("servicios_requeridos").document(jsonServicio.getString("id"))
                .set(mapOf(
                    "estado" to "rechazado"
                ), SetOptions.merge()).addOnCompleteListener {
                    if(it.isSuccessful){
                        db.collection("usuario").document(jsonUsuario.getString("email"))
                            .collection("servicios_solicitados").document(jsonServicio.getString("id_solicitados"))
                            .set(mapOf(
                                "estado" to "rechazado"
                            ), SetOptions.merge()).addOnCompleteListener {
                                if(it.isSuccessful){

                                    enviarNotificacionRechazar(true)
                                    showAlert("Se rechazó el servicio", """
                                        Se rechazó el servicio de ${jsonServicio.getString("servicio").uppercase()}.
                                        Se notificará a la otra persona.
                                    """.trimMargin())

                                }else{
                                    showAlert("Error", (it.exception as FirebaseException).message.toString())
                                }
                            }
                    }else{
                        showAlert("Error", (it.exception as FirebaseException).message.toString())
                    }
                }}

            2 -> {db.collection("usuario").document(user.currentUser!!.email!!)
                .collection("servicios_solicitados").document(jsonServicio.getString("id"))
                .set(mapOf(
                    "estado" to "cancelado"
                ), SetOptions.merge()).addOnCompleteListener {
                    if(it.isSuccessful){
                        db.collection("usuario").document(jsonUsuario.getString("email"))
                            .collection("servicios_requeridos").document(jsonServicio.getString("id_requeridos"))
                            .set(mapOf(
                                "estado" to "cancelado"
                            ), SetOptions.merge()).addOnCompleteListener {
                                if(it.isSuccessful){

                                    enviarNotificacionRechazar(false)
                                    showAlert("Se canceló el servicio", """
                                        Se canceló el servicio de ${jsonServicio.getString("servicio").uppercase()}.
                                        Se notificará a la otra persona.
                                    """.trimMargin())

                                }else{
                                    showAlert("Error", (it.exception as FirebaseException).message.toString())
                                }
                            }
                    }else{
                        showAlert("Error", (it.exception as FirebaseException).message.toString())
                    }
                }}

        }



    }

    private fun llenarInformacion() {

        binding.txtServicio.text = jsonServicio.getString("servicio")
        binding.txtNombre.text = jsonServicio.getString("nombre")
        binding.txtEstado.text = jsonServicio.getString("estado")
        binding.txtTitulo.text = jsonServicio.getString("titulo")
        binding.txtDescripcion.text = jsonServicio.getString("descripcion")
        fecha.let {
            try {
                val dateFromLong = Date(it)
                val date  = DateFormat.format("yyyy-MM-dd", dateFromLong)
                binding.txtFecha.text = "Fecha: $date"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.txtHora.text = "Hora: ${jsonServicio.getString("hora")} : ${jsonServicio.getString("minutos")}"

        val jsonDireccion = jsonServicio.getJSONObject("direccion")

        binding.txtDireccionNombre.text = jsonDireccion.getString("nombre")
        binding.txtDireccionDireccion.text = jsonDireccion.getString("direccion")
        binding.txtDireccionColonia.text = jsonDireccion.getString("colonia")
        binding.txtDireccionCP.text = jsonDireccion.getString("cp")
        binding.txtDireccionCiudad.text = jsonDireccion.getString("ciudad")
        binding.txtDireccionTelefono.text = jsonDireccion.getString("telefono")

    }

    private fun showAlert(titulo : String,mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar") { _,_ ->
            activity?.onBackPressed()
        }
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }


    private fun enviarNotificacionRechazar(aux : Boolean) {
        val token : String = jsonUsuario.getString("token")
        val keyMessaging = getString(R.string.default_key_messaging)

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        val jsonNotificacion = JSONObject()



        json.put("to" , token)

        if(aux)
            jsonNotificacion.put("titulo", "Se rechazó el servicio de: ${jsonServicio.getString("servicio").uppercase()}")
        else
            jsonNotificacion.put("titulo", "Se canceló el servicio de: ${jsonServicio.getString("servicio").uppercase()}")

        db.collection("usuario").document(user.currentUser!!.email!!)
                .get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        try {
                            val nombre = it.result.data?.get("nombre").toString()

                            if(aux)
                                jsonNotificacion.put("detalle", "${nombre.uppercase()} rechazó el servicio.")
                            else
                                jsonNotificacion.put("detalle", "${nombre.uppercase()} canceló el servicio.")

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
                    } else {
                        showAlert("Error", (it.exception as FirebaseException).message.toString())
                    }
                }
    }

    private fun enviarNotificacionAceptar() {
        val token : String = jsonUsuario.getString("token")
        val keyMessaging = getString(R.string.default_key_messaging)

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        val jsonNotificacion = JSONObject()



        json.put("to" , token)
        jsonNotificacion.put("titulo", "Se aceptó el servicio")

        db.collection("usuario").document(user.currentUser!!.email!!)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    try {
                        val nombre = it.result.data?.get("nombre").toString()
                        jsonNotificacion.put("detalle", "La persona aceptó realizar el servicio: ${jsonServicio.getString("servicio").uppercase()}")

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
                } else {
                    showAlert("Error", (it.exception as FirebaseException).message.toString())
                }
            }

    }



}