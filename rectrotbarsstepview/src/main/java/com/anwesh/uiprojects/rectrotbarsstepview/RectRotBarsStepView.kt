package com.anwesh.uiprojects.rectrotbarsstepview

/**
 * Created by anweshmishra on 26/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val rects : Int = 4
val scDiv : Double = 0.51
val scGap: Float = 0.05f
val sizeFactor : Float = 2.6f
val strokeFactor : Int = 90
val color : Int = Color.parseColor("#0D47A1")

fun Int.inverse() : Float = 1f / this

fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawRRBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val xGap : Float = size / (rects + 1)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = color 
    val barSize : Float = size / 5
    save()
    translate(gap * (i + 1), h/2)
    rotate(90f * sc2)
    drawRect(RectF(-size, -size/2, size, size/2), paint)
    for (k in 0..1) {
        val sck : Float = sc1.divideScale(k, 2)
        save()
        scale(1f, 1f - 2 * k)
        for (j in 0..(rects - 1)) {
            val scj : Float = sck.divideScale(j, rects)
            save()
            translate(xGap, -size/2)
            drawRect(RectF(-barSize/2, -barSize * scj, barSize/2, 0f), paint)
            restore()
        }
        restore()
    }
    restore()
}