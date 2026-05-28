package top.zoyn.freeswitchtitle.hook.permission

import taboolib.expansion.getPlayerDataContainer
import top.zoyn.freeswitchtitle.util.ConfigUtils
import java.util.UUID

object PermissionGrantStore {

    fun getGranted(uuid: UUID): Map<String, Set<String>> {
        val text = uuid.getPlayerDataContainer()[ConfigUtils.permissionRecordKey].orEmpty()
        if (text.isBlank()) return emptyMap()
        val map = linkedMapOf<String, MutableSet<String>>()
        text.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.contains(":") }
            .forEach { entry ->
                val uid = entry.substringBefore(":")
                val permission = entry.substringAfter(":")
                if (uid.isNotBlank() && permission.isNotBlank()) {
                    map.getOrPut(uid) { linkedSetOf() }.add(permission)
                }
            }
        return map
    }

    fun getGrantedForTitle(uuid: UUID, uid: String): Set<String> {
        return getGranted(uuid)[uid].orEmpty()
    }

    fun markGranted(uuid: UUID, uid: String, permission: String) {
        val map = getGranted(uuid).mapValues { it.value.toMutableSet() }.toMutableMap()
        map.getOrPut(uid) { linkedSetOf() }.add(permission)
        save(uuid, map)
    }

    fun unmarkGranted(uuid: UUID, uid: String, permission: String) {
        val map = getGranted(uuid).mapValues { it.value.toMutableSet() }.toMutableMap()
        map[uid]?.remove(permission)
        if (map[uid]?.isEmpty() == true) {
            map.remove(uid)
        }
        save(uuid, map)
    }

    private fun save(uuid: UUID, map: Map<String, Set<String>>) {
        val text = map.flatMap { (uid, permissions) ->
            permissions.map { permission -> "$uid:$permission" }
        }.joinToString(",")
        uuid.getPlayerDataContainer()[ConfigUtils.permissionRecordKey] = text
    }
}
