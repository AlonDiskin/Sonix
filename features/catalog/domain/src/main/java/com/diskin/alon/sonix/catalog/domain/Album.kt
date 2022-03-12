package com.diskin.alon.sonix.catalog.domain

data class Album(val id: Int,
                 val name: String,
                 val artist: String,
                 val tracks: Int,
                 val artPath: String)