package com.andreromano.invaders

interface Game {
    var width: Int
    var height: Int
    var activeScene: Scene

    fun changeScene(scene: Scene)
}