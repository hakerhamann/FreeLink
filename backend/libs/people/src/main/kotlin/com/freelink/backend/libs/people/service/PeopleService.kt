package com.freelink.backend.libs.people.service

import com.freelink.backend.libs.people.domain.PersonSummary

interface PeopleService {
    fun listPeople(userId: String, query: String?): List<PersonSummary>
}
