package com.app.kainta.ui.perfil.direcciones

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.app.kainta.R
import com.app.kainta.databinding.FragmentConfigPerfilBinding
import com.app.kainta.databinding.FragmentNuevaDireccionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class NuevaDireccionFragment : Fragment() {
    private var _binding: FragmentNuevaDireccionBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNuevaDireccionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Nueva Dirección"

        setup()

        return root
    }

    private fun setup(){

        binding.btnAceptar.setOnClickListener {

            val docRef = db.collection("usuario").document(user.currentUser?.email!!).collection("direcciones")

            val data = hashMapOf(
                "nombre" to binding.editNombre.text.toString(),
                "direccion" to binding.editDireccion.text.toString(),
                "colonia" to binding.editColonia.text.toString(),
                "cp" to binding.editCP.text.toString(),
                "ciudad" to binding.editCiudad.text.toString(),
                "telefono" to binding.editTelefono.text.toString()
            )

                docRef.add(data)
                .addOnSuccessListener {
                    showAlert(
                        "Correcto",
                        "Se agrego correctamente su direccion"
                    )
                }.addOnFailureListener {
                        showAlert(
                            "Error",
                            (it as FirebaseAuthException).message.toString()
                        )
                    }


        }


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


}