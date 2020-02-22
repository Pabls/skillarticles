package ru.skillbranch.skillarticles.extensions

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView

fun NestedScrollView.setMarginOptionally(
    left: Int = -1,
    top: Int = -1,
    right: Int = -1,
    bottom: Int = -1
) {
    if (bottom >= 0) (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = bottom
    if (left >= 0) (layoutParams as CoordinatorLayout.LayoutParams).leftMargin = left
    if (top >= 0) (layoutParams as CoordinatorLayout.LayoutParams).topMargin = top
    if (right >= 0) (layoutParams as CoordinatorLayout.LayoutParams).rightMargin = right

}
