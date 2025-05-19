package com.example.luggo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luggo.R
import com.example.luggo.models.Esya

class EsyaAdapter(
    private val esyalar: List<Esya>,
    private val onDeleteClick: (Esya) -> Unit
) : RecyclerView.Adapter<EsyaAdapter.EsyaViewHolder>() {

    class EsyaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewEsyaAdi: TextView = view.findViewById(R.id.textViewEsyaAdi)
        val textViewEsyaAgirlik: TextView = view.findViewById(R.id.textViewEsyaAgirlik)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsyaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_esya, parent, false)
        return EsyaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EsyaViewHolder, position: Int) {
        val esya = esyalar[position]
        holder.textViewEsyaAdi.text = esya.ad
        holder.textViewEsyaAgirlik.text = "%.2f kg".format(esya.agirlik / 1000.0)
        
        holder.buttonDelete.setOnClickListener {
            onDeleteClick(esya)
        }
    }

    override fun getItemCount() = esyalar.size
} 