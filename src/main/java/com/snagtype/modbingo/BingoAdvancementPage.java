package com.snagtype.modbingo;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
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
final class BingoAdvancementPage {


    private static final String ITEM_JSON_FILE_NAME_SUFFIX = ".json";
    private static final String EXPORT_SUCCESSFUL_MESSAGE = "Exported successfully %d items into %s";
    private static final String EXPORT_UNSUCCESSFUL_MESSAGE = "Exporting was unsuccessful.";
    private static final int NUM_OF_SPACES = 25;
    private static final int NUM_OF_ROWS = 5;
    private static final int NUM_OF_COLS = 5;
    @Nonnull
    private final File exportDirectory;
    @Nonnull
    private final IForgeRegistry<Item> itemRegistry;
    private final boolean isFreeSpaceEnabled;
    private static String ROOT_NAME = "root.json";

    //  BingoAdvancementPage(final IForgeRegistry<Item> itemRegistry)
    BingoAdvancementPage(@Nonnull final File exportDirectory, @Nonnull final IForgeRegistry<Item> itemRegistry, boolean isFreeSpaceEnabled)
    {
        this.exportDirectory = Preconditions.checkNotNull( exportDirectory );
        Preconditions.checkArgument( !exportDirectory.isFile() );
        this.itemRegistry = Preconditions.checkNotNull( itemRegistry );
        this.isFreeSpaceEnabled = isFreeSpaceEnabled;
    }
    private void writer(File exportDirectory, String fileName, List<String> lines) //string array needed
    {
        final File file = new File(this.exportDirectory, fileName);
        try (final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")))) {
            FileUtils.forceMkdir(this.exportDirectory);
            //writer.write(joined);
            writer.flush();

           // ModBingoLog.info(EXPORT_SUCCESSFUL_MESSAGE, lines.size(), fileName);
        } catch (final IOException e) {
            ModBingoLog.warn(EXPORT_UNSUCCESSFUL_MESSAGE);
        }
    }
    public void export() {
        final Iterable<Item> items = this.itemRegistry;
        final List<Item> itemList = Lists.newArrayList(items);

        //start by creating a root which creates the page in advancements gui
         writer(this.exportDirectory, ROOT_NAME,BuildRootJson());
        // 25 different item space json files
        if (!isFreeSpaceEnabled)
            for (int x = 0; x < NUM_OF_COLS; x++) {
                for (int y = 0; y < NUM_OF_ROWS; y++) {
                    int iterationNum = y + x * NUM_OF_ROWS;
                    String fileName = iterationNum+ ITEM_JSON_FILE_NAME_SUFFIX;
                    int parentIteration = x-1;
                    String parentName;
                    if (y==0)
                    {
                        parentName = ROOT_NAME;
                    }
                    else
                        parentName = parentIteration+ITEM_JSON_FILE_NAME_SUFFIX;
                    final List<String> lines = Lists.transform(itemList, new BingoAdvancementPage.BuildChildJson( this.itemRegistry,parentName));
                   /* final Joiner newLineJoiner = Joiner.on('\n');
                    final Joiner newLineJoinerIgnoringNull = newLineJoiner.skipNulls();
                    final String joined = newLineJoinerIgnoringNull.join(lines);


                    */
                   writer(this.exportDirectory, fileName,lines);
                }
            }
        else {
            for (int y = 0; y < NUM_OF_ROWS; y++) {
                for (int x = 0; x < NUM_OF_COLS; x++) {
                    int iterationNum = x + y * NUM_OF_COLS;
                    String fileName = iterationNum+ ITEM_JSON_FILE_NAME_SUFFIX;
                    int parentIteration = x-1;
                    String parentName;
                    if (y==0)
                    {
                        parentName = ROOT_NAME;
                    }
                    else
                        parentName = parentIteration+ITEM_JSON_FILE_NAME_SUFFIX;
                    final List<String> lines = Lists.transform(itemList, new BuildChildJson(this.itemRegistry, parentName));
                    /*final Joiner newLineJoiner = Joiner.on('\n');
                    final Joiner newLineJoinerIgnoringNull = newLineJoiner.skipNulls();


                    final String joined = newLineJoinerIgnoringNull.join(lines);
                        */
                    writer(this.exportDirectory,fileName,lines );
                }
            }
        }
    }
    List<String> BuildRootJson()
    {
        List<String> lines = new ArrayList<String>();
        /* final JsonObject jsonObject = new JsonObject();
            jsonObject.add()
            return jsonObject;*/

        return lines;
    }
    private static final class BuildChildJson implements Function<Item, String> {

        /**
         * this extension is required to apply the {@link I18n}
         */
        private static final String LOCALIZATION_NAME_EXTENSION = ".name";
        private static final String EXPORTING_NOTHING_MESSAGE = "Exporting nothing";
        private static final String EXPORTING_SUBTYPES_MESSAGE = "Exporting input %s with subtypes: %b";
        private static final String EXPORTING_SUBTYPES_FAILED_MESSAGE = "Could not export subtypes of: %s";

        @Nonnull
        private final IForgeRegistry<Item> itemRegistry;

        private BuildChildJson(@Nonnull final IForgeRegistry<Item> itemRegistry, String parentName) {
            this.itemRegistry = Preconditions.checkNotNull(itemRegistry);

        }

        @Nullable
        @Override
        public String apply( @Nullable final Item input )
        {
            if( input == null )
            {
                //ModBingoLog.debug( EXPORTING_NOTHING_MESSAGE );

                return null;
            }
            else
            {
                //ModBingoLog.debug( EXPORTING_SUBTYPES_MESSAGE, input.getUnlocalizedName(), input.getHasSubtypes() );
            }

            final String itemName = ForgeRegistries.ITEMS.getKey( input ).toString();


            final List<String> joinedBlockAttributes = Lists.newArrayListWithCapacity( 5 );
            final String unlocalizedItem = input.getUnlocalizedName();
            joinedBlockAttributes.add( itemName );


           /* final Joiner jsonJoiner = Joiner.on( ", " );
            final Joiner jsonJoinerIgnoringNulls = jsonJoiner.skipNulls();

            return jsonJoinerIgnoringNulls.join( joinedBlockAttributes );
            */
           /* final JsonObject jsonObject = new JsonObject();
            jsonObject.add()
            return jsonObject;*/
           return null;
        }
    }
    }


