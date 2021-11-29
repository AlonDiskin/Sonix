package com.diskin.alon.sonix.catalog.presentation.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.diskin.alon.sonix.catalog.presentation.R
import com.diskin.alon.sonix.catalog.presentation.databinding.FragmentCatalogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator

class CatalogFragment(
    registry: ActivityResultRegistry? = null
) : Fragment() {

    private lateinit var layout: FragmentCatalogBinding
    private val storagePermissionLauncher = createActivityResultLauncher(ActivityResultContracts.RequestMultiplePermissions(),registry) {
        it?.let { grantResult ->
            when(grantResult[Manifest.permission.READ_EXTERNAL_STORAGE]!! &&
                    grantResult[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!) {
                true -> createCatalogFragment()
                false -> showPermissionsDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentCatalogBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkPermissions()
    }

    private fun checkPermissions() {
        // Check runtime permission for storage access since app feature all require this permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, setup pager
            createCatalogFragment()

        } else {
            // Permission is not yet granted
            // Ask the user for the needed permission
            storagePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun <I,O> createActivityResultLauncher(
        contract: ActivityResultContract<I, O>,
        registry: ActivityResultRegistry? = null,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        return when(registry) {
            null -> registerForActivityResult(contract,callback)
            else -> registerForActivityResult(contract,registry, callback)
        }
    }

    private fun createCatalogFragment() {
        // Set pager
        layout.pager.adapter = PagerAdapter(requireActivity(), CatalogFragmentFactory())
        layout.pager.offscreenPageLimit = 5

        // Set tabs
        TabLayoutMediator(layout.tabLayout, layout.pager) { tab, position ->
            when(position) {
                0 -> tab.text = getString(R.string.tab_title_tracks)
                1 -> tab.text = getString(R.string.tab_title_albums)
                2 -> tab.text = getString(R.string.tab_title_playlists)
                3 -> tab.text = getString(R.string.tab_title_artists)
                4 -> tab.text = getString(R.string.tab_title_favorites)
            }
        }.attach()
    }

    private inner class PagerAdapter(
        fa: FragmentActivity,
        private val factory: CatalogFragmentFactory
    ): FragmentStateAdapter(fa) {

        override fun getItemCount(): Int {
            return 5
        }

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> factory.create(CatalogFragmentFactory.Type.TRACKS)
                else -> EmptyFragment()
            }
        }
    }

    private fun showPermissionsDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(getString(R.string.dialog_permissions_message))
            .setTitle(getString(R.string.dialog_permissions_title))
            .setPositiveButton(getString(R.string.dialog_permissions_positive_button)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri

                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton(getString(R.string.dialog_permissions_negative_button)) { _, _ ->
                requireActivity().finish()
            }
            .create()
            .show()
    }
}