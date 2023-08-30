package dev.lyne.mc

import net.minecraft.util.Identifier

class ThirstAttribute: PlayerAttribute(PlayerAttributeSettings(0.0f, 20.0f, 20.0f)) {
    override val UPDATE_CHANNEL = Identifier("fabric-learning", "thirst_attribute_update")

    override fun onTick(): ((PlayerAttribute, Float) -> Float)? {
        return {attr, curr -> maxOf(curr - 1f, attr.minVal) } 
    }
}

// TODO: Logic for drinking, for ticking down, for damage when dehydrated, etc.