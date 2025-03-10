package com.willfp.ecoskills

import com.willfp.ecoskills.api.PlayerSkillExpGainEvent
import com.willfp.ecoskills.api.PlayerSkillLevelUpEvent
import com.willfp.ecoskills.effects.Effect
import com.willfp.ecoskills.skills.Skill
import com.willfp.ecoskills.skills.Skills
import com.willfp.ecoskills.stats.Stat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import java.util.*

val expMultiplierCache = mutableMapOf<UUID, Double>()
val plugin: EcoSkillsPlugin = EcoSkillsPlugin.getInstance()

fun Player.getSkillExperienceMultiplier(): Double {
    if (expMultiplierCache.containsKey(this.uniqueId)) {
        return expMultiplierCache[this.uniqueId]!!
    }
    expMultiplierCache[this.uniqueId] = cacheSkillExperienceMultiplier()
    return this.getSkillExperienceMultiplier()
}

private fun Player.cacheSkillExperienceMultiplier(): Double {
    if (this.hasPermission("ecoskills.xpmultiplier.quadruple")) {
        return 4.0
    }

    if (this.hasPermission("ecoskills.xpmultiplier.triple")) {
        return 3.0
    }

    if (this.hasPermission("ecoskills.xpmultiplier.double")) {
        return 2.0
    }

    if (this.hasPermission("ecoskills.xpmultiplier.50percent")) {
        return 1.5
    }

    val prefix = "ecoskills.xpmultiplier."
    for (permissionAttachmentInfo in this.effectivePermissions) {
        val permission = permissionAttachmentInfo.permission
        if (permission.startsWith(prefix)) {
            return ((permission.substring(permission.lastIndexOf(".") + 1).toDoubleOrNull() ?: 100.0) / 100) + 1
        }
    }

    return 1.0
}

fun OfflinePlayer.getTotalSkillLevel(): Int {
    var total = 0
    for (skill in Skills.values()) {
        total += this.getSkillLevel(skill)
    }
    return total
}

fun OfflinePlayer.getAverageSkillLevel(): Double {
    var total = 0
    for (skill in Skills.values()) {
        total += this.getSkillLevel(skill)
    }
    return total / Skills.values().size.toDouble()
}

fun Player.giveSkillExperience(skill: Skill, experience: Double, isOvershoot: Boolean = false) {
    val exp = if (isOvershoot) experience else experience * this.getSkillExperienceMultiplier()

    val gainEvent = PlayerSkillExpGainEvent(this, skill, exp)
    Bukkit.getPluginManager().callEvent(gainEvent)

    if (gainEvent.isCancelled) {
        return
    }

    val level = this.getSkillLevel(skill)

    this.setSkillProgress(skill, this.getSkillProgress(skill) + exp)

    if (this.getSkillProgress(skill) >= skill.getExpForLevel(level + 1) && level + 1 <= skill.maxLevel) {
        val overshoot = this.getSkillProgress(skill) - skill.getExpForLevel(level + 1)
        this.setSkillProgress(skill, 0.0)
        this.setSkillLevel(skill, level + 1)
        val levelUpEvent = PlayerSkillLevelUpEvent(this, skill, level + 1)
        Bukkit.getPluginManager().callEvent(levelUpEvent)
        this.giveSkillExperience(skill, overshoot, true)
    }
}

fun OfflinePlayer.getSkillLevel(skill: Skill): Int {
    return plugin.dataHandler.readInt(this.uniqueId, skill.id)
}

fun OfflinePlayer.setSkillLevel(skill: Skill, level: Int) {
    plugin.dataHandler.write(this.uniqueId, skill.id, level)
}

fun OfflinePlayer.getSkillProgressToNextLevel(skill: Skill): Double {
    return this.getSkillProgress(skill) / this.getSkillProgressRequired(skill)
}

fun OfflinePlayer.getSkillProgressRequired(skill: Skill): Int {
    return skill.getExpForLevel(this.getSkillLevel(skill) + 1)
}

fun OfflinePlayer.getSkillProgress(skill: Skill): Double {
    return plugin.dataHandler.readDouble(this.uniqueId, skill.xpKey.key)
}

fun OfflinePlayer.setSkillProgress(skill: Skill, level: Double) {
    plugin.dataHandler.write(this.uniqueId, skill.xpKey.key, level)
}

fun OfflinePlayer.getEffectLevel(effect: Effect): Int {
    return plugin.dataHandler.readInt(this.uniqueId, effect.id)
}

fun OfflinePlayer.setEffectLevel(effect: Effect, level: Int) {
    plugin.dataHandler.write(this.uniqueId, effect.id, level)
}

fun OfflinePlayer.getStatLevel(stat: Stat): Int {
    return plugin.dataHandler.readInt(this.uniqueId, stat.id)
}

fun Player.setStatLevel(stat: Stat, level: Int) {
    plugin.dataHandler.write(this.uniqueId, stat.id, level)
    stat.updateStatLevel(this)
}

fun Entity.tryAsPlayer(): Player? {
    return when(this) {
        is Projectile -> {
            val shooter = this.shooter
            if (shooter is Player) shooter else null
        }
        is Player -> this
        else -> null
    }
}