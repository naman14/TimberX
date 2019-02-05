/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

import java.util.Random

/**
 * a music visualizer sort of animation (with random data)
 */
class MusicVisualizer : View {

    private var random = Random()

    private var paint = Paint()
    private val animateView = object : Runnable {
        override fun run() {

            //run every 100 ms
            postDelayed(this, 120)

            invalidate()
        }
    }

    constructor(context: Context) : super(context) {
        MusicVisualizer(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        //start runnable
        removeCallbacks(animateView)
        post(animateView)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //set paint style, Style.FILL will fill the color, Style.STROKE will stroke the color
        paint.style = Paint.Style.FILL

        canvas.drawRect(getDimensionInPixel(0).toFloat(), (height - (40 + random.nextInt((height / 1.5f).toInt() - 25))).toFloat(), getDimensionInPixel(7).toFloat(), (height - 15).toFloat(), paint)
        canvas.drawRect(getDimensionInPixel(10).toFloat(), (height - (40 + random.nextInt((height / 1.5f).toInt() - 25))).toFloat(), getDimensionInPixel(17).toFloat(), (height - 15).toFloat(), paint)
        canvas.drawRect(getDimensionInPixel(20).toFloat(), (height - (40 + random.nextInt((height / 1.5f).toInt() - 25))).toFloat(), getDimensionInPixel(27).toFloat(), (height - 15).toFloat(), paint)
    }

    //get all dimensions in dp so that views behaves properly on different screen resolutions
    private fun getDimensionInPixel(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE) {
            removeCallbacks(animateView)
            post(animateView)
        } else if (visibility == View.GONE) {
            removeCallbacks(animateView)
        }
    }
}
