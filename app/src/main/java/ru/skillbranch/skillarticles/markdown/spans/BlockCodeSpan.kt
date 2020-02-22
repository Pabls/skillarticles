package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.markdown.Element


class BlockCodeSpan(
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val bgColor: Int,
    @Px
    private val cornerRadius: Float,
    @Px
    private val padding: Float,
    private val type: Element.BlockCode.Type
) : ReplacementSpan() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect = RectF()
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()


    private val startBlockRadius = floatArrayOf(
            cornerRadius, cornerRadius,
            cornerRadius, cornerRadius,
            0f, 0f,
            0f, 0f
        )
    private val endBlockRadius = floatArrayOf(
        0f, 0f,
        0f, 0f,
        cornerRadius, cornerRadius,
        cornerRadius, cornerRadius
    )
    private val middleBlockRadius = floatArrayOf(
        0f, 0f,
        0f, 0f,
        0f, 0f,
        0f, 0f
    )
    private val singleBlockRadius = floatArrayOf(
        cornerRadius, cornerRadius,
        cornerRadius, cornerRadius,
        cornerRadius, cornerRadius,
        cornerRadius, cornerRadius
    )

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (fm != null) {
            when (type) {
                Element.BlockCode.Type.SINGLE -> {
                    fm.ascent = (paint.ascent() - 2 * padding).toInt()
                    fm.descent = (paint.descent() + 2 * padding).toInt()
                }
                Element.BlockCode.Type.START -> {
                    fm.ascent = (paint.ascent() - 2 * padding).toInt()
                    fm.descent = paint.descent().toInt()
                }
                Element.BlockCode.Type.END -> {
                    fm.descent = (paint.descent() + 2 * padding).toInt()
                    fm.ascent = paint.ascent().toInt()
                }
                Element.BlockCode.Type.MIDDLE -> {
                    fm.ascent = paint.ascent().toInt()
                    fm.descent = paint.descent().toInt()
                }

            }
        }
        return 0
    }


    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint.forBackground {
            path.reset()
            when (type) {
                Element.BlockCode.Type.SINGLE -> paint.forBackground {
                    rect.set(0f, top + padding, canvas.width.toFloat(), bottom - padding)
                    path.addRoundRect(rect, singleBlockRadius, Path.Direction.CW)
                }
                Element.BlockCode.Type.START -> paint.forBackground {
                    rect.set(0f, top + padding, canvas.width.toFloat(), bottom.toFloat())
                    path.addRoundRect(rect, startBlockRadius, Path.Direction.CW)
                }
                Element.BlockCode.Type.MIDDLE -> paint.forBackground {
                    rect.set(0f, top.toFloat(), canvas.width.toFloat(), bottom.toFloat())
                    path.addRoundRect(rect, middleBlockRadius, Path.Direction.CW)
                }
                Element.BlockCode.Type.END -> paint.forBackground {
                    rect.set(0f, top.toFloat(), canvas.width.toFloat(), bottom - padding)
                    path.addRoundRect(rect, endBlockRadius, Path.Direction.CW)
                }
            }
            canvas.drawPath(path, paint)


        }

        paint.forText {
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }

    }


    private inline fun Paint.forText(block: () -> Unit) {
        val oldColor = color
        val oldSize = textSize
        val oldStyle = typeface?.style ?: 0
        val oldFont = typeface
        color = textColor
        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)
        textSize *= 0.85f
        block()
        color = oldColor
        textSize = oldSize
        typeface = oldFont

    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style

        color = bgColor
        style = Paint.Style.FILL
        block()
        color = oldColor
        style = oldStyle
    }


    fun RoundedRect(
        rect: RectF,
        r: Float,
        tl: Boolean = true,
        tr: Boolean = true,
        br: Boolean = true,
        bl: Boolean = true
    ): Path {
        var rx = r
        var ry = r
        val path = Path()
        if (rx < 0) rx = 0f
        if (ry < 0) ry = 0f
        val width = rect.right - rect.left
        val height = rect.bottom - rect.top
        if (rx > width / 2) rx = width / 2
        if (ry > height / 2) ry = height / 2
        val widthMinusCorners = width - 2 * rx
        val heightMinusCorners = height - 2 * ry
        path.moveTo(rect.right, rect.top + ry)
        if (tr) path.rQuadTo(0f, -ry, -rx, -ry) //top-right corner
        else {
            path.rLineTo(0f, -ry)
            path.rLineTo(-rx, 0f)
        }
        path.rLineTo(-widthMinusCorners, 0f)
        if (tl) path.rQuadTo(-rx, 0f, -rx, ry) //top-left corner
        else {
            path.rLineTo(-rx, 0f)
            path.rLineTo(0f, ry)
        }
        path.rLineTo(0f, heightMinusCorners)
        if (bl) path.rQuadTo(0f, ry, rx, ry) //bottom-left corner
        else {
            path.rLineTo(0f, ry)
            path.rLineTo(rx, 0f)
        }
        path.rLineTo(widthMinusCorners, 0f)
        if (br) path.rQuadTo(rx, 0f, rx, -ry) //bottom-right corner
        else {
            path.rLineTo(rx, 0f)
            path.rLineTo(0f, -ry)
        }
        path.rLineTo(0f, -heightMinusCorners)
        path.close() //Given close, last line to can be removed.
        return path
    }
}
