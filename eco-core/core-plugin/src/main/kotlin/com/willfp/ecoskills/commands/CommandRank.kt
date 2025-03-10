package com.willfp.ecoskills.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.CommandHandler
import com.willfp.eco.core.command.TabCompleteHandler
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.ecoskills.data.LeaderboardHandler
import com.willfp.ecoskills.data.savedDisplayName
import com.willfp.ecoskills.getSkillLevel
import com.willfp.ecoskills.skills.Skills
import com.willfp.ecoskills.util.TabCompleteHelper
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil


class CommandRank(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "rank",
        "ecoskills.command.rank",
        false
    ) {

    override fun getHandler(): CommandHandler {
        return CommandHandler { sender: CommandSender, args: List<String> ->
            if (args.isEmpty()) {
                sender.sendMessage(plugin.langYml.getMessage("requires-skill"))
                return@CommandHandler
            }

            val skill = Skills.getByID(args[0].lowercase())

            if (skill == null) {
                sender.sendMessage(plugin.langYml.getMessage("invalid-skill"))
                return@CommandHandler
            }

            val page = if (args.size < 2) 1 else args[1].toIntOrNull() ?: 1

            val top = LeaderboardHandler.getPage(page, skill)

            val messages = plugin.langYml.getStrings("top", false)
            val lines = mutableListOf<String>()

            val useDisplayName = plugin.configYml.getBool("commands.rank.use-display-name")

            for ((rank, player) in top) {
                var line = plugin.langYml.getString("top-line-format", false)
                    .replace("%rank%", rank.toString())
                    .replace("%level%", player.getSkillLevel(skill).toString())

                var name = player.name!!

                if (useDisplayName) {
                    name = player.savedDisplayName
                }

                line = line.replace("%playername%", name)

                lines.add(line)
            }

            val linesIndex = messages.indexOf("%lines%")
            if (linesIndex != -1) {
                messages.removeAt(linesIndex)
                messages.addAll(linesIndex, lines)
            }

            for (message in messages) {
                sender.sendMessage(StringUtils.format(message))
            }
        }
    }

    override fun getTabCompleter(): TabCompleteHandler {
        return TabCompleteHandler { _, args ->
            val completions = mutableListOf<String>()

            if (args.size == 1) {
                StringUtil.copyPartialMatches(
                    args[0],
                    TabCompleteHelper.SKILL_NAMES,
                    completions
                )
                return@TabCompleteHandler completions
            }

            return@TabCompleteHandler emptyList()
        }
    }

}