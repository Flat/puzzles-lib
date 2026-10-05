package fuzs.puzzleslib.common.api.event.v1.server;

import fuzs.puzzleslib.common.api.event.v1.core.EventInvoker;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

@FunctionalInterface
public interface ModifyEnchantmentsCallback {
    EventInvoker<ModifyEnchantmentsCallback> EVENT = EventInvoker.lookup(ModifyEnchantmentsCallback.class);

    /**
     * Runs for every enchantment upon loading, allows for modifying the enchantment.
     *
     * @param resourceKey    the enchantment id
     * @param builder        the enchantment builder instance, containing the data of the original enchantment
     * @param lookupProvider the registry lookup
     */
    void onModifyEnchantment(ResourceKey<Enchantment> resourceKey, Enchantment.Builder builder, HolderGetter.Provider lookupProvider);
}
