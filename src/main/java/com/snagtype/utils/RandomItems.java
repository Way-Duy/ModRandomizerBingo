package com.snagtype.utils;

import com.google.common.collect.Lists;
import com.snagtype.modbingo.ModBingoLog;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomItems {

    private static Iterable<Item> items = ForgeRegistries.ITEMS;
    private static List<Item> itemList = Lists.newArrayList( items);
    public static Item getRandomItem() {


        Collections.shuffle(itemList);

        // checks that item can be found in creative tabs
        final CreativeTabs creativeTab = itemList.get(0).getCreativeTab();
        final NonNullList<ItemStack> stacks = NonNullList.create();
        try {
            if (creativeTab.equals(null)) {
            }
        }
        catch (Exception e) {
            ModBingoLog.info("exception block not found in Creativetabs = " + itemList.get(0));
            ModBingoLog.info("shuffling items and trying again");
            return getRandomItem();
        }
        // need to add code for meta values here



        return itemList.get(0);
    }

    public static List<Item> getRandomItemList(int numItems) throws IllegalArgumentException{

        if(numItems > itemList.size() - 1){
            throw new IllegalArgumentException();
        }

        // randomly saves i items into a list returns the list

        for (int i = 1; i < numItems; i++)
            {
                itemList.add(getRandomItem());
            }
        return itemList.subList(0, numItems);
    }
}
