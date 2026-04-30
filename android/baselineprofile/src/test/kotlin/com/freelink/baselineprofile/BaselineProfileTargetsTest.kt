package com.freelink.baselineprofile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineProfileTargetsTest {
    @Test
    fun targetsMatchRequiredMvpScenarios() {
        val scenarioIds = BaselineProfileTargets.all.map { it.scenarioId }.toSet()

        assertEquals(
            setOf("startup", "chat-list", "open-chat", "media", "voice"),
            scenarioIds
        )
    }

    @Test
    fun eachTargetHasRouteAndWarmupActions() {
        BaselineProfileTargets.all.forEach { target ->
            assertTrue(target.routeHint.isNotBlank())
            assertTrue(target.warmupActions.isNotEmpty())
        }
    }
}
