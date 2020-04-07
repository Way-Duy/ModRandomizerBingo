/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package export.json;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.snagtype.modbingo.ModBingoLog;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
////import appeng.core.ModBingoLog;

/**
 * handles the exporting including processing, transformation and persisting the information
 *
 * @author thatsIch
 * @version rv3 - 14.08.2015
 * @since rv3 14.08.2015
 */
final class MinecraftItemJsonExporter implements export.json.Exporter
{
	private static final String ITEM_JSON_FILE_NAME = "items.json";
	private static final String MINIMAL_HEADER = "Mod:Item:MetaData, Localized Name";
	private static final String VERBOSE_HEADER = MINIMAL_HEADER + ", Unlocalized Name, Is Block?, Class Name";
	private static final String EXPORT_SUCCESSFUL_MESSAGE = "Exported successfully %d items into %s";
	private static final String EXPORT_UNSUCCESSFUL_MESSAGE = "Exporting was unsuccessful.";

	@Nonnull
	private final File exportDirectory;
	@Nonnull
	private final IForgeRegistry<Item> itemRegistry;
	@Nonnull
	private final export.json.ExportMode mode;

	/**
	 * @param exportDirectory directory of the resulting export file. Non-null required.
	 * @param itemRegistry the registry with minecraft items. Needs to be populated at that time, thus the exporting can
	 * only happen in init (pre-init is the
	 * phase when all items are determined)
	 * @param mode mode in which the export should be operated. Resulting CSV will change depending on this.
	 */
	MinecraftItemJsonExporter(@Nonnull final File exportDirectory, @Nonnull final IForgeRegistry<Item> itemRegistry, @Nonnull final export.json.ExportMode mode )
	{
		this.exportDirectory = Preconditions.checkNotNull( exportDirectory );
		Preconditions.checkArgument( !exportDirectory.isFile() );
		this.itemRegistry = Preconditions.checkNotNull( itemRegistry );
		this.mode = Preconditions.checkNotNull( mode );
	}

