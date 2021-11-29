package com.diskin.alon.sonix.common.presentation

sealed class ViewUpdateState {

    object EndLoading : ViewUpdateState()

    object Loading : ViewUpdateState()
}