package com.app.kainta.ui.perfil.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.app.kainta.R
import com.app.kainta.databinding.FragmentConfigLoginEmailPassBinding
import com.app.kainta.databinding.FragmentEditInfoPersonalBinding
import com.app.kainta.modelos.UpdateEmailModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditInfoPersonalFragment : Fragment() {
    private var _binding: FragmentEditInfoPersonalBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditInfoPersonalBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore


        setup()


        return root
    }

    private fun setup() {

        val email: String = user.currentUser?.email.toString()

        //Boton modificar nombre
        binding.btnAceptar.setOnClickListener {

            val telefono = binding.editTelefono.text.toString().toLongOrNull()


            val data = hashMapOf(
                "nombre" to binding.editNombre.text.toString(),
                "telefono" to telefono
            )

            db.collection("usuario").document(email)
                .set(data, SetOptions.merge())
                .addOnCompleteListener {

                    if (it.isSuccessful) showAlert(
                        "Correcto",
                        "Se actualizo correctamente"
                    )
                    else showAlert(
                        "Error",
                        (it.exception as FirebaseAuthException).message.toString()
                    )

                }
        }

    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}