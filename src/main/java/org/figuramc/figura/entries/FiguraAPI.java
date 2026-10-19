package org.figuramc.figura.entries;

import java.util.Collection;
import org.figuramc.figura.avatar.Avatar;

public interface FiguraAPI {
   FiguraAPI build(Avatar var1);

   String getName();

   Collection<Class<?>> getWhitelistedClasses();

   Collection<Class<?>> getDocsClasses();
}
