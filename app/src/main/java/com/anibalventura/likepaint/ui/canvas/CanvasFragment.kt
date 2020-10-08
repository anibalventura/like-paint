package com.anibalventura.likepaint.ui.canvas

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.anibalventura.likepaint.R
import com.anibalventura.likepaint.databinding.FragmentCanvasBinding
import com.anibalventura.likepaint.utils.Constants
import com.anibalventura.likepaint.utils.toast
import kotlinx.android.synthetic.main.dialog_brush.*
import kotlinx.android.synthetic.main.fragment_canvas.*
import kotlinx.coroutines.launch
import java.io.OutputStream

class CanvasFragment : Fragment() {

    // DataBinding.
    private var _binding: FragmentCanvasBinding? = null
    private val binding get() = _binding!!

    private var ibCurrentPaint: ImageButton? = null
    private lateinit var colorTag: String
    private lateinit var result: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for requireContext() fragment.
        _binding = FragmentCanvasBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.drawing = drawingView

        setupCanvas()

        return binding.root
    }

    private fun setupCanvas() {
        // Setup palette.
        ibCurrentPaint = binding.clColorsPalette[1] as ImageButton
        ibCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.palette_selected)
        )

        // Enable buttons.
        brushColorSelected()
        brushSizeChooser()
        undoDraw()
        eraseDraw()
        addImage()
        saveDrawing()
        shareDrawing()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode == Activity.RESULT_OK && requestCode == Constants.GALLERY -> {
                try {
                    when {
                        data!!.data != null -> ivBackground.setImageURI(data.data)
                        else -> toast(requireContext(), R.string.error_parsing)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.STORAGE_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            toast(requireContext(), R.string.storage_permission_granted)
        } else {
            toast(requireContext(), R.string.storage_permission_denied)
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            ) -> toast(requireContext(), R.string.gallery_permission)
        }

        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), Constants.STORAGE_PERMISSION_CODE
        )
    }

    private fun brushColorSelected() {
        val colors = arrayOf(
            binding.colorBeige,
            binding.colorBlack,
            binding.colorRed,
            binding.colorGreen,
            binding.colorBlue,
            binding.colorYellow,
            binding.colorLollipop,
            binding.colorPurple
        )

        for (color in colors) {
            color.setOnClickListener { view ->
                if (view !== ibCurrentPaint) {
                    val imageButton = view as ImageButton
                    colorTag = imageButton.tag.toString()

                    drawingView.setBrushColor(colorTag)

                    imageButton.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.palette_selected)
                    )
                    ibCurrentPaint!!.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.palette_normal)
                    )

                    ibCurrentPaint = view
                }
            }
        }
    }

    private fun brushSizeChooser() {
        binding.ibBrushSize.setOnClickListener {
            val brushDialog = Dialog(requireContext())
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
    }

    private fun undoDraw() {
        binding.ibUndoDraw.setOnClickListener {
            drawingView.undoPath()
        }
    }

    private fun eraseDraw() {
        binding.ibEraseDraw.setOnClickListener {
            colorTag = it.tag.toString()
            drawingView.setBrushColor(colorTag)
        }
    }

    private fun addImage() {
        binding.ibAddImage.setOnClickListener {
            when {
                isReadStorageAllowed() -> {
                    val pickPhotoIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhotoIntent, Constants.GALLERY)
                }
                else -> requestStoragePermission()
            }
        }
    }

    private fun saveDrawing() {
        binding.ibSaveDrawing.setOnClickListener {
            when {
                isReadStorageAllowed() -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        saveBitmap(getBitmap(flDrawingViewContainer))
                    }
                }
                else -> requestStoragePermission()
            }
        }
    }

    private fun shareDrawing() {
        binding.ibShareDrawing.setOnClickListener {
            saveBitmap(getBitmap(flDrawingViewContainer))

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(result))
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            startActivity(Intent.createChooser(intent, "Share"))
        }
    }

    private fun saveBitmap(bitmap: Bitmap): String {
        val resolver = requireContext().contentResolver
        val fileName = "LikePaint_${System.currentTimeMillis() / 1000}.png"
        val saveLocation = Environment.DIRECTORY_PICTURES

        Toast.makeText(requireContext(), "Drawing saved on: $saveLocation", Toast.LENGTH_LONG)
            .show()

        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (uri != null) {
            saveDrawingToStream(bitmap, resolver.openOutputStream(uri))
            requireContext().contentResolver.update(uri, values, null, null)
            result = uri.toString()
        }

        return result
    }

    private fun getBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawing = view.background

        when {
            bgDrawing != null -> bgDrawing.draw(canvas)
            else -> canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return bitmap
    }

    private fun saveDrawingToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                Log.e("**Exception", "Could not write to stream")
                e.printStackTrace()
            }
        }
    }
}