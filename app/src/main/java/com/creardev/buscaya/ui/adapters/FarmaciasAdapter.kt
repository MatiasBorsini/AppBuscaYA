package com.creardev.buscaya.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.creardev.buscaya.R
import com.creardev.buscaya.data.model.Farmacia

/**
 * Adaptador de RecyclerView para mostrar la lista de farmacias.
 *
 * Utiliza [ListAdapter] y [DiffUtil] para calcular de forma eficiente las diferencias
 * entre listas y actualizar únicamente las filas modificadas en el RecyclerView.
 */
class FarmaciasAdapter(
    private val onFarmaciaClick: (Farmacia) -> Unit
) : ListAdapter<Farmacia, FarmaciasAdapter.FarmaciaViewHolder>(FarmaciaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmaciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_farmacia, parent, false)
        return FarmaciaViewHolder(view, onFarmaciaClick)
    }

    override fun onBindViewHolder(holder: FarmaciaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que mantiene las referencias de las vistas de cada elemento.
     */
    class FarmaciaViewHolder(
        itemView: View,
        private val onClick: (Farmacia) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val imgFarmacia: ImageView = itemView.findViewById(R.id.imgFarmacia)
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvDiaSemana: TextView = itemView.findViewById(R.id.tvDiaSemana)

        fun bind(farmacia: Farmacia) {
            tvNombre.text = farmacia.nombre
            tvDireccion.text = farmacia.direccion
            tvDiaSemana.text = farmacia.diaSemana.take(3).uppercase()

            if (farmacia.imagenSegura.isNotBlank()) {
                imgFarmacia.load(farmacia.imagenSegura) {
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                }
            } else {
                imgFarmacia.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener {
                onClick(farmacia)
            }
        }
    }

    /**
     * DiffCallback para comparar elementos y evitar redibujar toda la lista en cada actualización.
     */
    private class FarmaciaDiffCallback : DiffUtil.ItemCallback<Farmacia>() {
        override fun areItemsTheSame(oldItem: Farmacia, newItem: Farmacia): Boolean {
            return oldItem.nombre == newItem.nombre && oldItem.direccion == newItem.direccion
        }

        override fun areContentsTheSame(oldItem: Farmacia, newItem: Farmacia): Boolean {
            return oldItem == newItem
        }
    }
}
