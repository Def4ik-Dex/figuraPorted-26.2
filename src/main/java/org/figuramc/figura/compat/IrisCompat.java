package org.figuramc.figura.compat;

import java.lang.reflect.Method;
import org.figuramc.figura.model.rendering.texture.FiguraRenderTypes;

public class IrisCompat {
   public static void assignPipelinesToIrisPrograms() {
      try {
         Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
         Object api = apiClass.getMethod("getInstance").invoke(null);
         Class<? extends Enum> programClass = Class.forName("net.irisshaders.iris.api.v0.IrisProgram").asSubclass(Enum.class);
         Object entities = Enum.valueOf(programClass, "ENTITIES");
         Object pipeline = FiguraRenderTypes.FiguraRenderPipelines.FIGURA_SOLID;

         for (Method method : apiClass.getMethods()) {
            if (method.getName().equals("assignPipeline") && method.getParameterCount() == 2) {
               Class<?>[] parameters = method.getParameterTypes();
               if (parameters[0].isInstance(pipeline) && parameters[1].isInstance(entities)) {
                  method.invoke(api, pipeline, entities);
                  return;
               }
            }
         }
      } catch (ReflectiveOperationException | ClassCastException var10) {
      }
   }
}
