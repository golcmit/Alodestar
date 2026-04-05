package com.golcmit.alodestar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.level.levelgen.Xoroshiro128PlusPlus;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;



@Mixin(Xoroshiro128PlusPlus.class)
public class Astarize {


    @Inject(
            method = "nextLong",
            at = @At("RETURN"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void atarastar(
            CallbackInfoReturnable<Long> a,
            long i,
            long j,
            long k
    ) {
        //base **  a.setReturnValue(Long.rotateLeft(i * 5, 7) * 9);
        a.setReturnValue(
                0x85EBCA6B08122321L+
                        (Long.rotateLeft(j * 0x9E3779B97F4A7C15L, 17) * 0xFF51AFD7ED558CCDL) ^
                        (Long.rotateLeft(i * 0xC4CEB9FE1A85EC53L, 31) * 0x6364136223846793L)
        );
    }
}

