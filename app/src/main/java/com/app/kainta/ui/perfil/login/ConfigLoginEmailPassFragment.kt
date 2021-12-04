package com.app.kainta.ui.perfil.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.kainta.R
import com.app.kainta.databinding.FragmentConfigLoginEmailPassBinding
import com.app.kainta.modelos.UpdateEmailModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class ConfigLoginEmailPassFragment : Fragment() {

    private var _binding: FragmentConfigLoginEmailPassBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var infoUser: UpdateEmailModel
    private lateinit var motivo: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfigLoginEmailPassBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore
        motivo = arguments?.getString("motivo").toString()

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Configuración de Login"

        setup()


        return root
    }

    private fun setup() {
        setVisibility()
        val email: String = user.currentUser?.email.toString()


        //Boton modificar correo
        binding.btnCorreo.setOnClickListener {


            //Se obtienen las creedenciales
            val credential: AuthCredential = EmailAuthProvider
                .getCredential(
                    binding.editCorreoActual.text.toString(),
                    binding.editPassActual.text.toString()
                )
            //El usuario vuelve a iniciar sesion
            user.currentUser?.reauthenticate(credential)
                ?.addOnSuccessListener {
                    //Se actualiza email de firebaseauth
                    user.currentUser!!.updateEmail(binding.editCorreo.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Se obtiene el documento del usuario
                                val docRef = db.collection("usuario").document(email)
                                docRef.get()
                                    .addOnSuccessListener { document ->
                                        //Try transformar datos de la firestore a un objeto UpdateEmailModel
                                        try {
                                            infoUser = document.toObject<UpdateEmailModel>()!!
                                            //Se elimina el documento para crear uno nuevo con el nuevo correo
                                            db.collection("usuario").document(email)
                                                .delete()
                                                .addOnSuccessListener {
                                                    //Se convierte de objeto a map
                                                    val oMapper = ObjectMapper()
                                                    val map: HashMap<*, *>? =
                                                        oMapper.convertValue(
                                                            infoUser,
                                                            HashMap::class.java
                                                        )
                                                    //Se crea el nuevo documento con la información
                                                    val docProvider = db.collection("usuario")
                                                        .document(binding.editCorreo.text.toString())
                                                    docProvider.set(map ?: "")
                                                        .addOnSuccessListener {
                                                            //Se actualiza el correo dentro del usuario al nuevo correo
                                                            docProvider.update(
                                                                "email",
                                                                binding.editCorreo.text.toString()
                                                            )
                                                                .addOnSuccessListener {
                                                                    showAlert(
                                                                        "Correcto",
                                                                        "Se acutalizo correctamente"
                                                                    )
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            showAlert(
                                                                "Error",
                                                                (e as FirebaseAuthException).message.toString()
                                                            )
                                                        }

                                                }
                                                .addOnFailureListener { e ->
                                                    showAlert(
                                                        "Error",
                                                        (e as FirebaseAuthException).message.toString()
                                                    )
                                                }


                                        } catch (e: Exception) {
                                            showAlert(
                                                "Error",
                                                (e.toString())
                                            )
                                        }
                                    }
                                    .addOnFailureListener {
                                        showAlert(
                                            "Error",
                                            (it as FirebaseAuthException).message.toString()
                                        )
                                    }
                            } else showAlert(
                                "Error",
                                (task.exception as FirebaseAuthException).message.toString()
                            )
                        }
                }
                ?.addOnFailureListener {
                    showAlert(
                        "Error",
                        (it as FirebaseAuthException).message.toString()
                    )
                }
        }

        //Boton modificar contraseña
        binding.btnPass.setOnClickListener {

            val passActual = binding.editPass.text.toString()
            val nuevaPass = binding.editNuevaPass.text.toString()

            //Se obtienen las creedenciales
            val credential: AuthCredential = EmailAuthProvider
                .getCredential(
                    binding.editCorreoActualPass.text.toString(),
                    passActual
                )
            //El usuario vuelve a iniciar sesion
            user.currentUser?.reauthenticate(credential)
                ?.addOnSuccessListener {
                    //Se actualiza la contraseña
                    user.currentUser!!.updatePassword(nuevaPass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) showAlert(
                                "Correcto",
                                "Se actualizo correctamente su contraseña"
                            )
                            else showAlert(
                                "Error",
                                (task.exception as FirebaseAuthException).message.toString()
                            )
                        }
                }
                ?.addOnFailureListener {
                    showAlert("Error", (it as FirebaseAuthException).message.toString())
                }

        }


    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar") { _,_ ->
            activity?.onBackPressed()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    //Colocar la vista correcta
    private fun setVisibility() {

        when (motivo) {
            "correo" -> {
                binding.layoutPass.visibility = View.GONE

                binding.editCorreoActual.setText(user.currentUser!!.email)
            }
            "pass" -> {
                binding.layoutCorreo.visibility = View.GONE

                binding.editCorreoActualPass.setText(user.currentUser!!.email)
            }

        }
    }

}