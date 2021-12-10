package com.app.kainta.ui.perfil.direcciones

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private lateinit var dialogAlert : Dialog

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



            if(binding.editNombre.text.isNotEmpty()){
                binding.editNombre.background = context?.getDrawable(R.drawable.custom_edit)
                binding.editNombre.error = null

                if(binding.editDireccion.text.isNotEmpty()){

                    binding.editDireccion.background = context?.getDrawable(R.drawable.custom_edit)
                    binding.editDireccion.error = null

                    if(binding.editColonia.text.isNotEmpty()){

                        binding.editColonia.background = context?.getDrawable(R.drawable.custom_edit)
                        binding.editColonia.error = null

                        if(binding.editCP.text.isNotEmpty()){

                            binding.editCP.background = context?.getDrawable(R.drawable.custom_edit)
                            binding.editCP.error = null

                            if(binding.editCiudad.text.isNotEmpty()){

                                binding.editCiudad.background = context?.getDrawable(R.drawable.custom_edit)
                                binding.editCiudad.error = null

                                if(binding.editTelefono.text.isNotEmpty()){

                                    binding.editTelefono.background = context?.getDrawable(R.drawable.custom_edit)
                                    binding.editTelefono.error = null


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



                                }else{
                                    binding.editTelefono.background = context?.getDrawable(R.drawable.custom_edit_error)
                                    binding.editTelefono.error = "El campo es obligatorio."
                                }

                            }else{
                                binding.editCiudad.background = context?.getDrawable(R.drawable.custom_edit_error)
                                binding.editCiudad.error = "El campo es obligatorio."
                            }

                        }else{
                            binding.editCP.background = context?.getDrawable(R.drawable.custom_edit_error)
                            binding.editCP.error = "El campo es obligatorio."

                        }

                    }else{
                        binding.editColonia.background = context?.getDrawable(R.drawable.custom_edit_error)
                        binding.editColonia.error = "El campo es obligatorio."
                    }

                }else{
                    binding.editDireccion.background = context?.getDrawable(R.drawable.custom_edit_error)
                    binding.editDireccion.error = "El campo es obligatorio."
                }

            }else{
                binding.editNombre.background = context?.getDrawable(R.drawable.custom_edit_error)
                binding.editNombre.error = "El campo es obligatorio."
            }
        }


    }

    private fun showAlert(titulo : String,mensaje : String){
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