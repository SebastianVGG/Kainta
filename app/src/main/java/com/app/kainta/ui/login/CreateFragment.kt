package com.app.kainta.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.app.kainta.HomeActivity
import com.app.kainta.ProviderType
import com.app.kainta.R
import com.app.kainta.databinding.FragmentCreateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setup()


        return root
    }

    private fun setup(){

        //Boton crear cuenta
        binding.btnCrearCuenta.setOnClickListener {

            val nombre = binding.editNombre.text
            val apellidos = binding.editApellidos.text
            val email = binding.editEmail.text
            val pass = binding.editPass.text
            val pass2 = binding.editPass2.text



            if(
                nombre.isNotEmpty() &&
                apellidos.isNotEmpty() &&
                email.isNotEmpty() &&
                pass.isNotEmpty() &&
                pass2.isNotEmpty()
            ){
                if(pass.toString() == pass2.toString()){
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                            email.toString(),
                            pass.toString()
                        ).addOnCompleteListener {
                            if(it.isSuccessful){
                                //Colocando datos en la base de datos
                                newUser(nombre.toString(),apellidos.toString(),email.toString())

                                showHome(it.result?.user?.email ?: "", ProviderType.BASIC)

                            }
                            else{
                                when ((it.exception as FirebaseAuthException).errorCode) {

                                    "ERROR_EMAIL_ALREADY_IN_USE" -> showAlert("El correo electronico ya se encuentra en uso.")
                                    else -> showAlert("MENSAJE DE ERROR: " +    (it.exception as FirebaseAuthException).message.toString())
                                }

                            }
                        }
                }else showAlert("Las contraseñas no son las mismas")
            }else showAlert("Todos los campos son obligatorios")
        }
    }

    //Creando nuevo usuario
    private fun newUser(nombre : String, apellidos : String, email : String){
        val db = Firebase.firestore

        val user = hashMapOf(
            "nombre" to nombre,
            "apellidos" to apellidos,
            "email" to email,
        )

        db.collection("usuario")
            .document(email)
            .set(user)
            .addOnSuccessListener { documentReference ->

            }
            .addOnFailureListener { e ->
                showAlert(e.message.toString())
            }
    }

    //Iniciar Home
    private fun showHome(email : String, provider : ProviderType){

        activity?.let{
            val homeIntent = Intent(it, HomeActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider.name)
            }
            it.startActivity(homeIntent)
            it.finish()
        }
    }

    //Alerta
    private fun showAlert(mensaje : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

}