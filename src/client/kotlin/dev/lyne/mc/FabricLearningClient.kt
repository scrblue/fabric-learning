package dev.lyne.mc

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object FabricLearningClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                ThirstAttribute().UPDATE_CHANNEL,
                { _, _, buf, _ ->
                    val thirstValue = buf.readFloat()
                    FabricLearning.logger.info("Thirst: $thirstValue")
                }
        )
    }
}
