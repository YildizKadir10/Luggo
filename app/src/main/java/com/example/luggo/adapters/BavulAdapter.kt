package com.example.luggo.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luggo.R
import com.example.luggo.models.Bavul
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BavulAdapter(
    private val bavullar: List<Bavul>,
    private val onItemClick: (Bavul) -> Unit,
    private val onEditClick: (Bavul) -> Unit,
    private val onDeleteClick: (Bavul) -> Unit
) : RecyclerView.Adapter<BavulAdapter.BavulViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class BavulViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewBavul: ImageView = view.findViewById(R.id.imageViewBavul)
        val textViewBavulAdi: TextView = view.findViewById(R.id.textViewBavulAdi)
        val textViewToplamAgirlik: TextView = view.findViewById(R.id.textViewToplamAgirlik)
        val textViewAgirlikSiniri: TextView = view.findViewById(R.id.textViewAgirlikSiniri)
        val buttonEditBavul: ImageButton = view.findViewById(R.id.buttonEditBavul)
        val buttonDeleteBavul: ImageButton = view.findViewById(R.id.buttonDeleteBavul)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BavulViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bavul, parent, false)
        return BavulViewHolder(view)
    }

    override fun onBindViewHolder(holder: BavulViewHolder, position: Int) {
        val bavul = bavullar[position]
        holder.textViewBavulAdi.text = bavul.ad
        
        // Ağırlık sınırını göster
        holder.textViewAgirlikSiniri.text = "Ağırlık Sınırı: %.1f kg".format(bavul.agirlikSiniri)
        
        // Bavul tipine göre ikonu ayarla
        val iconResId = when (bavul.tip) {
            "man" -> R.drawable.ic_luggage_man
            "woman" -> R.drawable.ic_luggage_woman
            "boy" -> R.drawable.ic_luggage_boy
            "girl" -> R.drawable.ic_luggage_girl
            else -> R.drawable.ic_luggage
        }
        holder.imageViewBavul.setImageResource(iconResId)
        
        // Toplam ağırlığı gerçek zamanlı dinle
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("bavullar").document(bavul.id)
            .collection("esyalar")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("BavulAdapter", "Eşyaları dinleme hatası: ${e.message}")
                    holder.textViewToplamAgirlik.text = "Toplam Ağırlık: 0.00 kg"
                    return@addSnapshotListener
                }

                try {
                    var toplamAgirlik = 0.0
                    snapshot?.documents?.forEach { document ->
                        val agirlik = document.getDouble("agirlik") ?: 0.0
                        toplamAgirlik += agirlik
                    }
                    // Gram'ı kg'a çevir ve göster
                    val kg = toplamAgirlik / 1000.0
                    holder.textViewToplamAgirlik.text = "Toplam Ağırlık: %.2f kg".format(kg)
                } catch (e: Exception) {
                    Log.e("BavulAdapter", "Ağırlık hesaplama hatası: ${e.message}")
                    holder.textViewToplamAgirlik.text = "Toplam Ağırlık: 0.00 kg"
                }
            }
        
        // Tıklama olaylarını ayarla
        holder.itemView.setOnClickListener {
            onItemClick(bavul)
        }

        holder.buttonEditBavul.setOnClickListener {
            onEditClick(bavul)
        }

        holder.buttonDeleteBavul.setOnClickListener {
            onDeleteClick(bavul)
        }
    }

    override fun getItemCount() = bavullar.size
} 