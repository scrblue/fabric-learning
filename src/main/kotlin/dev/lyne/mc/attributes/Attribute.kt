package dev.lyne.mc

import java.util.UUID
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.world.PersistentState
import net.minecraft.world.World

val PLAYER_ATTRIBUTE_REGISTRY_KEY: RegistryKey<Registry<PlayerAttribute>> =
        RegistryKey.ofRegistry(Identifier("fabric-learning", "player_attribute_registry"))

val PLAYER_ATTRIBUTE_REGISTRY: Registry<PlayerAttribute> =
        FabricRegistryBuilder.createSimple(PLAYER_ATTRIBUTE_REGISTRY_KEY).buildAndRegister()

fun registerAttribute(
        identifier: Identifier,
        attribute: PlayerAttribute,
): PlayerAttribute {
    return Registry.register(PLAYER_ATTRIBUTE_REGISTRY, identifier, attribute)
}

class PlayerAttributeSettings(minVal: Float, maxVal: Float, default: Float) {
    val minVal: Float = minVal
    val maxVal: Float = maxVal
    val default: Float = default
}

abstract class PlayerAttribute(settings: PlayerAttributeSettings) {
    val minVal: Float = settings.minVal
    val maxVal: Float = settings.maxVal
    val default: Float = settings.default

    abstract val UPDATE_CHANNEL: Identifier

    abstract fun onTick(): ((PlayerAttribute, Float) -> Float)?
}

class PlayerAttributeState() : PersistentState() {
    var playerAttrs: HashMap<UUID, HashMap<Identifier, Float>> = HashMap()

    /**
     * Serializes the `PlayerAttributeState` into the NBT format.
     *
     * Should looks something like when encoded:
     * ```
     * playerAttrs:
     *    $uuid_1:
     *        $attr_1: $attr_1_val
     *        $attr_2: $attr_2_val
     *    $uuid_2:
     *        $attr_1: ...
     * ```
     *
     * Attributes names are pulled from the `PLAYER_ATTRIBUTE_REGISTRY` which also define the
     * settings for that attribute.
     */
    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        var allPlayers = NbtCompound()

        playerAttrs.forEach({ (playerUuid, attrMap) ->
            var perPlayer = NbtCompound()
            for (id in PLAYER_ATTRIBUTE_REGISTRY.ids) {
                val settings = PLAYER_ATTRIBUTE_REGISTRY.get(id)
                val default =
                        if (settings == null) {
                            0f
                        } else {
                            settings.default
                        }

                val currentValueForCurrentAttr = attrMap.getOrDefault(id, default)

                perPlayer.putFloat(id.toString(), currentValueForCurrentAttr)
            }
            allPlayers.put(playerUuid.toString(), perPlayer)
        })

        nbt.put("playerAttrs", allPlayers)
        return nbt
    }

    /**
     * Deserializes the `PlayerAttributeStats` from the NBT format.
     *
     * See `PlayerAttributeStats.writeNbt` for more information.
     */
    constructor(nbt: NbtCompound) : this() {
        val allPlayers = nbt.getCompound("playerAttrs")
        // For every UUID to attribute map
        allPlayers.keys.forEach({ playerUuidStr ->
            var mapOut: HashMap<Identifier, Float> = HashMap()
            val mapNbt = allPlayers.getCompound(playerUuidStr)

            // For every attribute in the map for one UUID
            mapNbt.keys.forEach({ attrName ->
                val attrNameId = Identifier(attrName)
                val attrValue = mapNbt.getFloat(attrName)
                mapOut.put(attrNameId, attrValue)
            })

            val playerUuid = UUID.fromString(playerUuidStr)
            this.playerAttrs.put(playerUuid, mapOut)
        })
    }

    constructor(server: MinecraftServer) : this() {
        val world = server.getWorld(World.OVERWORLD)
        if (world == null) {
            return
        } else {
            var stateManager = world.getPersistentStateManager()
            this.playerAttrs =
                    stateManager.getOrCreate(
                                    { nbt -> PlayerAttributeState(nbt) },
                                    { -> PlayerAttributeState() },
                                    "fabric-learning",
                            )
                            .playerAttrs
        }
    }

    fun getPlayerState(player: LivingEntity, identifier: Identifier): Float? {
        val server = player.world.server
        if (server == null) {
            return null
        } else {
            val state = PlayerAttributeState(server)
            val map = state.playerAttrs.computeIfAbsent(player.uuid, { _ -> HashMap() })
            val out =
                    map.computeIfAbsent(
                            identifier,
                            { id ->
                                val settings = PLAYER_ATTRIBUTE_REGISTRY.get(id)
                                val default =
                                        if (settings == null) {
                                            0f
                                        } else {
                                            settings.default
                                        }
                                default
                            }
                    )

            return out
        }
    }
}
