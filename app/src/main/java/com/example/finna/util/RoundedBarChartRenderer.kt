package com.example.finna.util

import android.graphics.Canvas
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * Renderer kustom untuk BarChart (MPAndroidChart) agar setiap batang digambar
 * dengan sudut membulat (rounded corners), bukan persegi tajam bawaan library.
 *
 * MPAndroidChart v3.1.0 tidak menyediakan opsi radius sudut secara native,
 * sehingga satu-satunya cara adalah menimpa proses menggambar tiap dataset
 * dan mengganti canvas.drawRect(...) bawaan dengan canvas.drawRoundRect(...).
 *
 * Catatan keamanan: jika versi library berubah dan method drawDataSet() di
 * versi baru berbeda signature-nya, class ini bisa gagal override (compile
 * error), bukan crash diam-diam -- sehingga aman dideteksi saat build.
 */
class RoundedBarChartRenderer(
    chart: BarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val cornerRadiusPx: Float = 14f
) : BarChartRenderer(chart, animator, viewPortHandler) {

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = com.github.mikephil.charting.utils.Utils.convertDpToPixel(dataSet.barBorderWidth)
        val drawBorder = dataSet.barBorderWidth > 0f

        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        val buffer = mBarBuffers[index]
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)
        buffer.feed(dataSet)
        trans.pointValuesToPixel(buffer.buffer)

        val isSingleColor = dataSet.colors.size == 1
        if (isSingleColor) mRenderPaint.color = dataSet.color

        val rect = RectF()
        var j = 0
        while (j < buffer.size()) {
            val left = buffer.buffer[j]
            val top = buffer.buffer[j + 1]
            val right = buffer.buffer[j + 2]
            val bottom = buffer.buffer[j + 3]

            if (!mViewPortHandler.isInBoundsLeft(right)) { j += 4; continue }
            if (!mViewPortHandler.isInBoundsRight(left)) break

            if (!isSingleColor) mRenderPaint.color = dataSet.getColor(j / 4)

            rect.set(left, top, right, bottom)
            c.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, mRenderPaint)
            if (drawBorder) c.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, mBarBorderPaint)
            j += 4
        }
    }
}
