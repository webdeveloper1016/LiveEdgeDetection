package info.hannes.liveedgedetection.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.shapes.Shape
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * Draws an array of shapes on a canvas
 */
class ScanCanvasView : View {
    private val shapes = ArrayList<ScanShape>()

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    inner class ScanShape(val shape: Shape, private val mPaint: Paint, private val mBorder: Paint?) {
        fun draw(canvas: Canvas?) {
            shape.draw(canvas, mPaint)
            if (mBorder != null) {
                shape.draw(canvas, mBorder)
            }
        }

        init {
            mBorder!!.style = Paint.Style.STROKE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        for (shape in shapes) {
            shape.shape.resize(contentWidth.toFloat(), contentHeight.toFloat())
            shape.draw(canvas)
        }
    }

    fun addShape(shape: Shape, paint: Paint, border: Paint?) {
        val scanShape = ScanShape(shape, paint, border)
        shapes.add(scanShape)
    }

    fun clear() {
        shapes.clear()
    }
}