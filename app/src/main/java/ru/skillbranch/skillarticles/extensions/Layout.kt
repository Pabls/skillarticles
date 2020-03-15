package ru.skillbranch.skillarticles.extensions

import android.text.Layout

fun Layout.getLineHeight(line: Int): Int {
    return getLineTop(line + 1) - getLineTop(line)
}

fun Layout.getLineBottomWithoutPadding(line: Int): Int{
    return if(line == lineCount.dec()) getLineBottomWithoutSpacingline(line) - bottomPadding
    else getLineBottomWithoutSpacingline(line)
}



fun Layout.getLineTopWithoutPadding(line: Int): Int {
    return if(line == 0) getLineTop(line) - topPadding
    else getLineTop(line)
}


fun Layout.getLineBottomWithoutSpacingline(line: Int): Int {
    val lineBottom = getLineBottom(line)
    val isLastLine = line == lineCount.dec()
    val hasLineSpacing = spacingAdd != 0f
    return if(!hasLineSpacing || isLastLine || (getLineEnd(line) != getLineVisibleEnd(line))) lineBottom + spacingAdd.toInt()
    else lineBottom - spacingAdd.toInt()
}
