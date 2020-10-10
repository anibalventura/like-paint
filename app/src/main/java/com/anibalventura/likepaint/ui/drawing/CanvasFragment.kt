package com.anibalventura.likepaint.ui.drawing

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.list.listItems
import com.anibalventura.likepaint.R
import com.anibalventura.likepaint.databinding.FragmentCanvasBinding
import com.anibalventura.likepaint.utils.Constants
import com.anibalventura.likepaint.utils.toast
import kotlinx.android.synthetic.main.fragment_canvas.*
import kotlinx.coroutines.launch

class CanvasFragment : Fragment() {

    // DataBinding.
    private var _binding: FragmentCanvasBinding? = null
    private val binding get() = _binding!!

    private var brushColor: Int = Color.BLACK

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for requireContext() fragment.
        _binding = FragmentCanvasBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.drawing = this

        // Set menu.
        setHasOptionsMenu(true)

        return binding.root
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
        val result = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_canvas, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_save_drawing -> saveDrawing()
            R.id.option_share_drawing -> shareDrawing()
        }
        return super.onOptionsItemSelected(item)
    }

    fun brush() {
        binding.drawingView.setBrushColor(brushColor)

        binding.ibBrushSize.setOnLongClickListener {
            val sizes = listOf("Small", "Medium", "Large")
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                title(R.string.dialog_choose_size)
                listItems(items = sizes) { _, index, _ ->
                    when (index) {
                        0 -> binding.drawingView.setBrushSize(5.toFloat())
                        1 -> binding.drawingView.setBrushSize(10.toFloat())
                        2 -> binding.drawingView.setBrushSize(20.toFloat())
                    }
                }
            }
            binding.drawingView.setBrushColor(brushColor)
            return@setOnLongClickListener true
        }
    }

    fun eraser() {
        binding.drawingView.setBrushColor(Color.WHITE)

        binding.ibEraseDraw.setOnLongClickListener {
            val sizes = listOf("Small", "Medium", "Large")
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                title(R.string.dialog_choose_size)
                listItems(items = sizes) { _, index, _ ->
                    when (index) {
                        0 -> binding.drawingView.setEraserSize(5.toFloat())
                        1 -> binding.drawingView.setEraserSize(10.toFloat())
                        2 -> binding.drawingView.setEraserSize(20.toFloat())
                    }
                }
            }
            binding.drawingView.setBrushColor(Color.WHITE)
            return@setOnLongClickListener true
        }
    }

    fun undoDraw() {
        binding.drawingView.undoPath()
    }

    fun redoDraw() {
        binding.drawingView.redoPath()
    }

    fun addImage() {
        when {
            isReadStorageAllowed() -> {
                val pickPhotoIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, Constants.GALLERY)
            }
            else -> requestStoragePermission()
        }
    }

    fun brushColor() {
        // Get colors.
        val colors = intArrayOf(
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
            Color.YELLOW, Color.MAGENTA, Color.GRAY, Color.CYAN,
            ActivityCompat.getColor(requireContext(), R.color.beige),
            ActivityCompat.getColor(requireContext(), R.color.orange),
            ActivityCompat.getColor(requireContext(), R.color.purpleBlue),
            ActivityCompat.getColor(requireContext(), R.color.greenLight)
        )

        // Create dialog to choose color.
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.dialog_choose_color)
            colorChooser(colors, allowCustomArgb = true, showAlphaSelector = true) { _, color ->
                brushColor = color
                binding.drawingView.setBrushColor(brushColor)
            }
            positiveButton(R.string.dialog_select)
            negativeButton(R.string.dialog_negative)
        }
    }

    private fun saveDrawing() {
        when {
            isReadStorageAllowed() -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    binding.drawingView.saveBitmap(
                        binding.drawingView.getBitmap(flDrawingViewContainer)
                    )
                }
            }
            else -> requestStoragePermission()
        }
    }

    private fun shareDrawing() {
        binding.drawingView.saveBitmap(
            binding.drawingView.getBitmap(flDrawingViewContainer)
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, Uri.parse(binding.drawingView.result))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(Intent.createChooser(intent, "Share"))
    }
}