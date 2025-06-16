package com.example.handicraft.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.handicraft.R
import com.example.handicraft.databinding.ItemOnboardingBinding

/**
 * Adapter for displaying onboarding screens.
 */
class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    private val items = listOf(
        OnboardingItem(R.drawable.onboarding1, R.string.onboarding_title1, R.string.onboarding_desc1),
        OnboardingItem(R.drawable.onboarding2, R.string.onboarding_title2, R.string.onboarding_desc2),
        OnboardingItem(R.drawable.onboarding3, R.string.onboarding_title3, R.string.onboarding_desc3)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder for onboarding items.
     */
    class OnboardingViewHolder(private val binding: ItemOnboardingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnboardingItem) {
            binding.image.setImageResource(item.imageRes)
            binding.title.setText(item.titleRes)
            binding.description.setText(item.descriptionRes)
        }
    }

    /**
     * Model representing onboarding content.
     */
    data class OnboardingItem(
        val imageRes: Int,
        val titleRes: Int,
        val descriptionRes: Int
    )
}
