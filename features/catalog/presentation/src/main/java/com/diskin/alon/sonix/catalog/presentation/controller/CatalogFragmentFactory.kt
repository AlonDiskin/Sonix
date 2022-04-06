package com.diskin.alon.sonix.catalog.presentation.controller

import androidx.fragment.app.Fragment
import javax.inject.Inject

open class CatalogFragmentFactory @Inject constructor() {

    enum class Type { TRACKS, ALBUMS, ARTISTS }

    fun create(type: Type): Fragment {
        return when(type) {
            Type.TRACKS -> AudioTracksFragment()
            Type.ALBUMS -> AlbumsFragment()
            Type.ARTISTS -> ArtistsFragment()
        }
    }
}