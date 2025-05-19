package com.example.luggo.models

data class EsyaKategori(
    val ad: String,
    val altKategoriler: List<AltKategori>
)

data class AltKategori(
    val ad: String,
    val varsayilanAgirlik: Int // gram cinsinden
)

object EsyaKategorileri {
    val kategoriler = listOf(
        EsyaKategori(
            "Giyim",
            listOf(
                AltKategori("Tişört", 200),
                AltKategori("Gömlek", 250),
                AltKategori("Pantolon", 400),
                AltKategori("Etek", 300),
                AltKategori("Elbise", 500),
                AltKategori("Kazak", 350),
                AltKategori("Mont", 1000),
                AltKategori("İç Çamaşırı", 100),
                AltKategori("Çorap", 50)
            )
        ),
        EsyaKategori(
            "Ayakkabı",
            listOf(
                AltKategori("Spor Ayakkabı", 800),
                AltKategori("Klasik Ayakkabı", 600),
                AltKategori("Bot", 1000),
                AltKategori("Terlik", 300),
                AltKategori("Sandalet", 400)
            )
        ),
        EsyaKategori(
            "Elektronik",
            listOf(
                AltKategori("Laptop", 2000),
                AltKategori("Tablet", 500),
                AltKategori("Telefon", 200),
                AltKategori("Şarj Aleti", 100),
                AltKategori("Kulaklık", 150),
                AltKategori("Powerbank", 300)
            )
        ),
        EsyaKategori(
            "Kozmetik",
            listOf(
                AltKategori("Şampuan", 300),
                AltKategori("Deodorant", 150),
                AltKategori("Diş Fırçası", 50),
                AltKategori("Diş Macunu", 100),
                AltKategori("Makaj Malzemeleri", 200),
                AltKategori("Parfüm", 100)
            )
        ),
        EsyaKategori(
            "Diğer",
            listOf(
                AltKategori("Kitap", 500),
                AltKategori("Havlu", 300),
                AltKategori("Şemsiye", 400),
                AltKategori("Çanta", 800),
                AltKategori("Hediyelik Eşya", 300)
            )
        )
    )
} 