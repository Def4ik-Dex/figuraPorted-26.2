package org.figuramc.figura.ducks;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;

public interface NodeCollectorExtension {
    <S extends EntityRenderState> void submitFiguraModel(
            Avatar var1, S var2, TriFunction<Avatar, S, SubmitNodeCollector, Void> var3
    );
}