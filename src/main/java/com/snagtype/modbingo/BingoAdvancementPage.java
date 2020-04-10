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

    private static final String BINGO_BACKGROUND = "minecraft:textures/gui/advancements/backgrounds/stone.png";
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
    private static String ROOT_NAME = "root.json";

    //  BingoAdvancementPage(final IForgeRegistry<Item> itemRegistry)
    BingoAdvancementPage(@Nonnull final File exportDirectory, @Nonnull final List<Item> itemName, boolean isFreeSpaceEnabled)
    {
        this.exportDirectory = Preconditions.checkNotNull( exportDirectory );
        Preconditions.checkArgument( !exportDirectory.isFile() );
        this.itemName = Preconditions.checkNotNull( itemName );
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
    public void run()
    {
        // no priority to this thread
        Thread.yield();
       File folder = new File(this.exportDirectory.toString());

        ModBingoLog.info("Directory = "+ this.exportDirectory.toString());
        folder.mkdir();
        export();

        //logger.info( EXPORT_END_MESSAGE, watch.elapsed( TimeUnit.MILLISECONDS ) );
    }
    public void export() {
        final Iterable<Item> items = this.itemName;
        final List<Item> itemList = Lists.newArrayList(items);

        //start by creating a root which creates the page in advancements gui

         writer(this.exportDirectory, ROOT_NAME,BuildRootJson());

        // 25 different item space json files
        if (!isFreeSpaceEnabled)
            for (int y = 0; y < NUM_OF_ROWS; y++) {
                for (int x = 0; x < NUM_OF_COLS; x++) {
                    int iterationNum = (y + x * NUM_OF_ROWS) +1;
                    String fileName = "Row "+ (y+1) + " Col " + (x+1) + ITEM_JSON_FILE_NAME_SUFFIX;
                    int parentIteration = x-1;
                    String parentName;
                    if (y==0)
                    {
                        parentName = ROOT_NAME;
                    }
                    else {
                        parentName = parentIteration + ITEM_JSON_FILE_NAME_SUFFIX;

                    }
                    final JsonObject lines = BuildChildJson( this.itemName,parentName);
                    writer(this.exportDirectory, fileName,lines);
                }
            }
        else {
            for (int y = 0; y < NUM_OF_ROWS; y++) {
                for (int x = 0; x < NUM_OF_COLS; x++) {

                    int iterationNum = (y + x * NUM_OF_ROWS) +1;
                    String fileName = iterationNum+ ITEM_JSON_FILE_NAME_SUFFIX;
                    int parentIteration = x-1;
                    String parentName;
                    if (y==0)
                    {
                        parentName = ROOT_NAME;
                    }
                    else
                        parentName = parentIteration+ITEM_JSON_FILE_NAME_SUFFIX;

                    final JsonObject lines = BuildChildJson( this.itemName,parentName);
                    writer(this.exportDirectory,fileName,lines );
                }
            }
        }

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
        dummy.addProperty("trigger" , "minecraft:inventory_changed");

        dummy.add("conditions", conditions);
        conditions.add("items", itemsarray);

        items.addProperty("item", "minecraft:cobblestone");
        title.addProperty("text", "Advancement");
        description.addProperty("text", "Description");
        icon.addProperty("item", "minecraft:map");
        display.addProperty("frame", "task");
        display.addProperty("show_toast", "true");
        display.addProperty("announce_to_chat", "true");
        display.addProperty("hidden", "false");
        display.addProperty("background", BINGO_BACKGROUND);

        itemsarray.add(items);
        lines.add("display", display);
        criteria.add("dummy", dummy);
        lines.add("criteria" , criteria);

        return lines;
    }
        public JsonObject BuildChildJson(@Nonnull final List<Item> itemName, String parentName) {

            JsonObject lines = new JsonObject();

            JsonObject display = new JsonObject();
            display.add("title", new JsonArray());
            display.add("description", new JsonArray());
            display.add("icon", new JsonArray());
            display.addProperty("frame", "task");
            display.addProperty("show_toast", "true");
            display.addProperty("announce_to_chat", "true");
            display.addProperty("hidden", "false");
            lines.add("display", display);
        return lines;
        }


    }


