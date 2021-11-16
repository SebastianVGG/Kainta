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
import com.app.kainta.databinding.FragmentFillAccountBinding
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FillAccountFragment : Fragment() {

    private var _binding: FragmentFillAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var email : String
    private lateinit var provider : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFillAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        email = arguments?.getString("email").toString()
        provider = arguments?.getString("provider").toString()

        setup()


        return root
    }

    private fun setup(){

        //Boton crear cuenta
        binding.btnCrearCuenta.setOnClickListener {

            val nombre = binding.editNombre.text
            val apellidos = binding.editApellidos.text

            if(
                nombre.isNotEmpty() &&
                apellidos.isNotEmpty()
            ){

            //Colocando datos en la base de datos
            newUser(nombre.toString(),apellidos.toString(),email.toString())

            showHome( email , ProviderType.BASIC)

            }else showAlert()
        }

        /*binding.back.setOnClickListener {
            replaceFragment(LoginFragment())
        }*/


    }

    //Creando nuevo usuario
    private fun newUser(nombre : String, apellidos : String, email : String){
        val db = Firebase.firestore



        val user = hashMapOf(
            "nombre" to nombre,
            "apellidos" to apellidos,
            "email" to email,
            "provider" to provider
        )

        db.collection("usuario")
            .document(email)
            .set(user, SetOptions.merge())
            .addOnSuccessListener { documentReference ->

            }
            .addOnFailureListener { e ->
                showAlert()
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
    private fun showAlert(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }
}