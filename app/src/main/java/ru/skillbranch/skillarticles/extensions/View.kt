package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup.MarginLayoutParams


fun View.setMarginOptionally(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    if (this.layoutParams is MarginLayoutParams) {
        val p = this.layoutParams as MarginLayoutParams
        p.setMargins(left, top, right, bottom)
        this.requestLayout()
    }
}