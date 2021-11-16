package com.app.kainta.modelos

class UpdateEmailModel {
    var email: String = ""
    var nombre : String = ""
    var apellidos : String = ""
    var provider: String = ""

    fun UpdateEmailModel(email : String, nombre : String, apellidos : String, provider : String)
    {
        this.email = email
        this.nombre = nombre
        this.apellidos = apellidos
        this.provider = provider
    }

    fun UpdateEmailModel(){

    }

}
