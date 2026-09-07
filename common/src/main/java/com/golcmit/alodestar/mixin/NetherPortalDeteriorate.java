package com.golcmit.alodestar.mixin;

import com.golcmit.alodestar.portal.SpatialTransform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public class NetherPortalDeteriorate {

    @Redirect(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/dimension/DimensionType;getTeleportationScale(Lnet/minecraft/world/level/dimension/DimensionType;Lnet/minecraft/world/level/dimension/DimensionType;)D"
            )
    )
    private double astaring$cancelVanillaScale(DimensionType from, DimensionType to) {
        return 1.0;
    }

    @ModifyArgs(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;clampToBounds(DDD)Lnet/minecraft/core/BlockPos;"
            )
    )
    private void astaring$rotateBeforeClamp(Args args) {
        // Entity自体がthisなので、そこからlevelを取得
        Level sourceLevel = ((Entity)(Object)this).level();
        long seed = ((ServerLevel) sourceLevel).getSeed();
        double theta = Math.PI / 6;

        double x = args.get(0);
        double z = args.get(2);

        SpatialTransform.Point result;
        if (sourceLevel.dimension() == Level.NETHER) {
            result = SpatialTransform.inverse(x, z, theta, seed);
        } else {
            result = SpatialTransform.forward(x, z, theta, seed);
        }

        args.set(0, result.x());
        args.set(2, result.z());
    }
}