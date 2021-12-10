package com.app.kainta.ui.servicio

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
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
import java.text.SimpleDateFormat

class SolicitarServicioFragment : Fragment() {
    private var _binding: FragmentSolicitarServicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: FirebaseAuth
    private lateinit var nombre: String
    private lateinit var db: FirebaseFirestore
    private lateinit var jsonUsuario: JSONObject
    private var jsonDireccion: JSONObject = JSONObject()
    private lateinit var dialog: Dialog
    private lateinit var fecha : Timestamp
    private lateinit var hora : String
    private lateinit var minutos : String
    private lateinit var adaptador: MostrarDireccionesAdapter
    private lateinit var idRequeridos : String
    private lateinit var idSolicitados : String
    private lateinit var dialogLoading : Dialog
    private lateinit var dialogAlert : Dialog
    private var txtFecha : String = ""
    private var txtHora : String = ""
    private val today = MaterialDatePicker.todayInUtcMilliseconds()
    private val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    private val constraintsBuilder =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .setStart(today)

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
            .setSelection(calendar.timeInMillis)
            .setCalendarConstraints(constraintsBuilder.build())
            .build()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSolicitarServicioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Solicitando servicio"

        jsonUsuario = JSONObject(arguments?.getString("jsonUsuario").toString())

        setup()

        return root
    }

    private fun setup() {

        inicializarLoading()

        if(txtFecha != "")
            binding.btnFecha.text = txtFecha

        if(txtHora != "")
            binding.btnHora.text = txtHora

        binding.txtServicio.text =
                "Solicitar el servicio de: ${jsonUsuario.getString("servicio").uppercase()}"


        //FECHA
        binding.btnFecha.setOnClickListener {
            datePicker.show(childFragmentManager, "")
        }

        datePicker.addOnPositiveButtonClickListener {

            val utcTime = Date(it)
            val format = "yyy/MM/dd HH:mm:ss"
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("GMT+8")
            val gmtTime = SimpleDateFormat(format, Locale.getDefault()).parse(sdf.format(utcTime))
            gmtTime?.let {  date ->
                println(date)
                fecha = Timestamp(date)
            }
            txtFecha = "Fecha: ${datePicker.headerText}"
            binding.btnFecha.text = txtFecha

        }


        //HORARIO
        binding.btnHora.setOnClickListener {
            picker.show(requireActivity().supportFragmentManager, "hora")
        }

        picker.addOnPositiveButtonClickListener {

            if (picker.minute < 10) {
                hora = picker.hour.toString()
                minutos = "0" + picker.minute.toString()
                txtHora = "Horario: $hora : $minutos "
                binding.btnHora.text = txtHora
            } else {
                hora = picker.hour.toString()
                minutos = picker.minute.toString()
                txtHora = "Horario: $hora : $minutos "
                binding.btnHora.text = txtHora
            }
        }


        //Direcciones
        binding.btnDireccion.setOnClickListener { showDialog() }


        binding.btnSoliciarServicio.setOnClickListener {

            if(binding.editTitulo.text.isNotEmpty()){

                binding.editTitulo.background = context?.getDrawable(R.drawable.custom_edit)
                binding.editTitulo.error = null

                if(binding.editDescripcion.text.isNotEmpty()){

                    binding.editDescripcion.background = context?.getDrawable(R.drawable.custom_edit)
                    binding.editDescripcion.error = null

                    if(txtFecha != ""){
                        binding.btnFecha.setTextColor(Color.parseColor("#000000"))

                        if(txtHora != ""){
                            binding.btnHora.setTextColor(Color.parseColor("#000000"))

                            if(jsonDireccion.length() != 0){

                                binding.btnDireccion.setTextColor(Color.parseColor("#000000"))

                                if(binding.checkBox.isChecked){

                                    binding.checkBox.setTextColor(Color.parseColor("#000000"))

                                    dialogLoading.show()

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
                                                            dialogLoading.dismiss()
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

                                }else{
                                    binding.checkBox.setTextColor(Color.parseColor("#e62f22"))
                                }

                            }else{
                                binding.btnDireccion.setTextColor(Color.parseColor("#e62f22"))
                                showSnackBar(binding.layoutPrincipal, "Debe de seleccionar una dirección")
                            }
                        }else{
                            binding.btnHora.setTextColor(Color.parseColor("#e62f22"))
                        }
                    }else{
                        binding.btnFecha.setTextColor(Color.parseColor("#e62f22"))
                    }
                }else{
                    binding.editDescripcion.background = context?.getDrawable(R.drawable.custom_edit_error)
                    binding.editDescripcion.error = "Este campo es obligatorio."
                }
            }else{
                binding.editTitulo.background = context?.getDrawable(R.drawable.custom_edit_error)
                binding.editTitulo.error = "Este campo es obligatorio."
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

        if(dialog.window!=null)
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))

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
                    dialog.findViewById<TextView>(R.id.textView4).visibility = View.VISIBLE
                    dialog.findViewById<Button>(R.id.btnAddDireccion).visibility = View.VISIBLE
                    dialog.show()
                }
            }

        dialog.findViewById<Button>(R.id.btnAddDireccion).setOnClickListener {
            findNavController().navigate(R.id.action_solicitarServicioFragment_to_nuevaDireccionFragment)
            dialog.dismiss()
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
            findNavController().navigate(R.id.action_solicitarServicioFragment_to_servicioFragment)
        }

        if(dialogAlert.window!=null)
            dialogAlert.window?.setBackgroundDrawable(ColorDrawable(0))

        dialogAlert.show()

    }


    private fun showSnackBar(view: ConstraintLayout, text: String) {

        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
        val snackbarLayout : Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        val customView = layoutInflater.inflate(R.layout.custom_snackbar, null)

        customView.findViewById<TextView>(R.id.btnOK).setOnClickListener {
            snackbar.dismiss()
        }
        customView.findViewById<TextView>(R.id.txtSnackBar).text = text

        snackbarLayout.setPadding(0,0,0,0)
        snackbarLayout.addView(customView, 0)

        snackbar.view.setBackgroundColor(Color.TRANSPARENT)

        snackbar.show()

    }

    private fun inicializarLoading() {
        dialogLoading = Dialog(requireContext())
        dialogLoading.setContentView(R.layout.dialog_loading)
        dialogLoading.setCancelable(false)
        if(dialogLoading.window!=null)
            dialogLoading.window?.setBackgroundDrawable(ColorDrawable(0))
    }

}