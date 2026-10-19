package org.figuramc.figura.compat;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.compat.wrappers.ClassWrapper;
import org.figuramc.figura.compat.wrappers.FieldWrapper;
import org.figuramc.figura.compat.wrappers.MethodWrapper;

public class SimpleVCCompat {
   private static ClassWrapper ClientManager;
   private static FieldWrapper renderEventsField;
   private static ClassWrapper RenderEvents;
   private static MethodWrapper onRenderName;

   public static void init() {
      ClientManager = new ClassWrapper("de.maxhenkel.voicechat.voice.client.ClientManager");
      renderEventsField = ClientManager.getField("renderEvents");
      RenderEvents = new ClassWrapper("de.maxhenkel.voicechat.voice.client.RenderEvents");
      onRenderName = RenderEvents.getMethod("onRenderName", EntityRenderState.class, Component.class, PoseStack.class, SubmitNodeCollector.class, int.class);
   }

   public static void renderSimpleVCIcon(EntityRenderState state, Component text, PoseStack stack, SubmitNodeCollector multiBufferSource, int light) {
      if (ClientManager.isLoaded && renderEventsField.exists() && onRenderName.exists()) {
         onRenderName.invoke(renderEventsField.getValue(ClientManager.getMethod("instance").invoke(null)), state, text, stack, multiBufferSource, light);
      }
   }
}
