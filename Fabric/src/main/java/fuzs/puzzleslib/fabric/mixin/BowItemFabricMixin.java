package fuzs.puzzleslib.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Cancellable;
import fuzs.puzzleslib.common.api.event.v1.data.MutableInt;
import fuzs.puzzleslib.fabric.api.event.v1.FabricPlayerEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
abstract class BowItemFabricMixin extends ProjectileWeaponItem {

    public BowItemFabricMixin(Properties properties) {
        super(properties);
    }

    @ModifyVariable(method = "releaseUsing", at = @At("STORE"), ordinal = 1)
    public int releaseUsing(int timeHeld, ItemStack itemStack, Level level, LivingEntity entity, int remainingTime, @Cancellable CallbackInfoReturnable<Boolean> callback) {
        MutableInt timeHeldValue = MutableInt.fromValue(timeHeld);
        if (FabricPlayerEvents.ARROW_LOOSE.invoker()
                .onArrowLoose((Player) entity, itemStack, level, timeHeldValue)
                .isInterrupt()) {
            callback.setReturnValue(false);
        }

        return timeHeldValue.getAsInt();
    }
}
