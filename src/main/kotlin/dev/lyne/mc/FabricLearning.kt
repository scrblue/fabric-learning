package dev.lyne.mc

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object FabricLearning : ModInitializer {
    val logger = LoggerFactory.getLogger("fabric-learning")

    val ALUMINUM = MetalMaterial("aluminum")

    val THIRST_ATTRIBUTE_ID = Identifier("fabric-learning", "thirst_attribute")
    val THIRST_ATTRIBUTE = registerAttribute(THIRST_ATTRIBUTE_ID, ThirstAttribute())

    override fun onInitialize() {
        logger.info("Hello Fabric world!")

        ServerPlayConnectionEvents.JOIN.register({ handler, sender, server ->
            val serverState = PlayerAttributeState(server)
            val playerThirst = serverState.getPlayerState(handler.player, THIRST_ATTRIBUTE_ID)
            if (playerThirst != null) {
                var packet = PacketByteBufs.create()
                packet.writeFloat(playerThirst)
                sender.sendPacket(THIRST_ATTRIBUTE.UPDATE_CHANNEL, packet)
            }
        })

        var callbacks: ArrayList<Pair<Identifier, (PlayerAttribute, Float) -> Float>> =
                ArrayList(PLAYER_ATTRIBUTE_REGISTRY.ids.size)
        for (id in PLAYER_ATTRIBUTE_REGISTRY.ids) {
            val attribute = PLAYER_ATTRIBUTE_REGISTRY.get(id)
            if (attribute != null) {
                val maybeLambda: ((PlayerAttribute, Float) -> Float)? = attribute.onTick()
                if (maybeLambda != null) {
                    callbacks.add(Pair(id, maybeLambda))
                }
            }
        }
        ServerTickEvents.END_SERVER_TICK.register({ server -> 
            for ((id, callback) in callbacks) {
                val serverState = PlayerAttributeState(server)
                for ((uuid, map) in serverState.playerAttrs) {
                    val settings = PLAYER_ATTRIBUTE_REGISTRY.get(id)
                    var curVal = map.computeIfAbsent(id, { _ -> 
                        if (settings == null) {
                            0f
                        } else {
                            settings.default
                        }
                    } )

                    if (settings != null) {
                        map.set(id,callback(settings, curVal))
                        serverState.markDirty()
                        val player = server.playerManager.getPlayer(uuid)
                        if (player != null) {
                            var packet = PacketByteBufs.create()
                            packet.writeFloat(curVal)
                            ServerPlayNetworking.send(player, settings.UPDATE_CHANNEL, packet)
                        }
                    }
                }
            }
        })
    }
}
