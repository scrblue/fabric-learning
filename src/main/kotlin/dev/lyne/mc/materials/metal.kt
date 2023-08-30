/**
 * Contains a function for registering arbitrary metals. Given the name of a material, it will
 * register the following:
 * * An ore block
 * * A deepslate ore block
 * * A "Block of Raw ..."
 * * A standard "Block of ..."
 * * A "Raw ..." item
 * * A "... Nugget" item
 * * A "... Ingot" item
 * * Each of the tools, armor, and weapons that can be made from standard metals
 */
package dev.lyne.mc

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class MetalMaterial(name: String) {
    public val ORE_BLOCK =
            registerBlockAndItem(
                    "${name}_ore",
                    FabricBlockSettings.create().strength(3.0f).requiresTool(),
                    FabricItemSettings()
            )

    public val DEEPSLATE_ORE_BLOCK =
            registerBlockAndItem(
                    "deepslate_${name}_ore",
                    FabricBlockSettings.create().strength(4.5f).requiresTool(),
                    FabricItemSettings()
            )

    public val BLOCK_OF_RAW_ORE =
            registerBlockAndItem(
                    "block_of_raw_$name",
                    FabricBlockSettings.create().strength(5.0f).requiresTool(),
                    FabricItemSettings()
            )

    public val BLOCK_OF_METAL =
            registerBlockAndItem(
                    "block_of_$name",
                    FabricBlockSettings.create().strength(5.0f).requiresTool(),
                    FabricItemSettings()
            )
}

fun registerBlockAndItem(
        name: String,
        blockSettings: FabricBlockSettings,
        itemSettings: FabricItemSettings,
): Pair<Block, BlockItem> {
    val id = Identifier("fabric-learning", name)
    val block = Registry.register(Registries.BLOCK, id, Block(blockSettings))
    val blockItem = Registry.register(Registries.ITEM, id, BlockItem(block, itemSettings))

    return Pair(block, blockItem)
}
