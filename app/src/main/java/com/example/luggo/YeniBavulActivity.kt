package com.example.luggo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.luggo.models.Bavul
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class YeniBavulActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            // Arka plan ekranını kaldır
            setContentView(android.R.layout.activity_list_item)

            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_yeni_bavul, null)
            val editTextBavulAdi = dialogView.findViewById<EditText>(R.id.editTextBavulAdi)

            AlertDialog.Builder(this)
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
                            Log.e("YeniBavulActivity", "Kullanıcı ID bulunamadı")
                            Toast.makeText(this, "Oturum bulunamadı", Toast.LENGTH_SHORT).show()
                            finish()
                            return@setPositiveButton
                        }

                        Log.d("YeniBavulActivity", "Yeni bavul ekleniyor. Ad: $bavulAdi")
                        val yeniBavul = Bavul(ad = bavulAdi)
                        
                        db.collection("users").document(userId)
                            .collection("bavullar")
                            .add(yeniBavul)
                            .addOnSuccessListener { documentReference ->
                                Log.d("YeniBavulActivity", "Bavul başarıyla eklendi. ID: ${documentReference.id}")
                                Toast.makeText(this, "Bavul başarıyla eklendi", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("YeniBavulActivity", "Bavul ekleme hatası: ${e.message}")
                                Toast.makeText(this, "Bavul eklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                            }
                    } catch (e: Exception) {
                        Log.e("YeniBavulActivity", "Bavul ekleme işlemi hatası: ${e.message}")
                        Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal") { _, _ ->
                    finish()
                }
                .setOnCancelListener {
                    finish()
                }
                .show()
        } catch (e: Exception) {
            Log.e("YeniBavulActivity", "onCreate hatası: ${e.message}")
            Toast.makeText(this, "Bir hata oluştu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
} 