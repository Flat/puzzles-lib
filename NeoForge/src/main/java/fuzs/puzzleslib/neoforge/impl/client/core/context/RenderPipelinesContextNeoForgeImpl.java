package fuzs.puzzleslib.neoforge.impl.client.core.context;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import fuzs.puzzleslib.common.api.client.core.v1.context.RenderPipelinesContext;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import java.util.Objects;

public record RenderPipelinesContextNeoForgeImpl(RegisterRenderPipelinesEvent event) implements RenderPipelinesContext {
    @Override
    public void registerPipeline(RenderPipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline is null");
        this.event.registerPipeline(pipeline);
    }

    @Override
    public void registerOptionalPipeline(RenderPipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline is null");
        this.event.registerOptionalPipeline(pipeline);
    }

    @Override
    public void registerOitPipelineSet(OitPipelineSet pipelineSet) {
        Objects.requireNonNull(pipelineSet, "pipeline set is null");
        this.event.registerOitPipelineSet(pipelineSet);
    }
}
