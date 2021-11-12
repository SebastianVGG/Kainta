package com.app.kainta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.kainta.databinding.ActivityAccountBinding
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



    }

    private fun setup() {
        title = "Crear Cuenta"

        //Crear cuenta BASIC

        binding.btnCrearCuenta.setOnClickListener {


            /*if(binding.editNombre.text.isNotEmpty() && binding.editApellidos.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.editUsuario.text.toString(),
                    binding.editContrasena.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful){
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }else{
                        showAlert()
                    }
                }

            }*/
        }
    }
}