package party.dargy.gtbsolver.config;

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
            name = "Build Battle Helper",
            description = "Automatically guess words in Build Battle based on action bar clues"
    )
    public static boolean buildBattleHelper = true;

    @Switch(
            name = "Build Battle Block Tracking",
            description = "Track blocks placed during Build Battle rounds and categorize them by theme"
    )
    public static boolean buildBattleBlockTracking = true;

    public GTBConfig() {
        super(new Mod(GTBSolver.NAME, ModType.UTIL_QOL), GTBSolver.MODID + ".json");
        initialize();
    }
}

