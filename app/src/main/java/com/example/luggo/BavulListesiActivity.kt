package com.example.luggo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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

            adapter = BavulAdapter(
                bavullar = bavullar,
                onItemClick = { bavul -> bavulDetayinaGit(bavul) },
                onEditClick = { bavul -> bavulDuzenle(bavul) },
                onDeleteClick = { bavul -> bavulSil(bavul) }
            )
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
            val layoutManLuggage = dialogView.findViewById<View>(R.id.layoutManLuggage)
            val layoutWomanLuggage = dialogView.findViewById<View>(R.id.layoutWomanLuggage)
            val layoutBoyLuggage = dialogView.findViewById<View>(R.id.layoutBoyLuggage)
            val layoutGirlLuggage = dialogView.findViewById<View>(R.id.layoutGirlLuggage)

            var secilenTip = "man" // Varsayılan tip

            // Tıklama olaylarını ayarla
            layoutManLuggage.setOnClickListener {
                secilenTip = "man"
                layoutManLuggage.setBackgroundResource(R.drawable.selected_background)
                layoutWomanLuggage.setBackgroundResource(0)
                layoutBoyLuggage.setBackgroundResource(0)
                layoutGirlLuggage.setBackgroundResource(0)
            }

            layoutWomanLuggage.setOnClickListener {
                secilenTip = "woman"
                layoutManLuggage.setBackgroundResource(0)
                layoutWomanLuggage.setBackgroundResource(R.drawable.selected_background)
                layoutBoyLuggage.setBackgroundResource(0)
                layoutGirlLuggage.setBackgroundResource(0)
            }

            layoutBoyLuggage.setOnClickListener {
                secilenTip = "boy"
                layoutManLuggage.setBackgroundResource(0)
                layoutWomanLuggage.setBackgroundResource(0)
                layoutBoyLuggage.setBackgroundResource(R.drawable.selected_background)
                layoutGirlLuggage.setBackgroundResource(0)
            }

            layoutGirlLuggage.setOnClickListener {
                secilenTip = "girl"
                layoutManLuggage.setBackgroundResource(0)
                layoutWomanLuggage.setBackgroundResource(0)
                layoutBoyLuggage.setBackgroundResource(0)
                layoutGirlLuggage.setBackgroundResource(R.drawable.selected_background)
            }

            // İlk seçeneği seçili olarak işaretle
            layoutManLuggage.setBackgroundResource(R.drawable.selected_background)

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

                        Log.d("BavulListesiActivity", "Yeni bavul ekleniyor. Ad: $bavulAdi, Tip: $secilenTip")
                        val yeniBavul = Bavul(
                            ad = bavulAdi,
                            tip = secilenTip,
                            agirlikSiniri = if (secilenTip == "man" || secilenTip == "woman") 25.0 else 10.0
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

    private fun bavulDuzenle(bavul: Bavul) {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_yeni_bavul, null)
            val editTextBavulAdi = dialogView.findViewById<EditText>(R.id.editTextBavulAdi)
            val layoutManLuggage = dialogView.findViewById<View>(R.id.layoutManLuggage)
            val layoutWomanLuggage = dialogView.findViewById<View>(R.id.layoutWomanLuggage)
            val layoutBoyLuggage = dialogView.findViewById<View>(R.id.layoutBoyLuggage)
            val layoutGirlLuggage = dialogView.findViewById<View>(R.id.layoutGirlLuggage)

            // Mevcut değerleri ayarla
            editTextBavulAdi.setText(bavul.ad)
            var secilenTip = bavul.tip

            // Mevcut tipi seçili olarak işaretle
            when (bavul.tip) {
                "woman" -> layoutWomanLuggage.setBackgroundResource(R.drawable.selected_background)
                "boy" -> layoutBoyLuggage.setBackgroundResource(R.drawable.selected_background)
                "girl" -> layoutGirlLuggage.setBackgroundResource(R.drawable.selected_background)
                else -> layoutManLuggage.setBackgroundResource(R.drawable.selected_background)
            }

            // Tıklama olaylarını ayarla
            layoutManLuggage.setOnClickListener {
                secilenTip = "man"
                layoutManLuggage.setBackgroundResource(R.drawable.selected_background)
                layoutWomanLuggage.setBackgroundResource(0)
                layoutBoyLuggage.setBackgroundResource(0)
                layoutGirlLuggage.setBackgroundResource(0)
            }

            layoutWomanLuggage.setOnClickListener {
                secilenTip = "woman"
                layoutManLuggage.setBackgroundResource(0)
                layoutWomanLuggage.setBackgroundResource(R.drawable.selected_background)
                layoutBoyLuggage.setBackgroundResource(0)
                layoutGirlLuggage.setBackgroundResource(0)
            }

            layoutBoyLuggage.setOnClickListener {
                secilenTip = "boy"
                layoutManLuggage.setBackgroundResource(0)
                layoutWomanLuggage.setBackgroundResource(0)
                layoutBoyLuggage.setBackgroundResource(R.drawable.selected_background)
                layoutGirlLuggage.setBackgroundResource(0)
            }

            layoutGirlLuggage.setOnClickListener {
                secilenTip = "girl"
                layoutManLuggage.setBackgroundResource(0)
                layoutWomanLuggage.setBackgroundResource(0)
                layoutBoyLuggage.setBackgroundResource(0)
                layoutGirlLuggage.setBackgroundResource(R.drawable.selected_background)
            }

            dialog = AlertDialog.Builder(this)
                .setTitle("Bavulu Düzenle")
                .setView(dialogView)
                .setPositiveButton("Kaydet") { _, _ ->
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

                        Log.d("BavulListesiActivity", "Bavul güncelleniyor. ID: ${bavul.id}, Ad: $bavulAdi, Tip: $secilenTip")
                        val guncelBavul = Bavul(
                            id = bavul.id,
                            ad = bavulAdi,
                            userId = userId,
                            tip = secilenTip,
                            agirlikSiniri = if (secilenTip == "man" || secilenTip == "woman") 25.0 else 10.0
                        )
                        
                        db.collection("users").document(userId)
                            .collection("bavullar").document(bavul.id)
                            .set(guncelBavul)
                            .addOnSuccessListener {
                                Log.d("BavulListesiActivity", "Bavul başarıyla güncellendi")
                                Toast.makeText(this, "Bavul başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("BavulListesiActivity", "Bavul güncelleme hatası: ${e.message}")
                                Toast.makeText(this, "Bavul güncellenirken hata oluştu", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: Exception) {
                        Log.e("BavulListesiActivity", "Bavul güncelleme işlemi hatası: ${e.message}")
                        Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .create()

            dialog?.show()
        } catch (e: Exception) {
            Log.e("BavulListesiActivity", "Bavul düzenleme dialog hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bavulSil(bavul: Bavul) {
        try {
            AlertDialog.Builder(this)
                .setTitle("Bavulu Sil")
                .setMessage("Bu bavulu ve içindeki tüm eşyaları silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    val userId = auth.currentUser?.uid ?: run {
                        Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    db.collection("users").document(userId)
                        .collection("bavullar").document(bavul.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Bavul başarıyla silindi", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("BavulListesiActivity", "Bavul silme hatası: ${e.message}")
                            Toast.makeText(this, "Bavul silinirken hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Hayır", null)
                .show()
        } catch (e: Exception) {
            Log.e("BavulListesiActivity", "Bavul silme dialog hatası: ${e.message}")
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