package com.anibalventura.likepaint

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush.*

class MainActivity : AppCompatActivity() {

    private var ibCurrentPaint: ImageButton? = null
    private lateinit var colorTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView.setBrushSize(20.toFloat())

        ibCurrentPaint = llColorsPalette[1] as ImageButton
        ibCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_selected)
        )

        ibBrush.setOnClickListener {
            brushSizeChooser()
        }

        ibEraser.setOnClickListener {
            colorTag = it.tag.toString()
            drawingView.setBrushColor(colorTag)
        }
    }

    private fun brushSizeChooser() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush)
        brushDialog.setTitle("Choose brush size")

        brushDialog.ibBrushSmall.setOnClickListener {
            drawingView.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.ibBrushMedium.setOnClickListener {
            drawingView.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.ibBrushLarge.setOnClickListener {
            drawingView.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun colorClicked(view: View) {
        if (view !== ibCurrentPaint) {
            val imageButton = view as ImageButton
            colorTag = imageButton.tag.toString()

            drawingView.setBrushColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_selected)
            )
            ibCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_normal)
            )

            ibCurrentPaint = view
        }
    }
}