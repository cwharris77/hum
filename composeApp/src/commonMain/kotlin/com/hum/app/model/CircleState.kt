package com.hum.app.model

enum class CircleState {
    Recording,
    Ready,
    Muted,
    Soloed;

    fun canTransitionTo(target: CircleState): Boolean = when (this) {
        Recording -> target == Ready
        Ready -> target == Muted || target == Soloed
        Muted -> target == Ready || target == Soloed
        Soloed -> target == Ready || target == Muted
    }

    fun transitionTo(target: CircleState): CircleState {
        require(canTransitionTo(target)) {
            "Invalid state transition: $this -> $target"
        }
        return target
    }
}
