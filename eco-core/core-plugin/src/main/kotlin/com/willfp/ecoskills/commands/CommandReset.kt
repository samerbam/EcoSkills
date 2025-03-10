package com.willfp.ecoskills.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.CommandHandler
import com.willfp.eco.core.command.TabCompleteHandler
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.ecoskills.effects.Effects
import com.willfp.ecoskills.setEffectLevel
import com.willfp.ecoskills.setSkillLevel
import com.willfp.ecoskills.setSkillProgress
import com.willfp.ecoskills.setStatLevel
import com.willfp.ecoskills.skills.Skills
import com.willfp.ecoskills.stats.Stats
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil


class CommandReset(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "reset",
        "ecoskills.command.reset",
        false
    ) {
    override fun getHandler(): CommandHandler {
        return CommandHandler { sender: CommandSender, args: List<String> ->
            if (args.isEmpty()) {
                sender.sendMessage(plugin.langYml.getMessage("requires-player"))
                return@CommandHandler
            }

            val player = Bukkit.getPlayer(args[0])
            if (player == null) {
                sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
                return@CommandHandler
            }

            sender.sendMessage(plugin.langYml.getMessage("reset-player"))
            for (stat in Stats.values()) {
                player.setStatLevel(stat, 0)
            }
            for (effect in Effects.values()) {
                player.setEffectLevel(effect, 0)
            }
            for (skill in Skills.values()) {
                player.setSkillLevel(skill, 0)
                player.setSkillProgress(skill, 0.0)
            }
        }
    }

    override fun getTabCompleter(): TabCompleteHandler {
        return TabCompleteHandler { _, args ->
            val completions = mutableListOf<String>()

            if (args.size == 1) {
                StringUtil.copyPartialMatches(
                    args[0],
                    Bukkit.getOnlinePlayers().map { player -> player.name }.toCollection(ArrayList()),
                    completions
                )
                return@TabCompleteHandler completions
            }

            return@TabCompleteHandler emptyList<String>()
        }
    }
}