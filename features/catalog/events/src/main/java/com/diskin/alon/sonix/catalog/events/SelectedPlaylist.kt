package com.diskin.alon.sonix.catalog.events

import android.net.Uri

/**
 * Catalog bounded context event that holds the data of a selected by user playlist.
 *
 * @param startIndex the index in the [tracks] list that contains the starting track.
 * @param tracks a list containing the playlist tracks [Uri]s.
 */
data class SelectedPlaylist(val startIndex: Int,val tracks: List<Uri>)