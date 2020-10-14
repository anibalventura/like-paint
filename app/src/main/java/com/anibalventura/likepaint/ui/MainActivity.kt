package com.anibalventura.likepaint.ui

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.anibalventura.likepaint.R
import com.anibalventura.likepaint.databinding.ActivityMainBinding
import com.anibalventura.likepaint.utils.setupTheme

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme after splash screen.
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        // Use DataBinding to set the activity view.
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setupNavigation()
        setupTheme(this)
    }

    /* ===================================== Navigation ===================================== */

    override fun onSupportNavigateUp(): Boolean {
        return (Navigation.findNavController(this, R.id.navHostFragment).navigateUp()
                || super.onSupportNavigateUp())
    }

    private fun setupNavigation() {
        val navController: NavController = findNavController(R.id.navHostFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            val toolBar = supportActionBar ?: return@addOnDestinationChangedListener

            binding.toolbar.setBackgroundColor(
                ActivityCompat.getColor(this, R.color.backgroundColor)
            )
            this.window.navigationBarColor = ActivityCompat.getColor(this, R.color.primaryColor)
            this.window.statusBarColor = ActivityCompat.getColor(this, R.color.primaryColor)

            when (destination.id) {
                R.id.canvasFragment -> showToolbarTitleOrUp(toolBar, true, false)
                R.id.aboutFragment -> showToolbarTitleOrUp(toolBar, true, true)
            }
        }
    }

    private fun showToolbarTitleOrUp(
        toolBar: ActionBar, showTitle: Boolean, showUpButton: Boolean
    ) {
        toolBar.setDisplayShowTitleEnabled(showTitle)
        toolBar.setDisplayHomeAsUpEnabled(showUpButton)
    }
}