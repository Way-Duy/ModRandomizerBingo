package com.snagtype.modbingo;

import com.google.common.collect.Lists;
import export.json.ExportConfig;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Logger;
import export.json.JsonExportProcess;
import export.json.ForgeExportConfig;
import export.json.ExportConfig;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

@Mod(modid = BingoModRandomizer.MODID, name = BingoModRandomizer.NAME, version = BingoModRandomizer.VERSION)
public class BingoModRandomizer
{
    private File configDirectory;
    private ExportConfig exportConfig;
    public static final String MODID = "modbingo";
    public static final String NAME = "Mod Bingo";
    public static final String VERSION = "1.0.0";
    private  ExportConfig config;
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        this.configDirectory = new File(event.getModConfigurationDirectory().getPath(), "BingoMod");
        final File recipeFile = new File( this.configDirectory, "CustomRecipes.cfg" );
        final Configuration recipeConfiguration = new Configuration(recipeFile);
        this.exportConfig = new ForgeExportConfig (recipeConfiguration );

    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        final JsonExportProcess process = new JsonExportProcess(this.configDirectory, this.exportConfig);
        final Thread exportProcessThread = new Thread( process);
        this.startService( "BingoMod Json Export", exportProcessThread);

    }
    private void startService( final String serviceName, final Thread thread )
    {
        thread.setName( serviceName );
        thread.setPriority( Thread.MIN_PRIORITY );

        logger.info( "Starting " + serviceName );
        thread.start();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event){
        event.registerServerCommand(new CreateBingoCommand());
    }

}
