package com.snagtype.utils;

import com.google.common.collect.Lists;
import com.snagtype.modbingo.BingoModRandomizor;
import com.snagtype.modbingo.ModBingoLog;
import jdk.nashorn.internal.parser.JSONParser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.gson.*;



public class RandomItems {

    public File configDirectory = BingoModRandomizor.configDirectory;
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
        //now with list parameter for mod weights
    public static Item getRandomItemFromMod(List<Item> itemListFromMod) {


        Collections.shuffle(itemListFromMod);

        // checks that item can be found in creative tabs
        final CreativeTabs creativeTab = itemListFromMod.get(0).getCreativeTab();
        final NonNullList<ItemStack> stacks = NonNullList.create();
        try {
            if (creativeTab.equals(null)) {
            }
        }
        catch (Exception e) {
            ModBingoLog.info("exception block not found in Creativetabs = " + itemListFromMod.get(0));
            ModBingoLog.info("shuffling items and trying again");
            return getRandomItemFromMod(itemListFromMod);
        }
        // need to add code for meta values here



        return itemList.get(0);
    }
    public static String getRandomMod(List<String> modList) { // picks a random mod not found on blacklist


        Collections.shuffle(modList);

       //if mod found on blacklist
        /*
       if(blacklist.find(modList.get(0))
            return getRandomMod(modList));

         */
        return modList.get(0);
    }
    public void readJson()
    {

        try {
            Object obj = new JsonParser().parse(new FileReader(configDirectory + "items.json"));
            JsonObject currMod= (JsonObject) obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    public static List<Item> getRandomItemList(int numItems) throws IllegalArgumentException{

        // ** todo going to need to pull the modlist from json file instead here

        //list <String> modList =
        if(numItems > itemList.size() - 1){
            throw new IllegalArgumentException();
        }

        // randomly saves i items into a list returns the list

        for (int i = 1; i < numItems; i++)
            {
                itemList.add(getRandomItem());
            }
        return itemList.subList(0, numItems);

        /* with mod weights
        for (int i = 1; i < numItems; i++)
        {
            list <item> itemListFromMod = generateModItemList(getRandomMod(modList);
            getRandomItemFromMod(itemListFromMod)
        }
        return itemList.subList(0, numItems);

         */
    }

}
