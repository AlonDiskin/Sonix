package com.diskin.alon.sonix.catalog.application.model

sealed class AudioTracksSorting(val ascending: Boolean) {

    class DateAdded(ascending: Boolean) : AudioTracksSorting(ascending) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is DateAdded) return false
            if (!super.equals(other)) return false
            return true
        }

    }

    class ArtistName(ascending: Boolean) : AudioTracksSorting(ascending) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ArtistName) return false
            if (!super.equals(other)) return false
            return true
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioTracksSorting) return false

        if (ascending != other.ascending) return false

        return true
    }

    override fun hashCode(): Int {
        return ascending.hashCode()
    }
}
