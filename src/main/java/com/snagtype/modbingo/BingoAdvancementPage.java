package com.snagtype.modbingo;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraft.advancements.Advancement;
import org.apache.commons.io.FileUtils;
import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.function.Supplier;
final class BingoAdvancementPage implements Runnable {

    private static final String BINGO_DESCRIPTION = "Collect the following items to fill out your bingo card";
    private static final String BINGO_ADVANCEMENT = "BINGO!";
    private static final String BINGO_ICON = "minecraft:map";
    private static final String BINGO_BACKGROUND = "minecraft:textures/gui/advancements/backgrounds/end.png";
    //private static final String BINGO_BACKGROUND = "minecraft:textures/map/map_background.png";
    private static final String BINGO_CHILD_DESCRIPTION = "get ";
    //private static final String BINGO_CHILD_ADVANCEMENT = "BINGO!";
    //private static final String BINGO_CHILD_ICON = "minecraft:map";
    private static final String ITEM_JSON_FILE_NAME_SUFFIX = ".json";
    private static final String EXPORT_SUCCESSFUL_MESSAGE = "Exported successfully %d items into %s";
    private static final String EXPORT_UNSUCCESSFUL_MESSAGE = "Exporting was unsuccessful.";
    private static final int NUM_OF_SPACES = 25;
    private static final int NUM_OF_ROWS = 5;
    private static final int NUM_OF_COLS = 5;
    @Nonnull
    private final File exportDirectory;
    @Nonnull
    private final List<Item> itemName;
    private final boolean isFreeSpaceEnabled;
    private static String ROOT_NAME = "root"; //beyond me why they don't make the suffix required

    //  BingoAdvancementPage(final IForgeRegistry<Item> itemRegistry)
    BingoAdvancementPage(@Nonnull final File exportDirectory, @Nonnull final List<Item> itemName, boolean isFreeSpaceEnabled) {
        this.exportDirectory = Preconditions.checkNotNull(exportDirectory);
        Preconditions.checkArgument(!exportDirectory.isFile());
        this.itemName = Preconditions.checkNotNull(itemName);
        this.isFreeSpaceEnabled = isFreeSpaceEnabled;
    }

