package com.app.kainta.ui.servicio

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.kainta.R
import com.app.kainta.adaptadores.MostrarDireccionesAdapter
import com.app.kainta.databinding.FragmentSolicitarServicioBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import com.google.gson.Gson





class SolicitarServicioFragment : Fragment() {
    private var _binding: FragmentSolicitarServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: FirebaseAuth
    private lateinit var nombre: String
    private lateinit var db: FirebaseFirestore
    private lateinit var jsonUsuario: JSONObject
    private lateinit var jsonDireccion: JSONObject
    private lateinit var dialog: Dialog
    private lateinit var fecha : Timestamp
    private lateinit var hora : String
    private lateinit var minutos : String
    private lateinit var adaptador: MostrarDireccionesAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var idRequeridos : String
    private lateinit var idSolicitados : String

    private val picker =
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Appointment time")
            .build()

    private val datePicker =
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona fecha")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

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

        binding.txtServicio.text =
                "Solicitar el servicio de: ${jsonUsuario.getString("servicio").uppercase()}"


        //FECHA
        binding.btnFecha.setOnClickListener {
            datePicker.show(childFragmentManager, "")
        }

        datePicker.addOnPositiveButtonClickListener {
            val date = Date(it)
            fecha = Timestamp(date)
            binding.btnFecha.text = "Fecha: ${datePicker.headerText}"
        }


        //HORARIO
        binding.btnHora.setOnClickListener {
            picker.show(requireActivity().supportFragmentManager, "hora")
        }

        picker.addOnPositiveButtonClickListener {

            if (picker.minute < 10) {
                hora = picker.hour.toString()
                minutos = "0" + picker.minute.toString()
                binding.btnHora.text = "Horario: $hora : $minutos "
            } else {
                hora = picker.hour.toString()
                minutos = picker.minute.toString()
                binding.btnHora.text = "Horario: $hora : $minutos "
            }
        }


        //Direcciones
        binding.btnDireccion.setOnClickListener { showDialog() }


