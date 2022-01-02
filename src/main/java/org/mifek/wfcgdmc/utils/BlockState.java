package org.mifek.wfcgdmc.utils;

import com.google.common.base.Optional;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import org.jetbrains.annotations.NotNull;
import org.mifek.vgl.implementations.Block;
import org.mifek.vgl.implementations.Blocks;

import java.util.HashMap;

public class BlockState {
    public static Block serialize(IBlockState blockState) {
        HashMap<String, Object> params = new HashMap<>();

        for (IProperty<?> prop : blockState.getPropertyKeys()) {
            if (prop instanceof PropertyEnum) {
                params.put(prop.getName(), ((Enum<?>) blockState.getValue(prop)).name());
            }
        }

        return new Block(Blocks.Companion.getById(net.minecraft.block.Block.getIdFromBlock(blockState.getBlock())), params);
    }

    public static IBlockState deserialize(Block block) {
        IBlockState state = net.minecraft.block.Block.getBlockById(block.getBlock().getId()).getDefaultState();

        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (!block.getProps().containsKey(prop.getName())) continue;

            if (prop instanceof PropertyEnum) state = setEnumProperty(state, (PropertyEnum) prop, block);
        }

        return state;
    }


    @NotNull
    private static <T extends Enum<T> & IStringSerializable> IBlockState setEnumProperty(IBlockState state, PropertyEnum<T> prop, Block block) throws Error {
//        System.out.println("Converting " + prop.getName() + " value " + block.getProps().get(prop.getName()).toString().toLowerCase());
//        System.out.println("Allowed values: " + prop.getAllowedValues().stream().map(item -> item.toString() + " ").collect(Collectors.joining()));
        Optional<T> value = prop.parseValue(block.getProps().get(prop.getName()).toString().toLowerCase());
        if (value.isPresent())
            return state.withProperty(prop, value.get());

        throw new Error("Given property was not an EnumProperty. " + prop.getName() + " " + prop.getValueClass().getName() + " " + prop.getClass().getName() + " \t" + block.getProps().get(prop.getName()).toString());
    }
}
