package com.app.kainta.ui.perfil.login

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.app.kainta.R
import com.app.kainta.databinding.FragmentEditInfoPersonalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditInfoPersonalFragment : Fragment() {
    private var _binding: FragmentEditInfoPersonalBinding? = null
    private val binding get() = _binding!!
    private lateinit var user : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var dialogAlert : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditInfoPersonalBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = binding.progressBar

        user = Firebase.auth
        db = Firebase.firestore

        activity?.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        activity?.findViewById<TextView>(R.id.txtToolbar)?.text = "Editando Información Personal"

        setup()


        return root
    }

    private fun setup() {

        val email: String = user.currentUser?.email.toString()
        llenarCampos()
        binding.btnAceptar.setOnClickListener {

            if(binding.editNombre.text.isNotEmpty()){
                binding.editNombre.background = context?.getDrawable(R.drawable.custom_edit)
                binding.editNombre.error = null

                if(binding.editBibliografia.text.isNotEmpty()){

                    binding.editBibliografia.background = context?.getDrawable(R.drawable.custom_edit)
                    binding.editBibliografia.error = null

                    if(binding.editCiudad.text.isNotEmpty()){

                        binding.editCiudad.background = context?.getDrawable(R.drawable.custom_edit)
                        binding.editCiudad.error = null

                        if(binding.editTelefono.text.isNotEmpty()){

                            binding.editTelefono.background = context?.getDrawable(R.drawable.custom_edit)
                            binding.editTelefono.error = null


                            val telefono = binding.editTelefono.text.toString()
                            val nombre = binding.editNombre.text.toString()
                            val bibliografia = binding.editBibliografia.text.toString()
                            val ciudad = binding.editCiudad.text.toString()
                            val facebook = binding.editFacebook.text.toString()
                            val twitter = binding.editTwitter.text.toString()
                            val instagram = binding.editInstagram.text.toString()
                            val youtube = binding.editYoutube.text.toString()
                            val web = binding.editPaginaWeb.text.toString()


                            val data = hashMapOf(
                                "nombre" to nombre,
                                "telefono" to telefono,
                                "bibliografia" to bibliografia,
                                "ciudad" to ciudad,
                                "facebook" to facebook,
                                "twitter" to twitter,
                                "instagram" to instagram,
                                "youtube" to youtube,
                                "web" to web
                            )

                            db.collection("usuario").document(email)
                                .update(data as Map<String, Any>)
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

                        }else{
                            binding.editTelefono.background = context?.getDrawable(R.drawable.custom_edit_error)
                            binding.editTelefono.error = "El campo es obligatorio."

                        }

                    }else{
                        binding.editCiudad.background = context?.getDrawable(R.drawable.custom_edit_error)
                        binding.editCiudad.error = "El campo es obligatorio."
                    }

                }else{
                    binding.editBibliografia.background = context?.getDrawable(R.drawable.custom_edit_error)
                    binding.editBibliografia.error = "El campo es obligatorio."
                }

            }else{
                binding.editNombre.background = context?.getDrawable(R.drawable.custom_edit_error)
                binding.editNombre.error = "El campo es obligatorio."
            }
        }

        }





    private fun llenarCampos() {

        val email: String = user.currentUser?.email.toString()

        db.collection("usuario").document(email)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    binding.editNombre.setText(it.result.data?.get("nombre").toString())
                    binding.editBibliografia.setText(it.result.data?.get("bibliografia").toString())
                    binding.editTelefono.setText(it.result.data?.get("telefono").toString())
                    binding.editCiudad.setText(it.result.data?.get("ciudad").toString())
                    binding.editFacebook.setText(it.result.data?.get("facebook").toString())
                    binding.editTwitter.setText(it.result.data?.get("twitter").toString())
                    binding.editInstagram.setText(it.result.data?.get("instagram").toString())
                    binding.editYoutube.setText(it.result.data?.get("youtube").toString())
                    binding.editPaginaWeb.setText(it.result.data?.get("web").toString())

                    progressBar.visibility = View.GONE
                    binding.layout.visibility = View.VISIBLE

                }else{
                    showAlert(
                        "Error",
                        (it.exception as FirebaseAuthException).message.toString())
                    progressBar.visibility = View.GONE
                    binding.layout.visibility = View.VISIBLE
                }
            }

    }

    private fun showAlert(titulo: String, mensaje: String) {

        dialogAlert = Dialog(requireContext())

        dialogAlert.setContentView(R.layout.dialog_alert)

        dialogAlert.findViewById<TextView>(R.id.txtTitulo).text = titulo
        dialogAlert.findViewById<TextView>(R.id.txtMensaje).text = mensaje
        dialogAlert.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialogAlert.dismiss()
        }
        dialogAlert.findViewById<Button>(R.id.btnAceptar).setOnClickListener {
            dialogAlert.dismiss()
        }
        dialogAlert.setOnDismissListener {
            activity?.onBackPressed()
        }

        if(dialogAlert.window!=null)
            dialogAlert.window?.setBackgroundDrawable(ColorDrawable(0))

        dialogAlert.show()

    }


}