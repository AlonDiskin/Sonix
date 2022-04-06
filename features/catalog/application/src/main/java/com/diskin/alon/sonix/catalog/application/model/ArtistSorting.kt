package com.diskin.alon.sonix.catalog.application.model

sealed class ArtistSorting(val ascending: Boolean) {

    class Date(ascending: Boolean) : ArtistSorting(ascending) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Date) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    class Name(ascending: Boolean) : ArtistSorting(ascending) {

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
