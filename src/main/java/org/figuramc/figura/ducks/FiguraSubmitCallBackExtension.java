package org.figuramc.figura.ducks;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Function;

public interface FiguraSubmitCallBackExtension {
    void figura$addPreRenderingCallback(Function<PoseStack, Boolean> var1);

    default List<Function<PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
        return List.of();
    }

    void figura$addPostRenderingCallback(Runnable var1);

    default List<Runnable> figura$getPostRenderingCallbacks() {
        return List.of();
    }

    default boolean figura$getPreventAnimSetup() {
        return false;
    }

    default void figura$setPreventAnimSetup(boolean prevent) {
    }
}