package com.snagtype.utils;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomItems {

    public static Item getRandomItem() {
        Iterable<Item> items = ForgeRegistries.ITEMS;
        List<Item> itemList = Lists.newArrayList( items );
        Collections.shuffle(itemList);
        return itemList.get(0);
    }

    public static List<Item> getRandomItemList(int numItems) throws IllegalArgumentException{
        Iterable<Item> items = ForgeRegistries.ITEMS;
        List<Item> itemList = Lists.newArrayList( items );

        if(numItems > itemList.size() - 1){
            throw new IllegalArgumentException();
        }

        Collections.shuffle(itemList);
        return itemList.subList(0, numItems);
    }
}
