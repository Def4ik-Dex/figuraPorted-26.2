package org.figuramc.figura.entries;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.model.ParentType;

public interface FiguraVanillaPart {
   String getID();

   Collection<Pair<String, Function<EntityModel<?>, ModelPart>>> getParts();

   Collection<Pair<String, Pair<Function<EntityModel<?>, ModelPart>, ParentType>>> getPartsWithParent();
}
