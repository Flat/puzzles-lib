package fuzs.puzzleslib.neoforge.api.event.v1;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for every enchantment upon loading, allows for modifying the enchantment.
 * <p>
 * This event is fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS main event bus}.
 */
public class ModifyEnchantmentsEvent extends Event {
    private final ResourceKey<Enchantment> resourceKey;
    private final Enchantment.Builder builder;
    private final HolderGetter.Provider lookupProvider;

    @ApiStatus.Internal
    public ModifyEnchantmentsEvent(ResourceKey<Enchantment> resourceKey, Enchantment.Builder builder, HolderGetter.Provider lookupProvider) {
        this.resourceKey = resourceKey;
        this.builder = builder;
        this.lookupProvider = lookupProvider;
    }

    /**
     * @return the enchantment id
     */
    public ResourceKey<Enchantment> getResourceKey() {
        return this.resourceKey;
    }

    /**
     * @return the enchantment builder instance, containing the data of the original enchantment
     */
    public Enchantment.Builder getBuilder() {
        return this.builder;
    }

    /**
     * @return the registry lookup
     */
    public HolderGetter.Provider getLookupProvider() {
        return this.lookupProvider;
    }
}
