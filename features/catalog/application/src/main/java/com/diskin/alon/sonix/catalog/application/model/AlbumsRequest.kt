package com.diskin.alon.sonix.catalog.application.model

sealed class AlbumsRequest {

    object LastSorted : AlbumsRequest()

    data class GetSorted(val sorting: AlbumSorting) : AlbumsRequest()
}
