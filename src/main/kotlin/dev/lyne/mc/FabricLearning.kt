package dev.lyne.mc

import net.fabricmc.api.ModInitializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object FabricLearning : ModInitializer {
    private val logger = LoggerFactory.getLogger("fabric-learning")

    val GOLD_COIN =
            Registry.register(
                    Registries.ITEM,
                    Identifier("fabric-learning", "gold_coin"),
                    GoldCoin()
            )

    override fun onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        logger.info("Hello Fabric world!")
    }
}
