package com.diskin.alon.sonix.catalog.domain

data class Album(val id: Long,
                 val name: String,
                 val artist: String,
                 val tracks: Int,
                 val artPath: String)