    private void writer(File exportDirectory, String fileName, JsonObject lines) //string array needed
    {
        final File file = new File(this.exportDirectory, fileName);
        try (final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")))) {
            FileUtils.forceMkdir(this.exportDirectory);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(lines));
            writer.flush();

            // ModBingoLog.info(EXPORT_SUCCESSFUL_MESSAGE, lines.size(), fileName);
        } catch (final IOException e) {
            ModBingoLog.warn(EXPORT_UNSUCCESSFUL_MESSAGE);
        }
    }

    @Override
    public void run() {
        // no priority to this thread
        Thread.yield();
        File folder = new File(this.exportDirectory.toString());

        ModBingoLog.info("Directory = " + this.exportDirectory.toString());
        folder.mkdir();
        export();

        //logger.info( EXPORT_END_MESSAGE, watch.elapsed( TimeUnit.MILLISECONDS ) );
    }

    public void export() {
        final Iterable<Item> items = this.itemName;
        final List<Item> itemList = Lists.newArrayList(items);

        //start by creating a root which creates the page in advancements gui

        writer(this.exportDirectory, ROOT_NAME + ".json", BuildRootJson());

        // 25 different item space json files
        for (int y = 0; y < NUM_OF_ROWS; y++) {
            for (int x = 0; x < NUM_OF_COLS+1; x++) {
                int iterationNum = (y + x * NUM_OF_ROWS) + 1;
                String fileName = "Row_" + (y + 1) + "_Col_" + (x + 1);
                int parentIteration = x - 1;
                String parentName;
                if (x == 0) {// connecting first col to root
                    parentName = ROOT_NAME;
                } else {
                    parentName = "Row_" + (y + 1) + "_Col_" + (x);


                }
                if (x == 5) {
                    final JsonObject lines =BuildDummyJson(parentName);
                    writer(this.exportDirectory, fileName + ".json", lines);
                } else {
                    Item currItem = itemName.get(iterationNum);
                    final JsonObject lines = BuildChildJson(currItem, parentName);
                    writer(this.exportDirectory, fileName + ".json", lines);
                }
            }
        }
    }

    public JsonObject BuildDummyJson( String parentName) {

        JsonObject lines = new JsonObject();
        JsonObject display = new JsonObject();
        JsonObject criteria = new JsonObject();
        JsonObject title = new JsonObject();
        JsonObject description = new JsonObject();
        JsonObject icon = new JsonObject();
        JsonObject dummy = new JsonObject();
        JsonObject conditions = new JsonObject();
        JsonArray itemsarray = new JsonArray();
        JsonObject items = new JsonObject();

        display.add("title",title);
        display.add("description", description);
        display.add("icon", icon);
        dummy.addProperty("trigger" , "minecraft:tick");

        dummy.add("conditions", conditions);
        conditions.add("items", itemsarray);

        title.addProperty("text", BINGO_ADVANCEMENT);
        description.addProperty("text", BINGO_DESCRIPTION);
        icon.addProperty("item", BINGO_ICON);
        display.addProperty("frame", "task");
        display.addProperty("show_toast", false);
        display.addProperty("announce_to_chat", false);
        display.addProperty("hidden", true);

        itemsarray.add(items);
        lines.add("display", display);
        criteria.add("Acquire_Item", dummy);
        lines.add("criteria" , criteria);
        lines.addProperty("parent", "bingo:" + parentName);

        return lines;
    }

    JsonObject BuildRootJson()
    {
        JsonObject lines = new JsonObject();
        JsonObject display = new JsonObject();
        JsonObject criteria = new JsonObject();
        JsonObject title = new JsonObject();
        JsonObject description = new JsonObject();
        JsonObject icon = new JsonObject();
        JsonObject dummy = new JsonObject();
        JsonObject conditions = new JsonObject();
        JsonArray itemsarray = new JsonArray();
        JsonObject items = new JsonObject();

        display.add("title",title);
        display.add("description", description);
        display.add("icon", icon);
        dummy.addProperty("trigger" , "minecraft:tick");

        dummy.add("conditions", conditions);
        conditions.add("items", itemsarray);

      //  items.addProperty("item", "minecraft:cobblestone");
        title.addProperty("text", BINGO_ADVANCEMENT);
        description.addProperty("text", BINGO_DESCRIPTION);
        icon.addProperty("item", BINGO_ICON);
        display.addProperty("frame", "task");
        display.addProperty("show_toast", true);
        display.addProperty("announce_to_chat", true);
        display.addProperty("hidden", false);
        display.addProperty("background", BINGO_BACKGROUND);

        itemsarray.add(items);
        lines.add("display", display);
        criteria.add("dummy", dummy);
        lines.add("criteria" , criteria);

        return lines;
    }
        public JsonObject BuildChildJson(@Nonnull final Item item, String parentName) {
            String iconPath = item.getRegistryName().toString();
            String itemName = iconPath.split(":")[1];
            ModBingoLog.info(" registry name = " + iconPath);
            //String itemName = item.toString();
            JsonObject lines = new JsonObject();
            JsonObject display = new JsonObject();
            JsonObject criteria = new JsonObject();
            JsonObject title = new JsonObject();
            JsonObject description = new JsonObject();
            JsonObject icon = new JsonObject();
            JsonObject acquireItem = new JsonObject();
            JsonObject conditions = new JsonObject();
            JsonArray itemsarray = new JsonArray();
            JsonObject items = new JsonObject();

            display.add("title",title);
            display.add("description", description);
            display.add("icon", icon);
            acquireItem.addProperty("trigger" , "minecraft:inventory_changed");

            acquireItem.add("conditions", conditions);
            conditions.add("items", itemsarray);

            items.addProperty("item", iconPath);
            title.addProperty("text", itemName );
            description.addProperty("text", BINGO_CHILD_DESCRIPTION+iconPath);
            icon.addProperty("item",iconPath);
            display.addProperty("frame", "task");
            display.addProperty("show_toast", true);
            display.addProperty("announce_to_chat", true);
            display.addProperty("hidden", false);

            itemsarray.add(items);
            lines.add("display", display);
            criteria.add("Acquire_Item", acquireItem);
            lines.add("criteria" , criteria);
            lines.addProperty("parent", "bingo:" + parentName);

            return lines;
        }


    }


