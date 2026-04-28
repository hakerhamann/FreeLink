package com.freelink.backend.libs.people.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPeopleServiceTest {

    @Test
    fun peopleAreReturnedOnlineFirst() {
        val service = InMemoryPeopleService()

        val people = service.listPeople("user-1", null)

        assertTrue(people.isNotEmpty())
        assertTrue(people.first().isOnline)
    }

    @Test
    fun queryFiltersByDisplayNameOrLogin() {
        val service = InMemoryPeopleService()

        val byDisplay = service.listPeople("user-1", "Lera")
        assertEquals(1, byDisplay.size)

        val byLogin = service.listPeople("user-1", "artem.svoi")
        assertEquals(1, byLogin.size)
    }
}
