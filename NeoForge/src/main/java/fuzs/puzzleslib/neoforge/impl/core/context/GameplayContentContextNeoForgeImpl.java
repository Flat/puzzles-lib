package fuzs.puzzleslib.neoforge.impl.core.context;

import com.google.common.base.Preconditions;
import fuzs.puzzleslib.common.api.core.v1.context.GameplayContentContext;
import fuzs.puzzleslib.common.api.data.v3.core.DataProviderContext;
import fuzs.puzzleslib.neoforge.api.data.v3.core.DataProviderBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Transformable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

import java.util.*;

public final class GameplayContentContextNeoForgeImpl implements GameplayContentContext {
    private final Map<Holder<? extends ItemLike>, ResourceKey<ContextIntProvider>> furnaceFuels = new LinkedHashMap<>();
    private final Map<Holder<? extends ItemLike>, ResourceKey<ContextIntProvider>> compostables = new LinkedHashMap<>();
    private final Map<Holder<Block>, Flammable> flammables = new LinkedHashMap<>();
    private final DataMapBuilder<Transformable> transformables;
    private final DataMapBuilder<Oxidizable> oxidizables;
    private final DataMapBuilder<Waxable> waxables;
    private final IEventBus eventBus;

    public GameplayContentContextNeoForgeImpl(String modId, IEventBus eventBus) {
        this.eventBus = eventBus;
        this.transformables = new DataMapBuilder<>(modId, NeoForgeDataMaps.TRANSFORMABLES);
        this.oxidizables = new DataMapBuilder<>(modId, NeoForgeDataMaps.OXIDIZABLES);
        this.waxables = new DataMapBuilder<>(modId, NeoForgeDataMaps.WAXABLES);
    }

    @Override
    public void registerFuel(Holder<? extends ItemLike> fuelItem, ResourceKey<ContextIntProvider> fuelValue) {
        Objects.requireNonNull(fuelItem, "fuel item is null");
        Objects.requireNonNull(fuelValue, "fuel value is null");
        if (this.furnaceFuels.isEmpty()) {
            this.eventBus.addListener((final ModifyDefaultComponentsEvent event) -> {
                this.furnaceFuels.forEach((Holder<? extends ItemLike> holder, ResourceKey<ContextIntProvider> key) -> {
                    event.modify(holder.value(),
                            (DataComponentMap.Builder builder, HolderLookup.Provider context, Item item) -> {
                                builder.set(DataComponents.COOKING_FUEL,
                                        new CookingFuel(key, ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER));
                            });
                });
            });
        }

        this.furnaceFuels.put(fuelItem, fuelValue);
    }

    @Override
    public void registerFlammable(Holder<Block> flammableBlock, int encouragement, int flammability) {
        Preconditions.checkArgument(encouragement > 0, "encouragement is non-positive");
        Preconditions.checkArgument(flammability > 0, "flammability is non-positive");
        Objects.requireNonNull(flammableBlock, "flammable block is null");
        if (this.flammables.isEmpty()) {
            this.eventBus.addListener((final FMLCommonSetupEvent event) -> {
                event.enqueueWork(() -> {
                    this.flammables.forEach((Holder<Block> holder, Flammable flammable) -> {
                        ((FireBlock) Blocks.FIRE).setFlammable(holder.value(),
                                flammable.encouragement(),
                                flammable.flammability());
                    });
                });
            });
        }

        this.flammables.put(flammableBlock, new Flammable(encouragement, flammability));
    }

    @Override
    public void registerCompostable(Holder<? extends ItemLike> compostableItem, ResourceKey<ContextIntProvider> compostingChance) {
        Objects.requireNonNull(compostableItem, "compostable item is null");
        Objects.requireNonNull(compostingChance, "composting chance is null");
        if (this.compostables.isEmpty()) {
            this.eventBus.addListener((final ModifyDefaultComponentsEvent event) -> {
                this.compostables.forEach((Holder<? extends ItemLike> holder, ResourceKey<ContextIntProvider> key) -> {
                    event.modify(holder.value(),
                            (DataComponentMap.Builder builder, HolderLookup.Provider context, Item item) -> {
                                builder.set(DataComponents.COMPOSTABLE, new Compostable(key));
                            });
                });
            });
        }

        this.compostables.put(compostableItem, compostingChance);
    }

