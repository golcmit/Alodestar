package com.golcmit.alodestar.mixin;



import com.golcmit.alodestar.slime.DeterministicSlimeRandom;
import com.golcmit.alodestar.slime.SlimeClusterCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldgenRandom.class)
public class MixinSlimeChunk {
    @Inject(method = "seedSlimeChunk", at = @At("HEAD"), cancellable = true)
    private static void injectSlimeChunk(int chunkX, int chunkZ, long worldSeed, long salt, CallbackInfoReturnable<RandomSource> cir) {
        boolean isSlime = SlimeClusterCache.isSlimeChunk(chunkX, chunkZ, worldSeed);

        // 生成済みの定数インスタンスを返すだけで即時終了
        cir.setReturnValue(isSlime ? DeterministicSlimeRandom.TRUE_INSTANCE : DeterministicSlimeRandom.FALSE_INSTANCE);
    }
}
