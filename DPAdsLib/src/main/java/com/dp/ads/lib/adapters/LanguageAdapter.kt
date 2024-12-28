package com.dp.ads.lib.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dp.ads.lib.R
import com.dp.ads.lib.data.Language
import com.dp.ads.lib.utils.MyLocaleHelper
import com.dp.ads.lib.utils.PrefHelper

class LanguageAdapter(
    private var ctx: Context,
    private val languages: List<Language>,
    private val selectedDrawable: Drawable,
    private val unSelectedDrawable: Drawable,
    private val selectedRadio: Drawable,
    private val unSelectedRadio: Drawable,
    private val onItemClickListener: (position: Int) -> Unit)
    : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedItem = RecyclerView.NO_POSITION
    private lateinit var prefHelper: PrefHelper

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.language_item, parent, false)
        prefHelper = PrefHelper(ctx)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.flagImageView.setImageResource(language.imageId)
        holder.languageTextView.text = language.name

        val savedPosition = prefHelper.getStringDefault("languagePosition", "-1")?.toInt() ?: -1

        if (ctx.javaClass.name.endsWith("LanguageScreenOne")) {
            holder.selectedBackground.setBackgroundDrawable(unSelectedDrawable)
            holder.selectionStatus.setBackgroundDrawable(unSelectedRadio)
        } else {
            if (savedPosition == position || selectedItem == position) {
                holder.selectedBackground.setBackgroundDrawable(selectedDrawable)
                holder.selectionStatus.setBackgroundDrawable(selectedRadio)
            } else {
                holder.selectedBackground.setBackgroundDrawable(unSelectedDrawable)
                holder.selectionStatus.setBackgroundDrawable(unSelectedRadio)
            }
        }

        holder.itemView.setOnClickListener {
            val previousSelectedItem = prefHelper.getStringDefault("languagePosition", "-1")?.toInt() ?: -1
            prefHelper.putString("languagePosition", position.toString())
            prefHelper.putString("languageCode", language.code)
            MyLocaleHelper.setLocale(ctx, language.code)
            notifyItemChanged(previousSelectedItem)
            prefHelper.getStringDefault("languagePosition", "-1")?.toInt()?.let { it1 -> notifyItemChanged(it1) }
            onItemClickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedBackground: ConstraintLayout = itemView.findViewById(R.id.selectedBackground)
        val flagImageView: ImageView = itemView.findViewById(R.id.ivCountryFlagLanguage)
        val selectionStatus: ImageView = itemView.findViewById(R.id.ivUnSelected)
        val languageTextView: TextView = itemView.findViewById(R.id.tvCountryNameLanguage)
    }
}