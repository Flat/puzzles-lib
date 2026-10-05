package fuzs.puzzleslib.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableValue;
import fuzs.puzzleslib.fabric.api.event.v1.FabricLivingEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StartAttacking.class)
abstract class StartAttackingFabricMixin {

    @ModifyArg(method = "lambda$create$3(Lnet/minecraft/world/entity/ai/behavior/StartAttacking$StartAttackingCondition;Lnet/minecraft/world/entity/ai/behavior/StartAttacking$TargetFinder;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;J)Z",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V"))
    private static Object create(Object target, @Local(argsOnly = true) Mob mob, @Cancellable CallbackInfoReturnable<Boolean> callback) {
        MutableValue<@Nullable LivingEntity> targetValue = MutableValue.fromValue((LivingEntity) target);
        EventResult result = FabricLivingEvents.LIVING_CHANGE_TARGET.invoker().onLivingChangeTarget(mob, targetValue);
        // This is specifically used for setting a new target only, not for clearing an existing one; so we need to handle the null case as well.
        if (result.isInterrupt() || targetValue.get() == null) {
            callback.setReturnValue(false);
        }

        return targetValue.get();
    }
}
