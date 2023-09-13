package com.andreromano.invaders

sealed class ViewEvent {
    class ScreenClicked(val x: Float, val y: Float) : ViewEvent()
}