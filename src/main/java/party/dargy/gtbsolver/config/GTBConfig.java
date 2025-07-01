package party.dargy.gtbsolver.config;

import cc.polyfrost.oneconfig.config.annotations.Slider;
import party.dargy.gtbsolver.GTBSolver;
import party.dargy.gtbsolver.hud.BuildBattleHud;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class GTBConfig extends Config {

    @HUD(
            name = "Build Battle Helper"
    )
    public BuildBattleHud buildBattleHud = new BuildBattleHud();

    @Switch(
            name = "Action Bar Notifications",
            description = "Send chat messages when the action bar changes"
    )
    public static boolean actionBarNotifications = true;

    @Switch(
            name = "Debug Mode",
            description = "Print debug information to your chat"
    )
    public static boolean debugMode = true;

    @Switch(
            name = "Build Battle Helper",
            description = "Automatically guess words in Build Battle based on action bar clues"
    )
    public static boolean buildBattleHelper = true;

    @Switch(
            name = "Auto Send Best Guess",
            description = "Automatically sends the best guess in chat, only when there is one possible word left"
    )
    public static boolean autoSendBestGuess = true;

    @Slider(
            name = "Minimum Auto Send Delay",
            min = 0f, max = 5000f,
            step = 500
    )
    public static float minimumAutoSenDelay = 0f;

    @Slider(
            name = "Maximum Auto Send Delay",
            min = 0f, max = 5000f,
            step = 500
    )
    public static float maximumAutoSenDelay = 500f;

    public GTBConfig() {
        super(new Mod(GTBSolver.NAME, ModType.UTIL_QOL), GTBSolver.MODID + ".json");
        initialize();
    }
}

