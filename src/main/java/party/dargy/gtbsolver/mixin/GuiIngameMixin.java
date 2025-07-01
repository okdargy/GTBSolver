package party.dargy.gtbsolver.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import party.dargy.gtbsolver.GTBSolver;
import party.dargy.gtbsolver.config.GTBConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class GuiIngameMixin {
    
    private IChatComponent lastActionBarMessage = null;
    
    @Inject(method = "setRecordPlaying(Lnet/minecraft/util/IChatComponent;Z)V", at = @At("HEAD"))
    private void onActionBarChange(IChatComponent message, boolean isPlaying, CallbackInfo ci) {
        if (GTBConfig.actionBarNotifications) {
            if (message != null && !message.equals(lastActionBarMessage)) {
                lastActionBarMessage = message;

                Minecraft mc = Minecraft.getMinecraft();
                if (mc.thePlayer != null) {
                    String actionBarText = message.getUnformattedText();
                    ChatComponentText privateMessage = new ChatComponentText("§7[Action Bar] §f" + actionBarText);
                    mc.thePlayer.addChatMessage(privateMessage);
                }
            } else if (message == null && lastActionBarMessage != null) {
                lastActionBarMessage = null;
                
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.thePlayer != null) {
                    ChatComponentText privateMessage = new ChatComponentText("§7[Action Bar Cleared]");
                    mc.thePlayer.addChatMessage(privateMessage);
                }
            }
        }

        if (message != null && GTBSolver.config != null && GTBSolver.config.buildBattleHud != null) {
            String actionBarText = message.getUnformattedText();
            GTBSolver.config.buildBattleHud.onActionBarMessage(actionBarText);
        }
    }
    
    @Inject(method = "setRecordPlaying(Ljava/lang/String;Z)V", at = @At("HEAD"))
    private void onActionBarChangeString(String message, boolean isPlaying, CallbackInfo ci) {
        if (GTBConfig.actionBarNotifications) {
            if (message != null && !message.isEmpty()) {
                IChatComponent component = new ChatComponentText(message);
                if (!component.equals(lastActionBarMessage)) {
                    lastActionBarMessage = component;
                    
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc.thePlayer != null) {
                        ChatComponentText privateMessage = new ChatComponentText("§7[Action Bar] §f" + message);
                        mc.thePlayer.addChatMessage(privateMessage);
                    }
                }
            }
        }

        if (message != null && !message.isEmpty() && GTBSolver.config != null && GTBSolver.config.buildBattleHud != null) {
            GTBSolver.config.buildBattleHud.onActionBarMessage(message);
        }
    }
}
