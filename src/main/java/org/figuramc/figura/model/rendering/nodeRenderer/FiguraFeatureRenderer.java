package org.figuramc.figura.model.rendering.nodeRenderer;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

@Environment(EnvType.CLIENT)
public class FiguraFeatureRenderer extends RenderTypeFeatureRenderer<FiguraFeatureRenderer.Submit> {
    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Figura");

    @Override
    protected void buildGroup(final FeatureFrameContext context, final List<Submit> submits) {
        SubmitNodeCollector collector = new SubmitNodeStorage() {
            @Override
            public void submitCustomGeometry(com.mojang.blaze3d.vertex.PoseStack stack, net.minecraft.client.renderer.rendertype.RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
                com.mojang.blaze3d.vertex.VertexConsumer consumer = FiguraFeatureRenderer.this.getVertexBuilder(renderType);
                renderer.render(stack.last(), consumer);
            }
        };

        for (Submit submit : submits) {
            for (FiguraSubmission figuraSubmission : submit.submissions()) {
                if (figuraSubmission.avatar() != null) {
                    figuraSubmission.renderer().apply(figuraSubmission.avatar(), figuraSubmission.renderState(), collector);
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static record Submit(List<FiguraSubmission> submissions) implements SubmitNode {
        @Override
        public FeatureRendererType<Submit> featureType() {
            return FiguraFeatureRenderer.TYPE;
        }
    }
}