    @Override
    public void registerStrippable(Holder<Block> unstrippedBlock, Holder<Block> strippedBlock) {
        Objects.requireNonNull(unstrippedBlock, "unstripped block is null");
        Objects.requireNonNull(strippedBlock, "stripped block is null");
        this.transformables.register(unstrippedBlock, strippedBlock, (Holder<Block> input, Holder<Block> output) -> {
            return Transformable.stripping(input.value(), output.value());
        });
    }

    @Override
    public void registerFlattenable(Holder<Block> unflattenedBlock, Holder<Block> flattenedBlock) {
        Objects.requireNonNull(unflattenedBlock, "unflattened block is null");
        Objects.requireNonNull(flattenedBlock, "flattened block is null");
        this.transformables.register(unflattenedBlock, flattenedBlock, (Holder<Block> input, Holder<Block> output) -> {
            return Transformable.flattening(input.value(), output.value());
        });
    }

    @Override
    public void registerTillable(Holder<Block> untilledBlock, Holder<Block> tilledBlock) {
        Objects.requireNonNull(untilledBlock, "untilled block is null");
        Objects.requireNonNull(tilledBlock, "tilled block is null");
        this.transformables.register(untilledBlock, tilledBlock, (Holder<Block> input, Holder<Block> output) -> {
            return Transformable.tilling(input.value(), output.value());
        });
    }

    @Override
    public void registerOxidizable(Holder<Block> unoxidizedBlock, Holder<Block> oxidizedBlock) {
        Objects.requireNonNull(unoxidizedBlock, "unoxidized block is null");
        Objects.requireNonNull(oxidizedBlock, "oxidized block is null");
        this.oxidizables.register(unoxidizedBlock, oxidizedBlock, (Holder<Block> _, Holder<Block> output) -> {
            return new Oxidizable(output.value());
        });
    }

    @Override
    public void registerWaxable(Holder<Block> unwaxedBlock, Holder<Block> waxedBlock) {
        Objects.requireNonNull(unwaxedBlock, "unwaxed block is null");
        Objects.requireNonNull(waxedBlock, "waxed block is null");
        this.waxables.register(unwaxedBlock, waxedBlock, (Holder<Block> _, Holder<Block> output) -> {
            return new Waxable(output.value());
        });
    }

    private record Flammable(int encouragement, int flammability) {

    }

    static class DataMapBuilder<T> {
        private final List<BlockTransform<T>> values = new ArrayList<>();
        private final String modId;
        private final DataProviderContext.Factory factory;

        DataMapBuilder(String modId, DataMapType<Block, T> type) {
            this.modId = modId;
            this.factory = (DataProviderContext context) -> {
                return new DataMapProvider(context.getPackOutput(), context.getRegistries()) {
                    @Override
                    protected void gather(HolderLookup.Provider registries) {
                        Builder<T, Block> builder = this.builder(type);
                        for (BlockTransform<T> transform : DataMapBuilder.this.values) {
                            transform.build(builder);
                        }
                    }

                    @Override
                    public String getName() {
                        return super.getName() + " for " + ResourceKey.create(type.registryKey(), type.id());
                    }
                };
            };
        }

        void register(Holder<Block> input, Holder<Block> output, BlockTransformResult<T> result) {
            if (this.values.isEmpty()) {
                DataProviderBuilder.of(this.modId, this.factory);
            }

            this.values.add(new BlockTransform<>(input, output, result));
        }

        record BlockTransform<T>(Holder<Block> input, Holder<Block> output, BlockTransformResult<T> result) {
            void build(DataMapProvider.Builder<T, Block> builder) {
                T result = this.result().createResult(this.input(), this.output());
                builder.add(this.input(), result, false);
            }
        }

        @FunctionalInterface
        interface BlockTransformResult<T> {
            T createResult(Holder<Block> input, Holder<Block> output);
        }
    }
}
