package com.snagtype.modbingo;

import com.snagtype.modbingo.commands.CreateBingoCommand;
import com.snagtype.modbingo.commands.ToggleFreeSpaceCommand;
import export.json.ExportConfig;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;
import export.json.JsonExportProcess;
import export.json.ForgeExportConfig;

import java.io.File;

@Mod(modid = BingoModRandomizor.MODID, name = BingoModRandomizor.NAME, version = BingoModRandomizor.VERSION)
public class BingoModRandomizor
{
    private File configDirectory;
    private ExportConfig exportConfig;
    //private ExportConfig testExportConfig;
    private BingoAdvancementConfig bingoConfig;
    public static final String MODID = "modbingo";
    public static final String NAME = "Mod Bingo";
    public static final String VERSION = "1.0.0";
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        this.configDirectory = new File(event.getModConfigurationDirectory().getPath(), "BingoMod");final File recipeFile = new File( this.configDirectory, "CustomRecipes.cfg" );

        final Configuration recipeConfiguration = new Configuration(recipeFile);
        final File configFile = new File(this.configDirectory, "Bingo.cfg" );
        final Configuration bingoConfiguration = new Configuration(configFile);
        bingoConfig = new BingoAdvancementConfig(bingoConfiguration);
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
        event.registerServerCommand(new CreateBingoCommand(this.bingoConfig));
        event.registerServerCommand(new ToggleFreeSpaceCommand(this.bingoConfig));
    }

}