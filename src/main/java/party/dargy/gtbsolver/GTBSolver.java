package party.dargy.gtbsolver;

import party.dargy.gtbsolver.command.OpenGUICommand;
import party.dargy.gtbsolver.config.GTBConfig;
import cc.polyfrost.oneconfig.events.event.InitializationEvent;
import net.minecraftforge.fml.common.Mod;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = GTBSolver.MODID, name = GTBSolver.NAME, version = GTBSolver.VERSION)
public class GTBSolver {

    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    @Mod.Instance(MODID)
    public static GTBSolver INSTANCE;
    public static GTBConfig config;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        config = new GTBConfig();
        CommandManager.INSTANCE.registerCommand(new OpenGUICommand());
    }
}
