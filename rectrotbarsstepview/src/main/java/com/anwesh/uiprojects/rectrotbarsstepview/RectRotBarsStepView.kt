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

class RectRotBarsStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, rects * 2, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RRBNode(var i : Int, val state : State = State()) {

        private var next : RRBNode? = null
        private var prev : RRBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = RRBNode(i + 1)
                prev = next
            }
        }
        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRRBNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RRBNode {
            var curr : RRBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RectRotBarsStep(var i : Int) {
        private val root : RRBNode = RRBNode(0)
        private var curr : RRBNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RectRotBarsStepView) {
        private val animator : Animator = Animator(view)
        private val rrbs : RectRotBarsStep = RectRotBarsStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            rrbs.draw(canvas, paint)
            animator.animate {
                rrbs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rrbs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : RectRotBarsStepView {
            val view : RectRotBarsStepView = RectRotBarsStepView(activity)
            activity.setContentView(view)
            return view 
        }
    }
}