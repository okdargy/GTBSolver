package party.dargy.gtbsolver.mixin;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import party.dargy.gtbsolver.GTBSolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public class GuiNewChatMixin {
    
    @Inject(method = "printChatMessage", at = @At("HEAD"))
    private void onChatMessage(IChatComponent chatComponent, CallbackInfo ci) {
        if (chatComponent != null && GTBSolver.config != null && GTBSolver.config.buildBattleHud != null) {
            String message = chatComponent.getUnformattedText();
            GTBSolver.config.buildBattleHud.onChatMessage(message);
        }
    }
}
