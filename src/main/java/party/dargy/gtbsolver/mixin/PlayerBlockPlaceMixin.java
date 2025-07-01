package party.dargy.gtbsolver.mixin;

import net.minecraft.util.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class PlayerBlockPlaceMixin {

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void onSetBlockState(BlockPos pos, IBlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("Block set at " + pos + " with " + state.getBlock().getLocalizedName());
    }
}