	@Override
	public void export()
	{
		final Iterable<Item> items = this.itemRegistry;
		final List<Item> itemList = Lists.newArrayList( items );

		final List<String> lines = Lists.transform( itemList, new ItemRowExtractFunction( this.itemRegistry, this.mode ) );

		final Joiner newLineJoiner = Joiner.on( '\n' );
		final Joiner newLineJoinerIgnoringNull = newLineJoiner.skipNulls();
		final String joined = newLineJoinerIgnoringNull.join( lines );

		final File file = new File( this.exportDirectory, ITEM_JSON_FILE_NAME );

		try(final Writer writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), Charset.forName( "UTF-8" ) ) ) )
		{
			FileUtils.forceMkdir( this.exportDirectory );

			final String header = this.mode == export.json.ExportMode.MINIMAL ? MINIMAL_HEADER : VERBOSE_HEADER;
			writer.write( header );
			writer.write( "\n" );
			writer.write( joined );
			writer.flush();

			ModBingoLog.info( EXPORT_SUCCESSFUL_MESSAGE, lines.size(), ITEM_JSON_FILE_NAME );
		}
		catch( final IOException e )
		{
			ModBingoLog.warn( EXPORT_UNSUCCESSFUL_MESSAGE );
		}
	}

	/**
	 * Extracts item name with meta and the display name
	 */
	private static final class TypeExtractFunction implements Function<ItemStack, String>
	{
		private static final String EXTRACTING_NULL_MESSAGE = "extracting type null";
		private static final String EXTRACTING_ITEM_MESSAGE = "extracting type %s:%d";

		@Nonnull
		private final String itemName;
		@Nonnull
		private final export.json.ExportMode mode;

		private TypeExtractFunction( @Nonnull final String itemName, @Nonnull final export.json.ExportMode mode )
		{
			this.itemName = Preconditions.checkNotNull( itemName );
			Preconditions.checkArgument( !itemName.isEmpty() );

			this.mode = Preconditions.checkNotNull( mode );
		}

		@Nullable
		@Override
		public String apply( @Nullable final ItemStack input )
		{
			if( input == null )
			{

				return null;
			}
			else
			{
			}

			final List<String> joinedBlockAttributes = Lists.newArrayListWithCapacity( 5 );
			final int meta = input.getItemDamage();
			final String metaName = this.itemName + ':' + meta;
			final String localization = input.getDisplayName();

			joinedBlockAttributes.add( metaName );
			joinedBlockAttributes.add( localization );

			if( this.mode == export.json.ExportMode.VERBOSE )
			{
				final Item item = input.getItem();
				final String unlocalizedItem = input.getUnlocalizedName();
				final Block block = Block.getBlockFromItem( item );
				final boolean isBlock = block != Blocks.AIR && !block.equals( Blocks.AIR );
				final Class<? extends ItemStack> stackClass = input.getClass();
				final String stackClassName = stackClass.getName();

				joinedBlockAttributes.add( unlocalizedItem );
				joinedBlockAttributes.add( Boolean.toString( isBlock ) );
				joinedBlockAttributes.add( stackClassName );
			}
			ModBingoLog.info("item name = " + itemName);
			final Joiner csvJoiner = Joiner.on( ", " );
			final Joiner csvJoinerIgnoringNulls = csvJoiner.skipNulls();

			return csvJoinerIgnoringNulls.join( joinedBlockAttributes );
		}
	}

	/**
	 * transforms an item into a row representation of the CSV file
	 */
	private static final class ItemRowExtractFunction implements Function<Item, String>
	{
		/**
		 * this extension is required to apply the {@link I18n}
		 */
		private static final String LOCALIZATION_NAME_EXTENSION = ".name";
		private static final String EXPORTING_NOTHING_MESSAGE = "Exporting nothing";
		private static final String EXPORTING_SUBTYPES_MESSAGE = "Exporting input %s with subtypes: %b";
		private static final String EXPORTING_SUBTYPES_FAILED_MESSAGE = "Could not export subtypes of: %s";

		@Nonnull
		private final IForgeRegistry<Item> itemRegistry;
		@Nonnull
		private final export.json.ExportMode mode;

		/**
		 * @param itemRegistry used to retrieve the name of the item
		 * @param mode extracts more or less information from item depending on mode
		 */
		ItemRowExtractFunction( @Nonnull final IForgeRegistry<Item> itemRegistry, @Nonnull final export.json.ExportMode mode )
		{
			this.itemRegistry = Preconditions.checkNotNull( itemRegistry );
			this.mode = Preconditions.checkNotNull( mode );
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
			final boolean hasSubtypes = input.getHasSubtypes();
			if( hasSubtypes )
			{
				final CreativeTabs creativeTab = input.getCreativeTab();
				final NonNullList<ItemStack> stacks = NonNullList.create();

				// modifies the stacks list and adds the different sub types to it
				try
				{
					input.getSubItems( creativeTab, stacks );
				}
				catch( final Exception ignored )
				{
					//ModBingoLog.warn( EXPORTING_SUBTYPES_FAILED_MESSAGE, input.getUnlocalizedName() );
					//ModBingoLog.debug( ignored );

					// ignore if mods do bullshit in their code
					return null;
				}

				// list can be empty, no clue why
				if( stacks.isEmpty() )
				{
					return null;
				}

				final Joiner newLineJoiner = Joiner.on( '\n' );
				final Joiner typeJoiner = newLineJoiner.skipNulls();
				final List<String> transformedTypes = Lists.transform( stacks, new TypeExtractFunction( itemName, this.mode ) );

				return typeJoiner.join( transformedTypes );
			}

			final List<String> joinedBlockAttributes = Lists.newArrayListWithCapacity( 5 );
			final String unlocalizedItem = input.getUnlocalizedName();
			final String localization = I18n.translateToLocal( unlocalizedItem + LOCALIZATION_NAME_EXTENSION );

			joinedBlockAttributes.add( itemName );
			joinedBlockAttributes.add( localization );

			if( this.mode == export.json.ExportMode.VERBOSE )
			{
				final Block block = Block.getBlockFromItem( input );
				final boolean isBlock = block != Blocks.AIR && !block.equals( Blocks.AIR );
				final Class<? extends Item> itemClass = input.getClass();
				final String itemClassName = itemClass.getName();

				joinedBlockAttributes.add( unlocalizedItem );
				joinedBlockAttributes.add( Boolean.toString( isBlock ) );
				joinedBlockAttributes.add( itemClassName );
			}

			final Joiner csvJoiner = Joiner.on( ", " );
			final Joiner csvJoinerIgnoringNulls = csvJoiner.skipNulls();

			return csvJoinerIgnoringNulls.join( joinedBlockAttributes );
		}
	}
}
