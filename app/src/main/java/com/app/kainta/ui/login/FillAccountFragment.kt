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

            val nombre = binding.editNombre.text  .toString()
            val telefono = binding.editTelefono.text.toString()
            val bibliografia = binding.editBibliografia.text.toString()
            val ciudad = binding.editCiudad.text.toString()

            if(
                nombre.isNotEmpty()
            ){

            //Colocando datos en la base de datos
            newUser(nombre,telefono,bibliografia,ciudad,email)

            }else showAlert()
        }

    }

    //Creando nuevo usuario
    private fun newUser(nombre : String, telefono : String, bibliografia : String, ciudad : String, email : String){

        val db = Firebase.firestore

        val user = hashMapOf(
            "nombre" to nombre,
            "telefono" to telefono,
            "bibliografia" to bibliografia,
            "ciudad" to ciudad,
            "facebook" to "",
            "twitter" to "",
            "instagram" to "",
            "youtube" to "",
            "web" to "",
            "email" to email,
            "provider" to provider
        )

        db.collection("usuario")
            .document(email)
            .set(user)
            .addOnSuccessListener { documentReference ->
                showHome( email , ProviderType.BASIC)
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