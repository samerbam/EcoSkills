package com.willfp.ecoskills.data.storage

import com.willfp.ecoskills.EcoSkillsPlugin
import com.willfp.ecoskills.effects.Effects
import com.willfp.ecoskills.skills.Skills
import com.willfp.ecoskills.stats.Stats
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@Suppress("UNCHECKED_CAST")
class MySQLDataHandler(
    private val plugin: EcoSkillsPlugin
) : DataHandler {
    init {
        Database.connect(
            "jdbc:mysql://" +
                    "${plugin.configYml.getString("mysql.host")}:" +
                    "${plugin.configYml.getString("mysql.port")}/" +
                    plugin.configYml.getString("mysql.database"),
            driver = "com.mysql.cj.jdbc.Driver",
            user = plugin.configYml.getString("mysql.user"),
            password = plugin.configYml.getString("mysql.password")
        )

        transaction {
            Players.apply {
                for (skill in Skills.values()) {
                    registerColumn<Int>(skill.id, IntegerColumnType())
                        .default(0)
                    registerColumn<Double>(skill.xpKey.key, DoubleColumnType())
                        .default(0.0)
                }

                for (stat in Stats.values()) {
                    registerColumn<Int>(stat.id, IntegerColumnType())
                        .default(0)
                }

                for (effect in Effects.values()) {
                    registerColumn<Int>(effect.id, IntegerColumnType())
                        .default(0)
                }
            }

            SchemaUtils.create(Players)
        }
    }

    override fun save() {
        // Do nothing
    }

    override fun <T> write(uuid: UUID, key: String, value: T) {
        transaction {
            Players.select { Players.uuid eq uuid }.firstOrNull() ?: run {
                Players.insert {
                    it[this.uuid] = uuid
                }
            }
            val column: Column<T> = Players.columns.stream().filter { it.name == key }.findFirst().get() as Column<T>
            Players.update ({ Players.uuid eq uuid }) {
                it[column] = value
            }
        }
    }

    override fun readInt(uuid: UUID, key: String): Int {
        var value = 0
        transaction {
            val player = Players.select { Players.uuid eq uuid }.firstOrNull() ?: return@transaction
            value = player[Players.columns.stream().filter { it.name == key }.findFirst().get()] as Int? ?: 0
        }
        return value
    }

    override fun readDouble(uuid: UUID, key: String): Double {
        var value = 0.0
        transaction {
            val player = Players.select { Players.uuid eq uuid }.firstOrNull() ?: return@transaction
            value = player[Players.columns.stream().filter { it.name == key }.findFirst().get()] as Double? ?: 0.0
        }
        return value
    }

    override fun readString(uuid: UUID, key: String, default: String): String {
        var value = ""
        transaction {
            val player = Players.select { Players.uuid eq uuid }.firstOrNull() ?: return@transaction
            value = player[Players.columns.stream().filter { it.name == key }.findFirst().get()] as String? ?: ""
        }
        return value
    }

    object Players : IntIdTable("EcoSkills_Players") {
        val uuid = uuid("uuid")
        val name = varchar("name", 50)
            .default("Unknown Player")
    }
}