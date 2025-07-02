package party.dargy.gtbsolver.config;

import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import party.dargy.gtbsolver.GTBSolver;
import party.dargy.gtbsolver.hud.BuildBattleHud;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class GTBConfig extends Config {

    @JsonName("build.battle.hud")
    @HUD(
            name = "Build Battle Helper"
    )
    public BuildBattleHud buildBattleHud = new BuildBattleHud();

    @JsonName("build.battle.helper")
    @Switch(
            name = "Build Battle Helper",
            description = "Automatically guess words in Build Battle based on action bar clues"
    )
    public static boolean buildBattleHelper = true;

    @JsonName("auto.send.best.guess")
    @Switch(
            name = "Auto Send Best Guess",
            description = "Automatically sends the best guess in chat, only when there is one possible word left"
    )
    public static boolean autoSendBestGuess = true;

    @JsonName("auto.send.delay")
    @Slider(
            name = "Auto Send Delay",
            description = "Delay before automatically sending the best guess (in ms)",
            min = 0, max = 5000,
            step = 500
    )
    public static float autoSendDelay = 1000;

    @JsonName("action.bar.notifications")
    @Switch(
            name = "Action Bar Notifications",
            description = "Send chat messages when the action bar changes"
    )
    public static boolean actionBarNotifications = false;

    @JsonName("debug.mode")
    @Switch(
            name = "Debug Mode",
            description = "Print debug information to your chat"
    )
    public static boolean debugMode = false;

    public GTBConfig() {
        super(new Mod(GTBSolver.NAME, ModType.UTIL_QOL), GTBSolver.MODID + ".json");
        initialize();
    }
}

