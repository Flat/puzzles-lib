package fuzs.puzzleslib.common.impl.event;

import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableDouble;
import fuzs.puzzleslib.common.api.event.v1.entity.living.LivingJumpCallback;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class EventImplHelper {

    private EventImplHelper() {
        // NO-OP
    }

    public static void onLivingJump(LivingJumpCallback callback, LivingEntity entity) {
        Vec3 deltaMovement = entity.getDeltaMovement();
        double jumpPower = deltaMovement.y;
        MutableDouble jumpPowerValue = MutableDouble.fromValue(jumpPower);
        EventResult result = callback.onLivingJump(entity, jumpPowerValue);
        if (result.isInterrupt()) {
            entity.setDeltaMovement(deltaMovement.x, 0.0, deltaMovement.z);
        } else if (jumpPowerValue.getAsDouble() != jumpPower) {
            entity.setDeltaMovement(deltaMovement.x, jumpPowerValue.getAsDouble(), deltaMovement.z);
        }
    }
}
