package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.class)
public class ModelFeatureRendererMixin {
    @Shadow
    @Final
    private PoseStack poseStack;

    @Inject(method = "prepareModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V"), cancellable = true)
    private <S> void figura$beforeSetupAnim(ModelFeatureRenderer.Submit<S> submit, CallbackInfo ci) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension)(Object) submit;
        if (callBackExtension.figura$getPreventAnimSetup()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "prepareModel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"),
            cancellable = true
    )
    private <S> void figura$preRender(ModelFeatureRenderer.Submit<S> submit, CallbackInfo ci) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension)(Object) submit;

        if (submit.model() instanceof PlayerModel && submit.state() instanceof AvatarRenderState playerState) {
            Avatar avatar = AvatarManager.getAvatar((EntityRenderState)playerState);
            if (avatar != null
                    && avatar.renderer != null
                    && avatar.renderer.hasMainModel()
                    && (avatar.luaRuntime == null || !avatar.luaRuntime.vanilla_model.PLAYER.checkVisible())) {
                ci.cancel();
            }
        }

        for (Function<PoseStack, Boolean> callback : callBackExtension.figura$getPreRenderingCallbacks()) {
            if (!callback.apply(this.poseStack)) {
                ci.cancel();
            }
        }

        if (ci.isCancelled()) {
            for (Runnable callback : callBackExtension.figura$getPostRenderingCallbacks()) {
                callback.run();
            }
            callBackExtension.figura$getPostRenderingCallbacks().clear();
        }

        callBackExtension.figura$getPreRenderingCallbacks().clear();
    }

    @Inject(
            method = "prepareModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    shift = Shift.AFTER
            )
    )
    private <S> void figura$postRender(ModelFeatureRenderer.Submit<S> submit, CallbackInfo ci) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension)(Object) submit;
        for (Runnable callback : callBackExtension.figura$getPostRenderingCallbacks()) {
            callback.run();
        }
        callBackExtension.figura$getPostRenderingCallbacks().clear();
    }
}