package fuzs.puzzleslib.neoforge.impl.client.core.context;

import fuzs.puzzleslib.common.api.client.core.v1.context.BlockStateResolverContext;
import fuzs.puzzleslib.common.api.client.renderer.v1.model.ModelLoadingHelper;
import fuzs.puzzleslib.common.impl.PuzzlesLib;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.block.LoadedBlockModels;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ParallelMapTransform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BlockStateResolverContextNeoForgeImpl implements BlockStateResolverContext {
    private final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
    private final MaterialBaker materialBaker;
    private final ResolvedModel missingModel;
    private final Function<Identifier, @Nullable ResolvedModel> modelResolver;
    private final BiConsumer<Identifier, ResolvedModel> modelCache;
    private final BiConsumer<BlockState, BlockStateModel> blockStateModelOutput;
    private final Function<MaterialBaker, ModelBaker> modelBakerFactory = this::createModelBaker;

    public BlockStateResolverContextNeoForgeImpl(ModelEvent.ModifyBakingResult event) {
        this.materialBaker = event.getMaterialBaker();
        ModelBakery bakery = event.getModelBakery();
        this.missingModel = bakery.missingModel;
        Map<Identifier, ResolvedModel> resolvedModels = new HashMap<>();
        this.modelResolver = (Identifier id) -> {
            // The resolved models map from the bakery is unmodifiable when the ModernFix mod is installed.
            return bakery.resolvedModels.containsKey(id) ? bakery.resolvedModels.get(id) : resolvedModels.get(id);
        };
        this.modelCache = resolvedModels::putIfAbsent;
        this.blockStateModelOutput = event.getBakingResult().blockStateModels()::put;
    }

    private ModelBaker createModelBaker(MaterialBaker materials) {
        ModelBaker.Interner interner = new ModelBakery.InternerImpl();
        ModelBakery.MissingModels missingModels = ModelBakery.MissingModels.bake(this.missingModel,
                materials,
                interner);
        return new ModelBakerImpl(materials, interner, missingModels);
    }

    /**
     * @see ModelDiscovery#ModelDiscovery(Map, UnbakedModel)
     * @see ModelManager#discoverModelDependencies(Map, BlockStateModelLoader.LoadedModels,
     *         ClientItemInfoLoader.LoadedClientInfos)
     */
    @Override
    public void registerBlockStateResolver(Block block, Consumer<BiConsumer<BlockState, BlockStateModel.UnbakedRoot>> blockStateConsumer) {
        ModelDiscovery discovery = new ModelDiscovery(new HashMap<>(), this.missingModel.wrapped());
        discovery.uncachedResolver = (Object object) -> {
            Identifier id = (Identifier) object;
            ResolvedModel model = this.modelResolver.apply(id);
            if (model instanceof ModelDiscovery.ModelWrapper wrapper) {
                return wrapper;
            } else {
                UnbakedModel blockModel = ModelLoadingHelper.loadBlockModel(this.resourceManager, id);
                if (blockModel == null) {
                    PuzzlesLib.LOGGER.warn("Missing block model: {}", id);
                    return (ModelDiscovery.ModelWrapper) this.missingModel;
                } else {
                    return discovery.createAndQueueWrapper(id, blockModel);
                }
            }
        };
        Map<BlockState, BlockStateModel.UnbakedRoot> models = new HashMap<>();
        blockStateConsumer.accept((BlockState blockState, BlockStateModel.UnbakedRoot unbakedBlockStateModel) -> {
            discovery.addRoot(unbakedBlockStateModel);
            models.put(blockState, unbakedBlockStateModel);
        });
        discovery.resolve().forEach(this.modelCache);
        this.loadModels(models).forEach(this.blockStateModelOutput);
    }

    /**
     * @see ModelManager#loadModels(SpriteLoader.Preparations, SpriteLoader.Preparations, ModelBakery,
     *         LoadedBlockModels, Object2IntMap, EntityModelSet, Executor)
     */
    private Map<BlockState, BlockStateModel> loadModels(Map<BlockState, BlockStateModel.UnbakedRoot> models) {
        return bakeModels(models, this.modelBakerFactory.apply(this.materialBaker), Util.backgroundExecutor()).join();
    }

    @Override
    public <T> void registerBlockStateResolver(Block block, BiFunction<ResourceManager, Executor, CompletableFuture<T>> resourceLoader, BiConsumer<T, BiConsumer<BlockState, BlockStateModel.UnbakedRoot>> blockStateConsumer) {
        this.registerBlockStateResolver(block, (BiConsumer<BlockState, BlockStateModel.UnbakedRoot> consumer) -> {
            blockStateConsumer.accept(resourceLoader.apply(this.resourceManager, Util.backgroundExecutor()).join(),
                    consumer);
        });
    }

    /**
     * @see ModelBakery#bakeModels(MaterialBaker, Executor)
     */
    private static CompletableFuture<Map<BlockState, BlockStateModel>> bakeModels(Map<BlockState, BlockStateModel.UnbakedRoot> models, ModelBaker baker, Executor taskExecutor) {
        return ParallelMapTransform.schedule(models, (BlockState blockState, BlockStateModel.UnbakedRoot model) -> {
            try {
                return model.bake(blockState, baker);
            } catch (Exception exception) {
                PuzzlesLib.LOGGER.warn("Unable to bake model: '{}': {}", blockState, exception);
                return null;
            }
        }, taskExecutor);
    }

    /**
     * @see ModelBakery.ModelBakerImpl
     */
    private class ModelBakerImpl implements ModelBaker {
        private final MaterialBaker materials;
        private final ModelBaker.Interner interner;
        private final ModelBakery.MissingModels missingModels;
        private final Map<ModelBaker.SharedOperationKey<?>, Object> operationCache;
        private final Function<ModelBaker.SharedOperationKey<?>, Object> cacheComputeFunction;

        private ModelBakerImpl(MaterialBaker materials, ModelBaker.Interner interner, ModelBakery.MissingModels missingModels) {
            this.operationCache = new ConcurrentHashMap<>();
            this.cacheComputeFunction = (SharedOperationKey<?> key) -> key.compute(this);
            this.materials = materials;
            this.interner = interner;
            this.missingModels = missingModels;
        }

        @Override
        public BlockStateModelPart missingBlockModelPart() {
            return this.missingModels.blockPart();
        }

        @Override
        public MaterialBaker materials() {
            return this.materials;
        }

        @Override
        public ModelBaker.Interner interner() {
            return this.interner;
        }

        @Override
        public ResolvedModel getModel(Identifier id) {
            ResolvedModel model = BlockStateResolverContextNeoForgeImpl.this.modelResolver.apply(id);
            if (model == null) {
                PuzzlesLib.LOGGER.warn("Requested a model that was not discovered previously: {}", id);
                return BlockStateResolverContextNeoForgeImpl.this.missingModel;
            } else {
                return model;
            }
        }

        @Override
        public <T> T compute(ModelBaker.SharedOperationKey<T> key) {
            return (T) this.operationCache.computeIfAbsent(key, this.cacheComputeFunction);
        }
    }
}
