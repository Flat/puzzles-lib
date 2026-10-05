package fuzs.puzzleslib.common.api.event.v1.entity.living;

import fuzs.puzzleslib.common.api.event.v1.core.EventInvoker;
import fuzs.puzzleslib.common.api.event.v1.data.MutableDouble;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface CalculateLivingVisibilityCallback {
    EventInvoker<CalculateLivingVisibilityCallback> EVENT = EventInvoker.lookup(CalculateLivingVisibilityCallback.class);

    /**
     * Called in {@link LivingEntity#getVisibilityPercent(Entity)} when an entity is trying to be targeted by another
     * entity for applying a given percentage to the looking entity's original visibility range.
     *
     * @param livingEntity      the entity trying to be targeted
     * @param lookingEntity     the looking entity that is trying to target the other entity
     * @param visibilityPercent the visibility percentage multiplied with the looking entity's targeting range; must be
     *                          in a range from {@code 0.0} to {@code 10.0}
     */
    void onCalculateLivingVisibility(LivingEntity livingEntity, @Nullable Entity lookingEntity, MutableDouble visibilityPercent);
}
