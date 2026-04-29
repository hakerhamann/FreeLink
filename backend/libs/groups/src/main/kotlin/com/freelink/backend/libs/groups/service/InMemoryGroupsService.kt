package com.freelink.backend.libs.groups.service

import com.freelink.backend.libs.groups.domain.GroupRole
import com.freelink.backend.libs.groups.domain.GroupSummary
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryGroupsService : GroupsService {
    private val groupsById = ConcurrentHashMap<String, StoredGroup>()

    init {
        seed()
    }

    override fun listGroups(userId: String, query: String?): List<GroupSummary> {
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        return groupsById.values
            .asSequence()
            .filter { group -> group.memberRoles.containsKey(userId) }
            .filter { group ->
                normalizedQuery.isBlank() ||
                    group.title.lowercase().contains(normalizedQuery) ||
                    group.lastMessagePreview.lowercase().contains(normalizedQuery)
            }
            .sortedByDescending { it.updatedAtEpochMs }
            .map { group ->
                GroupSummary(
                    id = group.id,
                    title = group.title,
                    membersCount = group.memberRoles.size,
                    myRole = group.memberRoles.getValue(userId),
                    lastMessagePreview = group.lastMessagePreview,
                    updatedAtEpochMs = group.updatedAtEpochMs
                )
            }
            .toList()
    }

    override fun createGroup(ownerUserId: String, command: CreateGroupCommand): GroupSummary {
        val now = System.currentTimeMillis()
        val groupId = "group-${UUID.randomUUID().toString().take(8)}"
        val roles = linkedMapOf<String, GroupRole>()
        roles[ownerUserId] = GroupRole.OWNER
        command.memberUserIds
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .filterNot { it == ownerUserId }
            .distinct()
            .forEach { roles[it] = GroupRole.MEMBER }

        val stored = StoredGroup(
            id = groupId,
            title = command.title.trim(),
            memberRoles = roles,
            lastMessagePreview = "Group created",
            updatedAtEpochMs = now
        )
        groupsById[groupId] = stored

        return GroupSummary(
            id = stored.id,
            title = stored.title,
            membersCount = stored.memberRoles.size,
            myRole = GroupRole.OWNER,
            lastMessagePreview = stored.lastMessagePreview,
            updatedAtEpochMs = stored.updatedAtEpochMs
        )
    }

    private fun seed() {
        val now = System.currentTimeMillis()
        putSeed(
            StoredGroup(
                id = "group-family",
                title = "Family",
                memberRoles = linkedMapOf(
                    "user-demo" to GroupRole.OWNER,
                    "user-lera" to GroupRole.MEMBER,
                    "user-artem" to GroupRole.MEMBER
                ),
                lastMessagePreview = "Dinner on Saturday at 20:00",
                updatedAtEpochMs = now - 60_000
            )
        )
        putSeed(
            StoredGroup(
                id = "group-weekend",
                title = "Weekend plans",
                memberRoles = linkedMapOf(
                    "user-demo" to GroupRole.ADMIN,
                    "user-masha" to GroupRole.OWNER,
                    "user-ilya" to GroupRole.MEMBER
                ),
                lastMessagePreview = "Let's pick a movie for tonight.",
                updatedAtEpochMs = now - 180_000
            )
        )
    }

    private fun putSeed(group: StoredGroup) {
        groupsById[group.id] = group
    }

    private data class StoredGroup(
        val id: String,
        val title: String,
        val memberRoles: Map<String, GroupRole>,
        val lastMessagePreview: String,
        val updatedAtEpochMs: Long
    )
}
