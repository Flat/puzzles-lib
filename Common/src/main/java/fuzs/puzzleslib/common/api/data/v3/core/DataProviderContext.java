package fuzs.puzzleslib.common.api.data.v3.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A context class used for {@link DataProvider DataProviders}.
 * <p>
 * Offers similar capabilities as NeoForge's {@code net.neoforged.neoforge.data.event.GatherDataEvent}.
 */
public interface DataProviderContext {
    /**
     * @return the generating mod id
     */
    String getModId();

    /**
     * @return the pack output
     */
    PackOutput getPackOutput();

    /**
     * @return the input paths provided via {@code --input}
     */
    Collection<Path> getInputs();

    /**
     * @return the full registry lookup provider
     */
    CompletableFuture<HolderLookup.Provider> getRegistries();

    /**
     * @return the world registries lookup provider; only to be used when the full registry lookup provider is not
     *         suitable
     *
     * @see VanillaRegistries#WORLD_BUILDER
     */
    @Deprecated
    CompletableFuture<HolderLookup.Provider> getWorldRegistries();

    /**
     * @return the client resource manager
     */
    @Nullable ResourceManager getClientResources();

    /**
     * @return the server resource manager
     */
    @Nullable ResourceManager getServerResources();

    /**
     * A simple shortcut for a data provider factory requiring this context.
     */
    @FunctionalInterface
    interface Factory extends Function<DataProviderContext, DataProvider> {
        // NO-OP
    }
}
