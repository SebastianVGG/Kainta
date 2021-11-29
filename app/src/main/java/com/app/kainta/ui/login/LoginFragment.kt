package com.app.kainta.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.app.kainta.*
import com.app.kainta.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.analytics.FirebaseAnalytics

import android.R.id
import android.content.Context
import androidx.appcompat.app.AppCompatActivity


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val GOOGLE_SING_IN = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setup()

        return binding.root
    }

    private fun setup(){

        binding.btnCrearCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_createFragment)
        }

        //Iniciar sesion BASIC

        binding.btnIniciarSesion.setOnClickListener {

            val prefs = requireActivity().getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE)
            val tokenc = prefs.getString("token", null).toString()

            if(binding.editUsuario.text.isNotEmpty() && binding.editContrasena.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.editUsuario.text.toString(),
                    binding.editContrasena.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful){
                        val db = Firebase.firestore
                        val docRef = db.collection("usuario").document(it.result?.user?.email ?: "")
                        docRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    if(document.contains("nombre") && document.contains("email")){

                                        val data = hashMapOf("provider" to "BASIC", "token" to tokenc)
                                        val docProvider = db.collection("usuario").document(it.result.user?.email ?: "")
                                        docProvider.set(data, SetOptions.merge())
                                            .addOnCompleteListener { document ->
                                                if (document.isSuccessful)
                                                    showHome(it.result.user?.email ?: "", ProviderType.BASIC)
                                                else showAlert()
                                            }
                                            .addOnFailureListener { showAlert() }
                                    }else{
                                        val bundle = Bundle()
                                        bundle.putString("email", it.result?.user?.email ?: "")
                                        bundle.putString("provider", "BASIC")
                                        findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                    }
                                } else {
                                    val bundle = Bundle()
                                    bundle.putString("email", it.result?.user?.email ?: "")
                                    bundle.putString("provider", "BASIC")
                                    findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                }
                            }
                            .addOnFailureListener { exception ->
                                showAlert()
                            }
                    }else{
                        showAlert()
                    }
                }

            }
        }

        //Iniciar sesion GOOGLE

        binding.btnIniciarSesionGoogle.setOnClickListener {

            val googleConfig = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id_))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(requireActivity(), googleConfig)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SING_IN)
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SING_IN){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if(account != null){

                    //Se recuperan credenciales de Google

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    //Se inicia sesion en firebase con la credencial de Google

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                        if(it.isSuccessful){
                            val prefs = requireActivity().getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE)
                            val tokenc = prefs.getString("token", null).toString()
                            val db = Firebase.firestore
                            val docRef = db.collection("usuario").document(account.email)
                                docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        if(document.contains("nombre") && document.contains("email")){

                                            val data = hashMapOf("provider" to "GOOGLE", "token" to tokenc)
                                            val docProvider = db.collection("usuario").document(account.email)
                                            docProvider.set(data, SetOptions.merge())
                                                .addOnCompleteListener { document ->

                                                    if (document.isSuccessful) showHome(account.email ?: "", ProviderType.GOOGLE)
                                                    else showAlert()

                                                    }
                                        } else{
                                            val bundle = Bundle()
                                            bundle.putString("email", account.email)
                                            bundle.putString("provider", "GOOGLE")
                                            findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                        }
                                    } else {
                                        //Se coloca este fragmento para llenar informacion faltante
                                        val bundle = Bundle()
                                        bundle.putString("email", account.email)
                                        bundle.putString("provider", "GOOGLE")
                                        findNavController().navigate(R.id.action_loginFragment_to_fillAccountFragment, bundle)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    showAlert()
                                }

                        }else{
                            showAlert()
                        }
                    }

                }
            }catch (e: ApiException){
                showAlert()
            }

        }
    }

}


