package com.freelink.backend.libs.people.service

import com.freelink.backend.libs.people.domain.PersonSummary
import java.util.concurrent.ConcurrentHashMap

class InMemoryPeopleService : PeopleService {
    private val peopleByUserId = ConcurrentHashMap<String, List<PersonSummary>>()

    override fun listPeople(userId: String, query: String?): List<PersonSummary> {
        val source = peopleByUserId.computeIfAbsent(userId) { seedPeople() }
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()

        return source
            .asSequence()
            .filter { person ->
                if (normalizedQuery.isBlank()) {
                    true
                } else {
                    person.displayName.lowercase().contains(normalizedQuery) ||
                        person.login.lowercase().contains(normalizedQuery)
                }
            }
            .sortedWith(compareByDescending<PersonSummary> { it.isOnline }.thenBy { it.displayName })
            .toList()
    }

    private fun seedPeople(): List<PersonSummary> {
        return listOf(
            PersonSummary(id = "u-lera", login = "lera.svoi", displayName = "Lera", isOnline = true),
            PersonSummary(id = "u-artem", login = "artem.svoi", displayName = "Artem", isOnline = true),
            PersonSummary(id = "u-masha", login = "masha.svoi", displayName = "Masha", isOnline = false),
            PersonSummary(id = "u-ilya", login = "ilya.svoi", displayName = "Ilya", isOnline = false)
        )
    }
}
