package com.freelink.backend.libs.groups.service

import com.freelink.backend.libs.groups.domain.GroupDetails
import com.freelink.backend.libs.groups.domain.GroupMember
import com.freelink.backend.libs.groups.domain.GroupRole
import com.freelink.backend.libs.groups.domain.GroupSummary
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryGroupsService : GroupsService {
    private val groupsById = ConcurrentHashMap<String, StoredGroup>()

    override fun listGroups(userId: String, query: String?): List<GroupSummary> {
        ensureUserSeeded(userId)
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

    override fun getGroupDetails(userId: String, groupId: String): GroupDetails? {
        ensureUserSeeded(userId)
        val group = groupsById[groupId] ?: return null
        val role = group.memberRoles[userId] ?: return null
        return GroupDetails(
            summary = GroupSummary(
                id = group.id,
                title = group.title,
                membersCount = group.memberRoles.size,
                myRole = role,
                lastMessagePreview = group.lastMessagePreview,
                updatedAtEpochMs = group.updatedAtEpochMs
            ),
            members = group.memberRoles.map { (memberUserId, memberRole) ->
                GroupMember(
                    userId = memberUserId,
                    displayName = memberDisplayName(memberUserId, userId),
                    role = memberRole
                )
            }
        )
    }

    override fun createGroup(ownerUserId: String, command: CreateGroupCommand): GroupSummary {
        ensureUserSeeded(ownerUserId)
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
            lastMessagePreview = "Группа создана",
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

    private fun ensureUserSeeded(userId: String) {
        val alreadyHasGroups = groupsById.values.any { it.memberRoles.containsKey(userId) }
        if (alreadyHasGroups) {
            return
        }

        val now = System.currentTimeMillis()
        putSeed(
            StoredGroup(
                id = "group-family-${userId.takeLast(4)}",
                title = "Семья",
                memberRoles = linkedMapOf(
                    userId to GroupRole.OWNER,
                    "user-lera" to GroupRole.MEMBER,
                    "user-artem" to GroupRole.MEMBER
                ),
                lastMessagePreview = "Ужин в субботу в 20:00",
                updatedAtEpochMs = now - 60_000
            )
        )
        putSeed(
            StoredGroup(
                id = "group-weekend-${userId.takeLast(4)}",
                title = "Планы на выходные",
                memberRoles = linkedMapOf(
                    userId to GroupRole.ADMIN,
                    "user-masha" to GroupRole.OWNER,
                    "user-ilya" to GroupRole.MEMBER
                ),
                lastMessagePreview = "Давайте выберем фильм на вечер.",
                updatedAtEpochMs = now - 180_000
            )
        )
    }

    private fun putSeed(group: StoredGroup) {
        groupsById[group.id] = group
    }

    private fun memberDisplayName(memberUserId: String, currentUserId: String): String {
        if (memberUserId == currentUserId) {
            return "Вы"
        }

        return when (memberUserId) {
            "user-lera" -> "Лера"
            "user-artem" -> "Артём"
            "user-masha" -> "Маша"
            "user-ilya" -> "Илья"
            else -> memberUserId.takeLast(8)
        }
    }

    private data class StoredGroup(
        val id: String,
        val title: String,
        val memberRoles: Map<String, GroupRole>,
        val lastMessagePreview: String,
        val updatedAtEpochMs: Long
    )
}