        binding.btnSoliciarServicio.setOnClickListener {


            val fechaActual = Timestamp(Date())
            val direccion = Gson().fromJson(
                jsonDireccion.toString(),
                HashMap::class.java
            )
            val refSolicitados = db.collection("usuario").document(user.currentUser!!.email!!)
                .collection("servicios_solicitados")

            val refRequeridos = db.collection("usuario").document(jsonUsuario.getString("email"))
                .collection("servicios_requeridos")

            idRequeridos = refRequeridos.document().id
            idSolicitados = refSolicitados.document().id

            db.collection("usuario").document(user.currentUser!!.email!!)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val data = mapOf<String, Any>(
                            "id" to idRequeridos,
                            "id_solicitados" to idSolicitados,
                            "servicio" to jsonUsuario.getString("servicio"),
                            "correo" to (it.result.data?.get("email")?.toString() ?: ""),
                            "nombre" to (it.result.data?.get("nombre")?.toString() ?: ""),
                            "titulo" to binding.editTitulo.text.toString(),
                            "descripcion" to binding.editDescripcion.text.toString(),
                            "fecha" to fecha,
                            "hora" to hora,
                            "estado" to "pendiente",
                            "minutos" to minutos,
                            "direccion" to direccion,
                            "fecha_creacion" to fechaActual
                        )
                        refRequeridos.document(idRequeridos).set(data)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    usuarioInformacion(direccion, fechaActual, refSolicitados)
                                } else {
                                    showAlert(
                                        "Error",
                                        (it.exception as FirebaseException).message.toString()
                                    )
                                }
                            }
                    } else {
                        showAlert(
                            "Error",
                            (it.exception as FirebaseException).message.toString()
                        )
                    }
                }
        }
    }

    private fun usuarioInformacion(direccion: HashMap<*, *>?, fechaActual : Timestamp, refSolicitados : CollectionReference) {

        db.collection("usuario").document(user.currentUser!!.email!!)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){

                    val data = mapOf<String, Any>(
                        "id" to idSolicitados,
                        "id_requeridos" to idRequeridos,
                        "servicio" to jsonUsuario.getString("servicio"),
                        "correo" to jsonUsuario.getString("email"),
                        "nombre" to jsonUsuario.getString("nombre"),
                        "titulo" to binding.editTitulo.text.toString(),
                        "descripcion" to binding.editDescripcion.text.toString(),
                        "fecha" to fecha,
                        "hora" to hora,
                        "estado" to "pendiente",
                        "minutos" to minutos,
                        "direccion" to (direccion ?: ""),
                        "fecha_creacion" to fechaActual
                    )

                    refSolicitados.document(idSolicitados).set(data)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                db.collection("valorados").document(jsonUsuario.getString("email"))
                                    .get().addOnSuccessListener { doc ->
                                        if(doc.exists())
                                            db.collection("valorados").document(jsonUsuario.getString("email"))
                                                .set(mapOf(
                                                    "valoracion" to (doc.getLong("valoracion")?.toInt()
                                                        ?.plus(1) ?: 0),
                                                    "correo" to jsonUsuario.getString("email")
                                                ))
                                        else
                                            db.collection("valorados").document(jsonUsuario.getString("email"))
                                                .set(mapOf(
                                                    "valoracion" to 1,
                                                    "correo" to jsonUsuario.getString("email")
                                                ))
                                    }
                                enviarNotificacion()
                                showAlert("Correcto",
                                    "Se realizó la solicitud correctamente")
                            }else{
                                showAlert("Error",
                                    (task.exception as FirebaseException).message.toString())
                            }
                        }
                }else{
                    showAlert("Error",
                        (it.exception as FirebaseException).message.toString())
                }
            }
    }


    private fun showDialog() {

        dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_mostrar_direcciones)

        val recyclerDirecciones = dialog.findViewById<RecyclerView>(R.id.recyclerDirecciones)

        dialog.findViewById<ImageButton>(R.id.btn_close)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    dialog.dismiss()
                }
            })

        val jsons = JSONArray()
        var jsonData : JSONObject = JSONObject()

        //Se obtienen las direcciones del usuario
        db.collection("usuario").document(user.currentUser?.email!!).collection("direcciones")
        .get()
            .addOnCompleteListener { documents ->
                if (!documents.result.isEmpty) {
                    for (document in documents.result) {
                        jsonData = JSONObject(document.data)
                        jsonData.put("id", document.id)
                        jsons.put(jsonData)
                    }
                    //Adaptador
                    adaptador = MostrarDireccionesAdapter(
                        binding.root.context,
                        R.layout.adapter_mostrar_direcciones,
                        jsons, object : MostrarDireccionesAdapter.OnItemClickListener {
                            override fun onItemClick(direccion : JSONObject) {

                                binding.txtDireccionNombre.text = "Nombre: "+ direccion.getString("nombre")
                                binding.txtDireccionDireccion.text ="Dirección: "+ direccion.getString("direccion")
                                binding.txtDireccionColonia.text ="Colonia: "+ direccion.getString("colonia")
                                binding.txtDireccionCP.text ="CP: "+ direccion.getString("cp")
                                binding.txtDireccionCiudad.text ="Ciudad: "+ direccion.getString("ciudad")
                                binding.txtDireccionTelefono.text ="Teléfono: "+ direccion.getString("telefono")
                                binding.layoutDireccion.visibility = View.VISIBLE

                                jsonDireccion = direccion
                                dialog.dismiss()

                            }

                        })
                    recyclerDirecciones.adapter = adaptador
                    recyclerDirecciones.layoutManager = LinearLayoutManager(requireContext())
                    dialog.show()

                }else{
                    showAlert("Error", "No tiene direcciones que mostrar")
                }
            }
    }

    private fun enviarNotificacion() {
        val token : String = jsonUsuario.getString("token")
        val keyMessaging = getString(R.string.default_key_messaging)

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        val jsonNotificacion = JSONObject()



            json.put("to" , token)
            jsonNotificacion.put("titulo", "Se solicita su servicio de: ${jsonUsuario.getString("servicio").uppercase()}")

            db.collection("usuario").document(user.currentUser!!.email!!)
                .get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        try {
                            nombre = it.result.data?.get("nombre").toString()
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
                    } else {
                        showAlert("Error", (it.exception as FirebaseException).message.toString())
                    }
                }

    }

    private fun showAlert(titulo : String,mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar") { _, _ ->
            findNavController().navigate(R.id.action_solicitarServicioFragment_to_servicioFragment)
        }
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

}