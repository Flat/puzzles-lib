package fuzs.puzzleslib.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.puzzleslib.common.api.event.v1.data.MutableValue;
import fuzs.puzzleslib.fabric.api.event.v1.FabricLivingEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(Monster.class)
abstract class MonsterFabricMixin extends PathfinderMob {

    protected MonsterFabricMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getProjectile", at = @At("RETURN"))
    public ItemStack getProjectile(ItemStack projectile, ItemStack heldWeapon) {
        if (heldWeapon.getItem() instanceof ProjectileWeaponItem) {
            MutableValue<ItemStack> projectileValue = MutableValue.fromValue(projectile);
            FabricLivingEvents.PICK_PROJECTILE.invoker().onPickProjectile(this, heldWeapon, projectileValue);
            return Objects.requireNonNull(projectileValue.get(), "projectile is null");
        } else {
            return projectile;
        }
    }
}
