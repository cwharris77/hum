package com.hum.app.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class CircleStateTest {

    @Test
    fun recordingCanOnlyTransitionToReady() {
        val state = CircleState.Recording
        assertTrue(state.canTransitionTo(CircleState.Ready))
        assertFalse(state.canTransitionTo(CircleState.Muted))
        assertFalse(state.canTransitionTo(CircleState.Soloed))
        assertFalse(state.canTransitionTo(CircleState.Recording))
    }

    @Test
    fun readyCanTransitionToMutedOrSoloed() {
        val state = CircleState.Ready
        assertTrue(state.canTransitionTo(CircleState.Muted))
        assertTrue(state.canTransitionTo(CircleState.Soloed))
        assertFalse(state.canTransitionTo(CircleState.Recording))
        assertFalse(state.canTransitionTo(CircleState.Ready))
    }

    @Test
    fun mutedCanTransitionToReadyOrSoloed() {
        val state = CircleState.Muted
        assertTrue(state.canTransitionTo(CircleState.Ready))
        assertTrue(state.canTransitionTo(CircleState.Soloed))
        assertFalse(state.canTransitionTo(CircleState.Recording))
        assertFalse(state.canTransitionTo(CircleState.Muted))
    }

    @Test
    fun soloedCanTransitionToReadyOrMuted() {
        val state = CircleState.Soloed
        assertTrue(state.canTransitionTo(CircleState.Ready))
        assertTrue(state.canTransitionTo(CircleState.Muted))
        assertFalse(state.canTransitionTo(CircleState.Recording))
        assertFalse(state.canTransitionTo(CircleState.Soloed))
    }

    @Test
    fun transitionToReturnsTargetState() {
        assertEquals(
            CircleState.Ready,
            CircleState.Recording.transitionTo(CircleState.Ready)
        )
        assertEquals(
            CircleState.Muted,
            CircleState.Ready.transitionTo(CircleState.Muted)
        )
    }

    @Test
    fun invalidTransitionThrows() {
        assertFailsWith<IllegalArgumentException> {
            CircleState.Recording.transitionTo(CircleState.Muted)
        }
    }
}
