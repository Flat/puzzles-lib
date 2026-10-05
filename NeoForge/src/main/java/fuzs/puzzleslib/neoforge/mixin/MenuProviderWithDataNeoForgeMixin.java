package fuzs.puzzleslib.neoforge.mixin;

import fuzs.puzzleslib.common.api.container.v1.MenuProviderWithData;
import fuzs.puzzleslib.neoforge.impl.event.NeoForgeEventImplHelper;
import fuzs.puzzleslib.neoforge.impl.init.MenuTypeWithData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MenuProviderWithData.class)
public interface MenuProviderWithDataNeoForgeMixin<T> extends MenuProvider {
    @Override
    default void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        Player player = NeoForgeEventImplHelper.getPlayerFromContainerMenu(menu);
        MenuTypeWithData.encodeMenuData(menu, buffer, this.getMenuData((ServerPlayer) player));
    }

    @Shadow
    T getMenuData(@Nullable ServerPlayer serverPlayer);
}
