package com.freelink.backend.libs.people.service

import com.freelink.backend.libs.people.domain.PersonSummary

class InMemoryPeopleService : PeopleService {
    private val peopleByUser = mutableMapOf<String, List<PersonSummary>>()

    override fun listPeople(userId: String, query: String?): List<PersonSummary> {
        val people = peopleByUser.getOrPut(userId) { seedPeople() }
        val normalized = query?.trim()?.lowercase().orEmpty()
        return people.filter { person ->
            normalized.isBlank() ||
                person.displayName.lowercase().contains(normalized) ||
                person.login.lowercase().contains(normalized)
        }
    }

    private fun seedPeople(): List<PersonSummary> {
        return listOf(
            PersonSummary(id = "u-lera", login = "lera.svoi", displayName = "Р›РµСЂР°", isOnline = true),
            PersonSummary(id = "u-artem", login = "artem.svoi", displayName = "РђСЂС‚С‘Рј", isOnline = true),
            PersonSummary(id = "u-masha", login = "masha.svoi", displayName = "РњР°С€Р°", isOnline = false),
            PersonSummary(id = "u-ilya", login = "ilya.svoi", displayName = "РР»СЊСЏ", isOnline = false)
        )
    }
}
