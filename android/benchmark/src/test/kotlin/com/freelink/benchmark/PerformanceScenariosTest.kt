package com.freelink.benchmark

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceScenariosTest {
    @Test
    fun catalogContainsMvpCriticalScenarios() {
        val scenarioIds = PerformanceScenarios.all.map { it.id }.toSet()

        assertEquals(
            setOf("startup", "chat-list", "open-chat", "media", "voice"),
            scenarioIds
        )
    }

    @Test
    fun eachScenarioHasBudgetAndCriticalPath() {
        PerformanceScenarios.all.forEach { scenario ->
            assertTrue("${scenario.id} budget must be positive", scenario.budgetMs > 0)
            assertTrue("${scenario.id} path must not be empty", scenario.criticalPath.isNotEmpty())
        }
    }
}
