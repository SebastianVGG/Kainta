package com.app.kainta.ui.perfil.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.app.kainta.ProviderType
import com.app.kainta.R
import com.app.kainta.databinding.FragmentConfigDireccionesBinding
import com.app.kainta.databinding.FragmentConfigLoginInfoBinding
import com.app.kainta.modelos.UpdateEmailModel
import com.google.firebase.auth.*
import com.google.firebase.auth.EmailAuthProvider.getCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*
import kotlin.collections.HashMap


class ConfigLoginInfoFragment : Fragment() {

    private var _binding: FragmentConfigLoginInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var infoUser : UpdateEmailModel
    private lateinit var motivo : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConfigLoginInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        user = Firebase.auth
        db = Firebase.firestore
        motivo = arguments?.getString("motivo").toString()

        setup()


        return root
    }

    private fun setup() {
        setVisibility()


        val email : String = user.currentUser?.email.toString()

        //Boton modificar nombre
        binding.btnNombre.setOnClickListener {

            val data = hashMapOf("nombre" to binding.editNombre.text.toString())

            db.collection("usuario").document(email)
                .set(data, SetOptions.merge())
                .addOnCompleteListener {

                    if (it.isSuccessful) showAlert(
                        "Correcto",
                        "Se actualizo correctamente su nombre"
                    )
                    else showAlert(
                        "Error",
                        (it.exception as FirebaseAuthException).message.toString()
                    )

                }
        }

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
                                                    val docProvider = db.collection("usuario").document(binding.editCorreo.text.toString())
                                                    docProvider.set(map ?: "")
                                                        .addOnSuccessListener {
                                                            //Se actualiza el correo dentro del usuario al nuevo correo
                                                            docProvider.update("email",binding.editCorreo.text.toString())
                                                                .addOnSuccessListener {
                                                                    showAlert(
                                                                        "Correcto",
                                                                        "Se acutalizo correctamente"
                                                                    )
                                                                }
                                                        }
                                                        .addOnFailureListener { e -> showAlert("Error", (e as FirebaseAuthException).message.toString()) }

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

        //Boton modificar telefono
        binding.btnTelefono.setOnClickListener {

            val telefono = binding.editTelefono.text.toString().toLongOrNull()

            if(telefono != null){
                val data = hashMapOf("telefono" to telefono)

                //Se actualiza telefono en firestore
                db.collection("usuario").document(email)
                    .set(data, SetOptions.merge())
                    .addOnCompleteListener {
                        //Si es correcto la actualizacion en base de datos entonces
                        if (it.isSuccessful) showAlert(
                            "Correcto",
                            "Se actualizo correctamente su telefono"
                        )
                        else showAlert(
                            "Error",
                            (it.exception as FirebaseAuthException).message.toString()
                        )
                    }
            }else showAlert("Error", "El telefono es incorrecto")
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




    //Colocar la vista correcta
    private fun setVisibility(){

        when(motivo){

            "nombre" -> {
                binding.layoutCorreo.visibility = View.GONE
                binding.layoutPass.visibility = View.GONE
                binding.layoutTelefono.visibility = View.GONE
            }

            "correo" -> {
                binding.layoutNombre.visibility = View.GONE
                binding.layoutPass.visibility = View.GONE
                binding.layoutTelefono.visibility = View.GONE

                binding.editCorreoActual.setText(user.currentUser!!.email)
            }
            "pass" -> {
                binding.layoutNombre.visibility = View.GONE
                binding.layoutCorreo.visibility = View.GONE
                binding.layoutTelefono.visibility = View.GONE

                binding.editCorreoActualPass.setText(user.currentUser!!.email)
            }
            "telefono" -> {
                binding.layoutNombre.visibility = View.GONE
                binding.layoutCorreo.visibility = View.GONE
                binding.layoutPass.visibility = View.GONE
            }

        }
    }

}