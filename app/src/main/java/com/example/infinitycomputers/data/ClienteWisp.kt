package com.example.infinitycomputers.data

data class ClienteWisp(
    val idCliente: String = "",
    val nombreCompleto: String = "",
    val telefono: String = "",
    val planMensual: String = "",
    val localidad: String = "",
    val direccionIp: String = "",
    val estadoServicio: String = "Activo"
)