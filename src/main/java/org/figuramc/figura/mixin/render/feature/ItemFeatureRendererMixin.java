package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {

    @ModifyVariable(method = "buildGroup", at = @At("HEAD"), argsOnly = true)
    private List<ItemFeatureRenderer.Submit> figura$runPreCallbacks(List<ItemFeatureRenderer.Submit> submits) {
        List<ItemFeatureRenderer.Submit> result = new ArrayList<>(submits.size());
        for (ItemFeatureRenderer.Submit submit : submits) {
            FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension)(Object) submit;
            PoseStack poseStack = figura$poseStackFromSubmit(submit);

            boolean cancel = false;
            for (Function<PoseStack, Boolean> callback : callBackExtension.figura$getPreRenderingCallbacks()) {
                if (!callback.apply(poseStack)) {
                    cancel = true;
                }
            }
            callBackExtension.figura$getPreRenderingCallbacks().clear();

            if (cancel) {
                for (Runnable r : callBackExtension.figura$getPostRenderingCallbacks()) {
                    r.run();
                }
                callBackExtension.figura$getPostRenderingCallbacks().clear();
            } else {
                result.add(submit);
            }
        }
        return result;
    }

    @Inject(method = "buildGroup", at = @At("RETURN"))
    private void figura$runPostCallbacks(FeatureFrameContext context, List<ItemFeatureRenderer.Submit> submits, CallbackInfo ci) {
        for (ItemFeatureRenderer.Submit submit : submits) {
            FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension)(Object) submit;
            for (Runnable callback : callBackExtension.figura$getPostRenderingCallbacks()) {
                callback.run();
            }
            callBackExtension.figura$getPostRenderingCallbacks().clear();
        }
    }

    private PoseStack figura$poseStackFromSubmit(ItemFeatureRenderer.Submit submit) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(submit.pose());
        return poseStack;
    }
}