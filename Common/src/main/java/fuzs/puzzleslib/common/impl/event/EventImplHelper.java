package fuzs.puzzleslib.common.impl.event;

import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableDouble;
import fuzs.puzzleslib.common.api.event.v1.entity.living.LivingJumpCallback;
import fuzs.puzzleslib.common.impl.core.proxy.ProxyImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;

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

    public static @Nullable Player getPlayerFromContainerMenu(AbstractContainerMenu abstractContainerMenu) {
        for (Slot slot : abstractContainerMenu.slots) {
            if (slot.container instanceof Inventory inventory) {
                return inventory.player;
            }
        }

        MinecraftServer server = ProxyImpl.get().getMinecraftServer();
        if (server != null) {
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                if (serverPlayer.containerMenu == abstractContainerMenu) {
                    return serverPlayer;
                }
            }
        }

        return null;
    }

    public static Map.@Nullable Entry<GrindstoneMenu, Player> getGrindstoneMenuFromInputs(ItemStack primaryItemStack, ItemStack secondaryItemStack) {
        MinecraftServer minecraftServer = ProxyImpl.get().getMinecraftServer();
        if (minecraftServer != null) {
            for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
                if (serverPlayer.containerMenu instanceof GrindstoneMenu grindstoneMenu) {
                    if (grindstoneMenu.getSlot(0).getItem() == primaryItemStack
                            && grindstoneMenu.getSlot(1).getItem() == secondaryItemStack) {
                        return Map.entry(grindstoneMenu, serverPlayer);
                    }
                }
            }
        }

        return null;
    }
}
