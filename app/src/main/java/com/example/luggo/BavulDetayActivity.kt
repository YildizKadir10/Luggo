package com.example.luggo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luggo.adapters.EsyaAdapter
import com.example.luggo.models.AltKategori
import com.example.luggo.models.Bavul
import com.example.luggo.models.Esya
import com.example.luggo.models.EsyaKategori
import com.example.luggo.models.EsyaKategorileri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BavulDetayActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabYeniEsya: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var buttonDeleteBavul: ImageButton
    private val esyalar = mutableListOf<Esya>()
    private lateinit var adapter: EsyaAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var bavulId: String = ""
    private var bavul: Bavul? = null

    // Dialog bileşenleri
    private lateinit var spinnerKategori: Spinner
    private lateinit var spinnerAltKategori: Spinner
    private lateinit var editTextAdet: EditText
    private lateinit var textViewToplamAgirlik: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_bavul_detay)

            // Toolbar'ı ayarla
            toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
                title = "Bavul Detayı"
            }

            bavulId = intent.getStringExtra("bavul_id") ?: run {
                Toast.makeText(this, "Bavul bulunamadı", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            buttonDeleteBavul = findViewById(R.id.buttonDeleteBavul)
            recyclerView = findViewById(R.id.recyclerViewEsyalar)
            fabYeniEsya = findViewById(R.id.fabYeniEsya)

            adapter = EsyaAdapter(esyalar) { esya ->
                esyaSil(esya)
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

            buttonDeleteBavul.setOnClickListener {
                bavulSil()
            }

            fabYeniEsya.setOnClickListener {
                yeniEsyaEkle()
            }

            bavulBilgileriniYukle()
            esyalariYukle()
        } catch (e: Exception) {
            Log.e("BavulDetayActivity", "onCreate hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun bavulSil() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Bavulu Sil")
                .setMessage("Bu bavulu ve içindeki tüm eşyaları silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { dialog, _ ->
                    dialog.dismiss()
                    val userId = auth.currentUser?.uid ?: run {
                        Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    // Önce aktiviteyi kapat
                    setResult(RESULT_OK)
                    finish()
                    
                    // Sonra silme işlemini gerçekleştir
                    db.collection("users").document(userId)
                        .collection("bavullar").document(bavulId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Bavul başarıyla silindi", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("BavulDetayActivity", "Bavul silme hatası: ${e.message}")
                            Toast.makeText(this, "Bavul silinirken hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Hayır", null)
                .show()
        } catch (e: Exception) {
            Log.e("BavulDetayActivity", "Bavul silme dialog hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun esyaSil(esya: Esya) {
        try {
            AlertDialog.Builder(this)
                .setTitle("Eşyayı Sil")
                .setMessage("${esya.ad} eşyasını silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        db.collection("users").document(userId)
                            .collection("bavullar").document(bavulId)
                            .collection("esyalar").document(esya.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Eşya başarıyla silindi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("BavulDetayActivity", "Eşya silme hatası: ${e.message}")
                                Toast.makeText(this, "Eşya silinirken hata oluştu", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Hayır", null)
                .show()
        } catch (e: Exception) {
            Log.e("BavulDetayActivity", "Eşya silme dialog hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bavulBilgileriniYukle() {
        try {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            db.collection("users").document(userId)
                .collection("bavullar").document(bavulId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        bavul = document.toObject(Bavul::class.java)
                        bavul?.let {
                            it.id = document.id
                            title = it.ad
                        }
                    } else {
                        Toast.makeText(this, "Bavul bulunamadı", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BavulDetayActivity", "Bavul bilgileri yükleme hatası: ${e.message}")
                    Toast.makeText(this, "Bavul bilgileri yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                    finish()
                }
        } catch (e: Exception) {
            Log.e("BavulDetayActivity", "Bavul bilgileri yükleme başlatma hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun esyalariYukle() {
        try {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            db.collection("users").document(userId)
                .collection("bavullar").document(bavulId)
                .collection("esyalar")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("BavulDetayActivity", "Eşyaları yükleme hatası: ${e.message}")
                        Toast.makeText(this, "Eşyalar yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    try {
                        esyalar.clear()
                        snapshot?.documents?.forEach { doc ->
                            val esya = doc.toObject(Esya::class.java)
                            esya?.let {
                                it.id = doc.id
                                esyalar.add(it)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        Log.e("BavulDetayActivity", "Eşya listesi güncelleme hatası: ${e.message}")
                        Toast.makeText(this, "Eşya listesi güncellenirken hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("BavulDetayActivity", "Eşyaları yükleme başlatma hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agirlikHesapla() {
        try {
            val kategoriPozisyon = spinnerKategori.selectedItemPosition
            val altKategoriPozisyon = spinnerAltKategori.selectedItemPosition
            val adet = editTextAdet.text.toString().toIntOrNull() ?: 1

            val secilenKategori = EsyaKategorileri.kategoriler[kategoriPozisyon]
            val secilenAltKategori = secilenKategori.altKategoriler[altKategoriPozisyon]
            val toplamAgirlik = secilenAltKategori.varsayilanAgirlik.toDouble() * adet

            // Her zaman kg olarak göster
            val kg = toplamAgirlik / 1000.0
            textViewToplamAgirlik.text = "Toplam Ağırlık: %.2f kg".format(kg)
        } catch (e: Exception) {
            Log.e("BavulDetayActivity", "Ağırlık hesaplama hatası: ${e.message}")
            textViewToplamAgirlik.text = "Toplam Ağırlık: 0.00 kg"
        }
    }

    private fun yeniEsyaEkle() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_yeni_esya, null)
            spinnerKategori = dialogView.findViewById(R.id.spinnerKategori)
            spinnerAltKategori = dialogView.findViewById(R.id.spinnerAltKategori)
            editTextAdet = dialogView.findViewById(R.id.editTextAdet)
            textViewToplamAgirlik = dialogView.findViewById(R.id.textViewToplamAgirlik)

            // Kategori spinner'ını doldur
            val kategoriAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                EsyaKategorileri.kategoriler.map { it.ad }
            )
            kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerKategori.adapter = kategoriAdapter

            // Kategori seçildiğinde alt kategorileri güncelle
            spinnerKategori.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    try {
                        val secilenKategori = EsyaKategorileri.kategoriler[position]
                        val altKategoriAdapter = ArrayAdapter(
                            this@BavulDetayActivity,
                            android.R.layout.simple_spinner_item,
                            secilenKategori.altKategoriler.map { it.ad }
                        )
                        altKategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerAltKategori.adapter = altKategoriAdapter
                        agirlikHesapla()
                    } catch (e: Exception) {
                        Log.e("BavulDetayActivity", "Alt kategori güncelleme hatası: ${e.message}")
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

            // Alt kategori seçildiğinde ağırlığı güncelle
            spinnerAltKategori.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    agirlikHesapla()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

            // Adet değiştiğinde ağırlığı güncelle
            editTextAdet.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    agirlikHesapla()
                }
            })

            AlertDialog.Builder(this)
                .setTitle("Yeni Eşya")
                .setView(dialogView)
                .setPositiveButton("Ekle") { _, _ ->
                    try {
                        val kategoriPozisyon = spinnerKategori.selectedItemPosition
                        val altKategoriPozisyon = spinnerAltKategori.selectedItemPosition
                        val adet = editTextAdet.text.toString().toIntOrNull() ?: 1

                        val secilenKategori = EsyaKategorileri.kategoriler[kategoriPozisyon]
                        val secilenAltKategori = secilenKategori.altKategoriler[altKategoriPozisyon]
                        val toplamAgirlik = secilenAltKategori.varsayilanAgirlik.toDouble() * adet

                        val userId = auth.currentUser?.uid ?: run {
                            Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        val yeniEsya = Esya(
                            ad = "${secilenAltKategori.ad} (${adet} adet)",
                            agirlik = toplamAgirlik,
                            kategori = secilenKategori.ad
                        )
                        
                        db.collection("users").document(userId)
                            .collection("bavullar").document(bavulId)
                            .collection("esyalar")
                            .add(yeniEsya)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Eşya başarıyla eklendi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("BavulDetayActivity", "Eşya ekleme hatası: ${e.message}")
                                Toast.makeText(this, "Eşya eklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: Exception) {
                        Log.e("BavulDetayActivity", "Eşya ekleme işlemi hatası: ${e.message}")
                        Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        } catch (e: Exception) {
            Log.e("BavulDetayActivity", "Yeni eşya dialog hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }
} 