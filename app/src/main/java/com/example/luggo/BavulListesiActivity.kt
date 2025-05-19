package com.example.luggo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luggo.adapters.BavulAdapter
import com.example.luggo.models.Bavul
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BavulListesiActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabYeniBavul: FloatingActionButton
    private val bavullar = mutableListOf<Bavul>()
    private lateinit var adapter: BavulAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var dialog: AlertDialog? = null

    companion object {
        private const val BAVUL_DETAY_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_bavul_listesi)

            // Toolbar'ı ayarla
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false) // Toolbar başlığını gizle

            // Kullanıcı kontrolü
            if (auth.currentUser == null) {
                Log.e("BavulListesiActivity", "Kullanıcı oturumu bulunamadı")
                Toast.makeText(this, "Oturum hatası, lütfen tekrar giriş yapın", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            }

            recyclerView = findViewById(R.id.recyclerViewBavullar)
            fabYeniBavul = findViewById(R.id.fabYeniBavul)

            adapter = BavulAdapter(bavullar) { bavul ->
                bavulDetayinaGit(bavul)
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

            fabYeniBavul.setOnClickListener {
                yeniBavulEkle()
            }

            bavullariYukle()
        } catch (e: Exception) {
            Log.e("BavulListesiActivity", "onCreate hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bavul_listesi, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                cikisYap()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cikisYap() {
        try {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e("BavulListesiActivity", "Çıkış yapma hatası: ${e.message}")
            Toast.makeText(this, "Çıkış yapılırken hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bavulDetayinaGit(bavul: Bavul) {
        try {
            if (bavul.id.isEmpty()) {
                Log.e("BavulListesiActivity", "Bavul ID boş")
                Toast.makeText(this, "Bavul ID bulunamadı", Toast.LENGTH_SHORT).show()
                return
            }
            
            Log.d("BavulListesiActivity", "Bavul detayına gidiliyor. Bavul ID: ${bavul.id}")
            val intent = Intent(this, BavulDetayActivity::class.java).apply {
                putExtra("bavul_id", bavul.id)
            }
            startActivityForResult(intent, BAVUL_DETAY_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e("BavulListesiActivity", "Bavul detayına gitme hatası: ${e.message}")
            Toast.makeText(this, "Bavul detayına gidilirken hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bavullariYukle() {
        try {
            val userId = auth.currentUser?.uid ?: run {
                Log.e("BavulListesiActivity", "Kullanıcı ID bulunamadı")
                Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            Log.d("BavulListesiActivity", "Bavullar yükleniyor. Kullanıcı ID: $userId")
            db.collection("users").document(userId)
                .collection("bavullar")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("BavulListesiActivity", "Bavulları yükleme hatası: ${e.message}")
                        Toast.makeText(this, "Bavullar yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    try {
                        bavullar.clear()
                        snapshot?.documents?.forEach { doc ->
                            val bavul = doc.toObject(Bavul::class.java)
                            bavul?.let {
                                it.id = doc.id
                                bavullar.add(it)
                                Log.d("BavulListesiActivity", "Bavul yüklendi: ${it.ad} (ID: ${it.id})")
                            }
                        }
                        adapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        Log.e("BavulListesiActivity", "Bavul listesi güncelleme hatası: ${e.message}")
                        Toast.makeText(this, "Bavul listesi güncellenirken hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("BavulListesiActivity", "Bavulları yükleme başlatma hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun yeniBavulEkle() {
        try {
            Log.d("BavulListesiActivity", "Yeni bavul dialog'u gösteriliyor")
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_yeni_bavul, null)
            val editTextBavulAdi = dialogView.findViewById<EditText>(R.id.editTextBavulAdi)

            dialog = AlertDialog.Builder(this)
                .setTitle("Yeni Bavul")
                .setView(dialogView)
                .setPositiveButton("Ekle") { _, _ ->
                    try {
                        val bavulAdi = editTextBavulAdi.text.toString().trim()
                        if (bavulAdi.isEmpty()) {
                            Toast.makeText(this, "Lütfen bavul adını girin", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val userId = auth.currentUser?.uid ?: run {
                            Log.e("BavulListesiActivity", "Kullanıcı ID bulunamadı")
                            Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        Log.d("BavulListesiActivity", "Yeni bavul ekleniyor. Ad: $bavulAdi")
                        val yeniBavul = Bavul(
                            ad = bavulAdi,
                            userId = userId
                        )
                        
                        db.collection("users").document(userId)
                            .collection("bavullar")
                            .add(yeniBavul)
                            .addOnSuccessListener { documentReference ->
                                Log.d("BavulListesiActivity", "Bavul başarıyla eklendi. ID: ${documentReference.id}")
                                Toast.makeText(this, "Bavul başarıyla eklendi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("BavulListesiActivity", "Bavul ekleme hatası: ${e.message}")
                                Toast.makeText(this, "Bavul eklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: Exception) {
                        Log.e("BavulListesiActivity", "Bavul ekleme işlemi hatası: ${e.message}")
                        Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .create()

            dialog?.show()
        } catch (e: Exception) {
            Log.e("BavulListesiActivity", "Yeni bavul dialog hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        dialog = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BAVUL_DETAY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Bavul silindi veya güncellendi, listeyi yenile
                bavullariYukle()
            }
        }
    }
} 