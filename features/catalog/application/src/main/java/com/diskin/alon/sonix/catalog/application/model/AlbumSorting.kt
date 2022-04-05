package com.diskin.alon.sonix.catalog.application.model

sealed class AlbumSorting(val ascending: Boolean) {

    class Artist(ascending: Boolean) : AlbumSorting(ascending) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Artist) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    class Name(ascending: Boolean) : AlbumSorting(ascending) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Name) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}
