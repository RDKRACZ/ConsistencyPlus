package io.github.consistencyplus.consistency_plus.core.entries.block;


import dev.architectury.registry.registries.RegistrySupplier;
import io.github.consistencyplus.consistency_plus.base.ConsistencyPlusMain;
import io.github.consistencyplus.consistency_plus.blocks.BlockTypes;
import io.github.consistencyplus.consistency_plus.blocks.BlockShapes;
import io.github.consistencyplus.consistency_plus.core.entries.interfaces.BlockRegistryEntryGroupInterface;
import io.github.consistencyplus.consistency_plus.core.extensions.CPlusFenceGateBlock;
import io.github.consistencyplus.consistency_plus.core.extensions.CPlusStairBlock;
import io.github.consistencyplus.consistency_plus.registry.CPlusEntries;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static io.github.consistencyplus.consistency_plus.registry.CPlusEntries.checkMinecraft;

public abstract class RegistryEntryGroup implements BlockRegistryEntryGroupInterface {
    public static String name;
    public static AbstractBlock.Settings blockSettings;

    public RegistryEntryGroup(String name, AbstractBlock.Settings blockSettings) {
        RegistryEntryGroup.name = name;
        RegistryEntryGroup.blockSettings = blockSettings;
        construct();
    }

    public RegistryEntryGroup(String name, AbstractBlock.Settings blockSettings, boolean construct) {
        //Use this as a way to reference the code without constructing the actual blocks. Useful for DyedRegistryEntry
        RegistryEntryGroup.name = name;
        RegistryEntryGroup.blockSettings = blockSettings;
        if (construct) construct();
    }

    public void construct() {
        for (BlockTypes type : BlockTypes.values()) {
            for (BlockShapes shape : BlockShapes.values()) {
                if (!checkset1(shape, type)) break;
                String id = getID(shape, type);
                if (!checkset2(id)) continue;
                AbstractBlock.Settings specialCased;
                if (checkMinecraft(type.addType(name))) specialCased = AbstractBlock.Settings.copy(getBlock(BlockShapes.BLOCK, type));
                else specialCased = blockSettings;
                register(id, shape, specialCased);
            }
        }
    }

    public boolean checkset1(BlockShapes shape, BlockTypes type) {
        if ((!shape.withTypes || !type.equals(BlockTypes.BASE)) && CPlusEntries.baseOnlyIDs.contains(name)) return false;
        if (!type.equals(BlockTypes.BASE) && !shape.withTypes) return false;
        if (type.equals(BlockTypes.COBBLED) && CPlusEntries.cobblelessMaterials.contains(name)) return false;
        return true;
    }

    public boolean checkset2(String id) {
        if (checkMinecraft(id)) return false;
        if (CPlusEntries.blacklistedIDs.contains(id)) return false;
        return true;
    }

    public RegistrySupplier<Block> blockRegistration(String name, BlockShapes blockShapes, AbstractBlock.Settings blockSettings) {
        return switch (blockShapes) {
            default -> ConsistencyPlusMain.BLOCKS.register(name, () ->  new Block(blockSettings));
            case SLAB -> ConsistencyPlusMain.BLOCKS.register(name, () -> new SlabBlock(blockSettings));
            case STAIRS -> ConsistencyPlusMain.BLOCKS.register(name, () ->  new CPlusStairBlock(Blocks.STONE.getDefaultState(), blockSettings));
            //case STAIRS -> new CPlusStairBlock(getBlock(material, BlockVariations.BLOCK, type).getDefaultState(), blockSettings);
            case WALL -> ConsistencyPlusMain.BLOCKS.register(name, () ->  new WallBlock(blockSettings));
            case GATE -> ConsistencyPlusMain.BLOCKS.register(name, () ->  new CPlusFenceGateBlock(blockSettings));
            case PILLAR -> ConsistencyPlusMain.BLOCKS.register(name, () ->  new PillarBlock(blockSettings));
        };
    }

    public void register(String id, BlockShapes blockShapes, AbstractBlock.Settings blockSettings) {
        RegistrySupplier<Block> a;// = blockRegistration(id, shape, blockSettings);
        RegistrySupplier<Item> b; // = ConsistencyPlusMain.ITEMS.register(id, () -> new BlockItem(a.get(), itemSettings));
    }

    public Block getBlock(BlockShapes shapes, BlockTypes type) {
        String id = getID(shapes, type);
        if (checkMinecraft(id)) return Registry.BLOCK.get(new Identifier("minecraft", (id)));
        return Registry.BLOCK.get(ConsistencyPlusMain.id(id));
    }

    public Item getItem(BlockShapes shapes, BlockTypes type) {
        String id = getID(shapes, type);
        if (checkMinecraft(id)) return Registry.ITEM.get(new Identifier("minecraft", (id)));
        return Registry.ITEM.get(ConsistencyPlusMain.id(id));
    }

    public String getID(BlockShapes shapes, BlockTypes type) {
        String id = shapes.addShapes(type.addType(name), type);
        return CPlusEntries.overrideMap.getOrDefault(id, id);
    }
}
