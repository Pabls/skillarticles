package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.isEmpty
import androidx.core.view.ViewCompat
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
    private var layoutManager: LayoutManager = LayoutManager()

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
        var index = 0
        content.forEach { child ->
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
                    )
                    addView(iv)
                    layoutManager.attachToParent(iv, index)
                    index++
                }
                is MarkdownElement.Scroll -> {
                    val sv = MarkdownCodeView(
                        context,
                        textSize,
                        child.blockCode.text
                    )
                    addView(sv)
                    layoutManager.attachToParent(sv, index)
                    index++
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

    override fun onSaveInstanceState(): Parcelable? {
        val state = SavedState(super.onSaveInstanceState())
        state.layout = layoutManager
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) layoutManager = state.layout
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        children.filter { it !is MarkdownTextView }.forEach {
            it.saveHierarchyState(layoutManager.container)
        }
        dispatchFreezeSelfOnly(container)
    }

    private class LayoutManager() : Parcelable {
        var ids: MutableList<Int> = mutableListOf()
        var container: SparseArray<Parcelable> = SparseArray()

        constructor(parcel: Parcel) : this() {
            ids = parcel.readArrayList(Int::class.java.classLoader) as ArrayList<Int>
            container =
                parcel.readSparseArray<Parcelable>(this::class.java.classLoader) as SparseArray<Parcelable>
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeIntArray(ids.toIntArray())
            parcel.writeSparseArray(container)
        }

        override fun describeContents(): Int = 0

        fun attachToParent(view: View, index: Int) {
            if (container.isEmpty()) {
                view.id = ViewCompat.generateViewId()
                ids.add(view.id)
            } else {
                view.id = ids[index]
                view.restoreHierarchyState(container)
            }
        }

        companion object CREATOR : Parcelable.Creator<LayoutManager> {
            override fun createFromParcel(parcel: Parcel): LayoutManager = LayoutManager(parcel)
            override fun newArray(size: Int): Array<LayoutManager?> = arrayOfNulls(size)
        }

    }

    private class SavedState : BaseSavedState, Parcelable {
        lateinit var layout: LayoutManager

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            layout = src.readParcelable(LayoutManager::class.java.classLoader)!!
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeParcelable(layout, flags)
        }

        override fun describeContents(): Int = 0

        companion object CREATER: Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int) : Array<SavedState?> = arrayOfNulls(size)
        }
    }

}