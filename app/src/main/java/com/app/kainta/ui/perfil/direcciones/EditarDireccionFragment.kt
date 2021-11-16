package com.app.kainta.ui.perfil.direcciones

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.kainta.R
import com.app.kainta.adaptadores.DireccionesAdapter
import com.app.kainta.databinding.FragmentConfigDireccionesBinding
import com.app.kainta.databinding.FragmentEditarDireccionBinding
import com.app.kainta.modelos.DireccionModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject


class EditarDireccionFragment : Fragment(){
    private var _binding: FragmentEditarDireccionBinding? = null
    private val binding get() = _binding!!
    private var jsonDireccion : JSONObject = JSONObject()
    private lateinit var adaptador : DireccionesAdapter
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditarDireccionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        jsonDireccion = JSONObject(arguments?.getString("jsonDireccion").toString())

        setup()

        return root
    }

    private fun setup(){


        binding.editNombre.setText(jsonDireccion.getString("nombre"))
        binding.editDireccion.setText(jsonDireccion.getString("direccion"))
        binding.editColonia.setText(jsonDireccion.getString("colonia"))
        binding.editCP.setText(jsonDireccion.getString("cp"))
        binding.editCiudad.setText(jsonDireccion.getString("ciudad"))
        binding.editTelefono.setText(jsonDireccion.getString("telefono"))

        binding.btnAceptar.setOnClickListener {

            val nombre = binding.editNombre.text.toString()
            val direccion = binding.editDireccion.text.toString()
            val colonia = binding.editColonia.text.toString()
            val cp = binding.editCP.text.toString()
            val ciudad = binding.editCiudad.text.toString()
            val telefono = binding.editTelefono.text.toString()

            val docRef = db.
                collection("usuario").document(user.currentUser?.email!!)
                .collection("direcciones").document(jsonDireccion.getString("id"))

            docRef.update(mapOf(
                "nombre" to nombre,
                "direccion" to direccion,
                "colonia" to colonia,
                "cp" to cp,
                "ciudad" to ciudad,
                "telefono" to telefono
            )).addOnSuccessListener {
                            showAlert(
                                "Correcto",
                                "Se acutalizo correctamente la dirección"
                            )

                }
                .addOnFailureListener { e -> showAlert("Error", (e as FirebaseAuthException).message.toString()) }
        }

    }

    private fun showAlert(titulo : String,mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

}