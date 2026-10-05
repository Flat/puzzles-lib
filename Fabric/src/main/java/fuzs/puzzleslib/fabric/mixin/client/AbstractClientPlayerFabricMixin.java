package fuzs.puzzleslib.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import fuzs.puzzleslib.common.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.fabric.api.client.event.v1.FabricClientPlayerEvents;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractClientPlayer.class)
abstract class AbstractClientPlayerFabricMixin extends Player {

    public AbstractClientPlayerFabricMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @ModifyArg(method = "getFieldOfViewModifier",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"),
               index = 2)
    public float getFieldOfViewModifier(float fieldOfViewModifier, @Local(ordinal = 0,
                                                                          argsOnly = true) float effectScale) {
        // Do not fire the event when FOV effects don't apply due to the option being set to zero.
        if (effectScale == 0.0F) {
            return fieldOfViewModifier;
        }

        MutableFloat fieldOfViewModifierValue = MutableFloat.fromValue(fieldOfViewModifier);
        FabricClientPlayerEvents.COMPUTE_FOV_MODIFIER.invoker().onComputeFovModifier(this, fieldOfViewModifierValue);
        return fieldOfViewModifierValue.getAsFloat();
    }
}
