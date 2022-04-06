package com.diskin.alon.sonix.catalog.application.model

sealed class ArtistsRequest {

    object LastSorted : ArtistsRequest()

    data class GetSorted(val sorting: ArtistSorting) : ArtistsRequest()
}
