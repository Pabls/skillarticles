package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.groupByBounds
import ru.skillbranch.skillarticles.extensions.setPaddingOptionally
import kotlin.properties.Delegates

//import ru.skillbranch.skillarticles.data.repositories.MarkdownElement

class MarkdownContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private lateinit var elements: List<MarkdownElement>

    //for restore
    private var ids = arrayListOf<Int>()

    var textSize by Delegates.observable(14f) { _, old, value ->
        if (value == old) return@observable
        children.forEach {
            it as IMarkdownView
            it.fontSize = value
        }
    }
    //    val textSize = 14f
    var isLoading: Boolean = true
//    val padding //8dp

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = paddingTop
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        children.forEach {
            measureChild(it, widthMeasureSpec, heightMeasureSpec)
            usedHeight += it.measuredHeight
        }
        usedHeight += paddingBottom
        setMeasuredDimension(width, usedHeight)

    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = right - left - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth
        children.forEach {
            if (it is MarkdownTextView) {
                it.layout(
                    left - paddingLeft / 2,
                    usedHeight,
                    right - paddingRight / 2,
                    usedHeight + it.measuredHeight
                )
            } else {
                it.layout(
                    left,
                    usedHeight,
                    right,
                    usedHeight + it.measuredHeight
                )

            }
            usedHeight += it.measuredHeight
        }
    }

    fun setContent(content: List<MarkdownElement>) {
        elements = content
        content.forEachIndexed { index, child ->
            when (child) {
                is MarkdownElement.Text -> {
                    val tv = MarkdownTextView(context, textSize).apply {
                        setPaddingOptionally(
                            left = context.dpToIntPx(8),
                            right = context.dpToIntPx(8)
                        )
                        setLineSpacing(fontSize * 0.5f, 1f)
                    }
                    MarkdownBuilder(context)
                        .markdownToSpan(child)
                        .run {
                            tv.setText(this, TextView.BufferType.SPANNABLE)
                        }
                    addView(tv)
                }
                is MarkdownElement.Image -> {
                    val iv = MarkdownImageView(
                        context,
                        textSize,
                        child.image.url,
                        child.image.text,
                        child.image.alt
                    ).apply {
                        id = index
                    }
                    addView(iv)
                }
                is MarkdownElement.Scroll -> {
                    val sv = MarkdownCodeView(
                        context,
                        textSize,
                        child.blockCode.text
                    ).apply {
                        id = index
                    }
                    addView(sv)
                }

            }
        }
    }

    fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        children.forEach { view ->
            view as IMarkdownView
            view.clearSearchResult()
        }
        if (searchResult.isEmpty()) return
        val bouds = elements.map { it.bounds }
        val result = searchResult.groupByBounds(bouds)
        children.forEachIndexed { index, view ->
            (view as IMarkdownView).renderSearchResult(result[index], elements[index].offset)
        }
    }

    fun renderSearchPosition(
        searchPosition: Pair<Int, Int>?
    ) {
        searchPosition ?: return
        val bounds = elements.map { it.bounds }
        val index = bounds.indexOfFirst { (start, end) ->
            val boundRange = start..end
            val (startPosition, endPosition) = searchPosition
            startPosition in boundRange && endPosition in boundRange
        }

        if (index == -1) return
        val view = getChildAt(index)
        view as IMarkdownView
        view.renderSearchPosition(searchPosition, elements[index].offset)
    }

    fun clearSearchResult() {
        children.forEach { (it as IMarkdownView).clearSearchResult() }
    }

    fun setCopyListener(listener: (String) -> Unit) {
        children.filterIsInstance<MarkdownCodeView>().forEach {
            it.copyListener = listener
        }
    }

}