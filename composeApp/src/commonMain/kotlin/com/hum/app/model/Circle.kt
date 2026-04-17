package com.hum.app.model

data class Circle(
    val id: String,
    val filePath: String,
    val durationMs: Long = 0,
    val state: CircleState = CircleState.Ready,
)
