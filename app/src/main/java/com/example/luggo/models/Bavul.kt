package com.example.luggo.models

data class Bavul(
    var id: String = "",
    val ad: String = "",
    val esyalar: MutableList<Esya> = mutableListOf(),
    val userId: String = ""
) {
    fun toplamAgirlik(): Double {
        return esyalar.sumOf { it.agirlik }
    }
} 