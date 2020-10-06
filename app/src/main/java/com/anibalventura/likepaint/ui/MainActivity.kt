package com.anibalventura.likepaint.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.anibalventura.likepaint.R
import com.anibalventura.likepaint.utils.Constants.GALLERY
import com.anibalventura.likepaint.utils.Constants.STORAGE_PERMISSION_CODE
import com.anibalventura.likepaint.utils.toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush.*

class MainActivity : AppCompatActivity() {

    private var ibCurrentPaint: ImageButton? = null
    private lateinit var colorTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme after splash screen.
        setTheme(R.style.AppTheme)
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

        ibUndo.setOnClickListener {
            drawingView.undoDrawing()
        }

        ibEraser.setOnClickListener {
            colorTag = it.tag.toString()
            drawingView.setBrushColor(colorTag)
        }

        ibGallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                val pickPhotoIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode == Activity.RESULT_OK && requestCode == GALLERY -> {
                try {
                    when {
                        data!!.data != null -> ivBackground.setImageURI(data.data)
                        else -> toast(this, R.string.error_parsing)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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

    fun colorSelected(view: View) {
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            toast(this, R.string.storage_permission_granted)
        } else {
            toast(this, R.string.storage_permission_denied)
        }
    }

    private fun requestStoragePermission() {
        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            ) -> toast(this, R.string.gallery_permission)
        }

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), STORAGE_PERMISSION_CODE
        )
    }

    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }
}