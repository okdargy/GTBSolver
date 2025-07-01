package party.dargy.gtbsolver.command;

import party.dargy.gtbsolver.GTBSolver;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;

@Command(value = GTBSolver.MODID, description = "Access the " + GTBSolver.NAME + " GUI.")
public class OpenGUICommand {
    @Main
    private void handle() {
        GTBSolver.INSTANCE.config.openGui();
    }
}