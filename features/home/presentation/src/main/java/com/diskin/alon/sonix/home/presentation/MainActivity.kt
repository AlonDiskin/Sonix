package com.diskin.alon.sonix.home.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.diskin.alon.sonix.home.presentation.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var graphProvider: AppGraphProvider
    private lateinit var layout: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set layout binding
        layout = ActivityMainBinding.inflate(layoutInflater)
        val view = layout.root
        setContentView(view)

        // Set toolbar
        setSupportActionBar(layout.toolbar)

        // Set player nav graph
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.nav_player_container,
                    NavHostFragment.create(graphProvider.getPlayerGraph())
                )
                .commitNow()
        }

        // Set nav controller and navigation ui for main app graph
        val navController = if (savedInstanceState == null) {
            val host = NavHostFragment.create(graphProvider.getAppGraph())

            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, host)
                .setPrimaryNavigationFragment(host)
                .commitNow()
            host.navController

        } else {
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController
        }
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        layout.toolbar.setupWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            layout.appBar.setExpanded(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val controller = findNavController(R.id.nav_host_fragment)

        return when (item.itemId) {
            R.id.action_settings -> {
                val settingsDestUri = getString(R.string.uri_settings).toUri()
                controller.navigate(settingsDestUri)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}