package com.app.kainta.modelos

class DireccionModel {
    var nombre: String = ""
    var direccion : String = ""
    var colonia : String = ""
    var cp: String = ""
    var telefono: String = ""

    fun DireccionModel(nombre : String, direccion : String, colonia : String, cp : String, telefono : String)
    {
       this.nombre = nombre
        this.direccion = direccion
        this.colonia = colonia
        this.cp = cp
        this.telefono = telefono
    }

    fun DireccionModel(){

    }

}