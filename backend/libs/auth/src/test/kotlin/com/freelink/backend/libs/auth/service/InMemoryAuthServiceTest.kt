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

        assertEquals(registerResult.userId, service.resolveUserIdByAccessToken(registerResult.accessToken))

        val devicesAfterRegister = service.listDevicesByAccessToken(registerResult.accessToken)
        assertNotNull(devicesAfterRegister)
        devicesAfterRegister ?: return
        assertEquals(1, devicesAfterRegister.size)

        assertTrue(service.logout(registerResult.refreshToken))
        assertNull(service.listDevicesByAccessToken(registerResult.accessToken))
        assertNull(service.resolveUserIdByAccessToken(registerResult.accessToken))

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
        val devicesAfterRefresh = service.listDevicesByAccessToken(refreshed.accessToken)
        assertNotNull(devicesAfterRefresh)
        assertEquals(1, devicesAfterRefresh?.size)
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

        val allDevices = service.listDevicesByAccessToken(firstSession.accessToken)
        assertNotNull(allDevices)
        allDevices ?: return
        assertEquals(2, allDevices.size)

        val secondDeviceId = secondSession.deviceId
        assertTrue(service.revokeDeviceByAccessToken(firstSession.accessToken, secondDeviceId))

        val remainingDevices = service.listDevicesByAccessToken(firstSession.accessToken)
        assertNotNull(remainingDevices)
        remainingDevices ?: return
        assertEquals(1, remainingDevices.size)
        assertFalse(remainingDevices.any { it.deviceId == secondDeviceId })
        assertFalse(service.revokeDeviceByAccessToken(firstSession.accessToken, "unknown-device-id"))
    }
}
