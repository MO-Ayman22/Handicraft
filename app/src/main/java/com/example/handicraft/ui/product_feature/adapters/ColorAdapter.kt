package com.example.handicraft.ui.product_feature.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.handicraft.R
import com.example.handicraft.databinding.ItemColorBinding


class ColorAdapter(
    private var colors: List<Int>,
    private val listener: OnColorsClickListener
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color, position)
    }

    override fun getItemCount(): Int = colors.size

    inner class ColorViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(color: Int, position: Int) {
            if (color == Color.TRANSPARENT) {
                binding.viewColor.setBackgroundResource(R.drawable.bg_circle_add_color)
                binding.ivAddColor.visibility = View.VISIBLE
            } else {
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    setStroke(2, Color.BLACK)
                }

                binding.viewColor.background = drawable
                binding.ivAddColor.visibility = View.GONE
            }
            binding.root.setOnClickListener {
               if (color == Color.TRANSPARENT)
                   listener.onAddColorClicked()
                else
                    listener.onColorClicked(binding.root, position)
            }
        }
    }

    fun updateColors(newColors: List<Int>) {
        colors = newColors
        notifyDataSetChanged()
    }
}
interface OnColorsClickListener{
    fun onAddColorClicked()
    fun onColorClicked(view: View, position: Int)

}