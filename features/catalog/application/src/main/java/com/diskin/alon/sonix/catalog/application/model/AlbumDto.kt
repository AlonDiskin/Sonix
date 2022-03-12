package com.diskin.alon.sonix.catalog.application.model

data class AlbumDto(val id: Int,
                    val name: String,
                    val artist: String,
                    val tracks: Int,
                    val artPath: String)