package com.example.luggo.models

data class Bavul(
    var id: String = "",
    val ad: String = "",
    val esyalar: MutableList<Esya> = mutableListOf(),
    val userId: String = "",
    val tip: String = "man", // man, woman, boy, girl
    val agirlikSiniri: Double = 0.0
) {
    fun toplamAgirlik(): Double {
        return esyalar.sumOf { it.agirlik }
    }
} 