package com.example.luggo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luggo.R
import com.example.luggo.models.Bavul
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BavulAdapter(
    private val bavullar: List<Bavul>,
    private val onItemClick: (Bavul) -> Unit
) : RecyclerView.Adapter<BavulAdapter.BavulViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class BavulViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewBavulAdi: TextView = view.findViewById(R.id.textViewBavulAdi)
        val textViewToplamAgirlik: TextView = view.findViewById(R.id.textViewToplamAgirlik)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BavulViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bavul, parent, false)
        return BavulViewHolder(view)
    }

    override fun onBindViewHolder(holder: BavulViewHolder, position: Int) {
        val bavul = bavullar[position]
        holder.textViewBavulAdi.text = bavul.ad
        
        // Toplam ağırlığı hesapla ve dinle
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("bavullar").document(bavul.id)
            .collection("esyalar")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    holder.textViewToplamAgirlik.text = "Toplam Ağırlık: 0.00 kg"
                    return@addSnapshotListener
                }

                var toplamAgirlik = 0.0
                snapshot?.documents?.forEach { document ->
                    val agirlik = document.getDouble("agirlik") ?: 0.0
                    toplamAgirlik += agirlik
                }
                // Gram'ı kg'a çevir ve göster
                val kg = toplamAgirlik / 1000.0
                holder.textViewToplamAgirlik.text = "Toplam Ağırlık: %.2f kg".format(kg)
            }

        holder.itemView.setOnClickListener {
            onItemClick(bavul)
        }
    }

    override fun getItemCount() = bavullar.size
} 