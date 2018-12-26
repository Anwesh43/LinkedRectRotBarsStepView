package com.anwesh.uiprojects.linkedrectrotbarsstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.rectrotbarsstepview.RectRotBarsStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RectRotBarsStepView.create(this)
    }
}
