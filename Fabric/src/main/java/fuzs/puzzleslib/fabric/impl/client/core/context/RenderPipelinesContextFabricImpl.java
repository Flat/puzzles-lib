package fuzs.puzzleslib.fabric.impl.client.core.context;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import fuzs.puzzleslib.common.api.client.core.v1.context.RenderPipelinesContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;

import java.util.Objects;

public final class RenderPipelinesContextFabricImpl implements RenderPipelinesContext {
    @Override
    public void registerPipeline(RenderPipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline is null");
        RenderPipelines.register(pipeline);
    }

    @Override
    public void registerOptionalPipeline(RenderPipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline is null");
        RenderPipelines.registerOptional(pipeline);
    }

    @Override
    public void registerOitPipelineSet(OitPipelineSet pipelineSet) {
        Objects.requireNonNull(pipelineSet, "pipeline set is null");
        RenderPipelines.register(pipelineSet);
    }
}
