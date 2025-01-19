package com.dp.ads.lib.data

import android.graphics.drawable.Drawable

data class WalkThroughItem(
    val heading: String,
    val description: String,
    val headingColor: Int,
    val descriptionColor: Int,
    val nextColor: Int,
    val drawable: Drawable?,
    val drawableBubble: Drawable?
)