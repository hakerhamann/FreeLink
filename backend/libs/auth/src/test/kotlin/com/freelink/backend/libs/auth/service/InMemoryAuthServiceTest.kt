package com.freelink.backend.libs.auth.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryAuthServiceTest {

    @Test
    fun registerAndLoginFlowWorks() {
        val service = InMemoryAuthService()

        val registerResult = service.register(
            RegisterCommand(
                login = "alice",
                password = "password123",
                deviceName = "Pixel 9"
            )
        )

        assertNotNull(registerResult)
        registerResult ?: return

        val devicesAfterRegister = service.listDevices(registerResult.refreshToken)
        assertEquals(1, devicesAfterRegister.size)

        assertTrue(service.logout(registerResult.refreshToken))
        assertTrue(service.listDevices(registerResult.refreshToken).isEmpty())

        val loginResult = service.login(
            LoginCommand(
                login = "alice",
                password = "password123",
                deviceName = "Pixel 9"
            )
        )

        assertNotNull(loginResult)
    }

    @Test
    fun refreshRotatesTokenAndInvalidatesOldRefresh() {
        val service = InMemoryAuthService()

        val registerResult = service.register(
            RegisterCommand(
                login = "bob",
                password = "password123",
                deviceName = "Galaxy"
            )
        )

        assertNotNull(registerResult)
        registerResult ?: return

        val refreshed = service.refresh(registerResult.refreshToken)
        assertNotNull(refreshed)
        refreshed ?: return

        assertNotEquals(registerResult.refreshToken, refreshed.refreshToken)
        assertNull(service.refresh(registerResult.refreshToken))
        assertEquals(1, service.listDevices(refreshed.refreshToken).size)
    }

    @Test
    fun revokeDeviceRemovesOnlyTargetSession() {
        val service = InMemoryAuthService()

        val firstSession = service.register(
            RegisterCommand(
                login = "charlie",
                password = "password123",
                deviceName = "Tablet"
            )
        )
        assertNotNull(firstSession)
        firstSession ?: return

        val secondSession = service.login(
            LoginCommand(
                login = "charlie",
                password = "password123",
                deviceName = "Laptop"
            )
        )
        assertNotNull(secondSession)
        secondSession ?: return

        val allDevices = service.listDevices(firstSession.refreshToken)
        assertEquals(2, allDevices.size)

        val secondDeviceId = secondSession.deviceId
        assertTrue(service.revokeDevice(firstSession.refreshToken, secondDeviceId))

        val remainingDevices = service.listDevices(firstSession.refreshToken)
        assertEquals(1, remainingDevices.size)
        assertFalse(remainingDevices.any { it.deviceId == secondDeviceId })
        assertFalse(service.revokeDevice(firstSession.refreshToken, "unknown-device-id"))
    }
}
