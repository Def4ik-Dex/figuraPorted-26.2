package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Locale;
import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FiguraItemModelResolverExtension;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(name = "ItemTask", value = "item_task")
public class ItemTask extends RenderTask {
   private ItemStack item;
   private ItemDisplayContext displayMode = ItemDisplayContext.NONE;
   private boolean left = false;
   private int cachedComplexity;

   public ItemTask(String name, Avatar owner, FiguraModelPart parent) {
      super(name, owner, parent);
   }

   @Override
   public void render(PoseStack poseStack, SubmitNodeCollector buffer, int light, int overlay) {
      poseStack.scale(-16.0F, 16.0F, -16.0F);
      LivingEntity entity = this.owner.renderer.entity instanceof LivingEntity living ? living : null;
      int newLight = this.customization.light != null ? this.customization.light : light;
      int newOverlay = this.customization.overlay != null ? this.customization.overlay : overlay;
      if (entity != null) {
         int seed = entity.getId() + this.displayMode.ordinal();
      } else {
         int seed = 0;
      }

      RenderUtils.renderStatic(entity, this.item, this.displayMode, poseStack, buffer, newLight, newOverlay);
   }

   @Override
   public int getComplexity() {
      return this.cachedComplexity;
   }

   @Override
   public boolean shouldRender() {
      return super.shouldRender() && this.item != null && !this.item.isEmpty();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "item"),
            @LuaMethodOverload(argumentTypes = ItemStackAPI.class, argumentNames = "item")
      },
      aliases = "item",
      value = "item_task.set_item"
   )
   public ItemTask setItem(Object item) {
      this.item = LuaUtils.parseItemStack("item", item);
      Minecraft client = Minecraft.getInstance();
      RandomSource random = client.level != null ? client.level.getRandom() : RandomSource.create();
      this.cachedComplexity = ((FiguraItemModelResolverExtension)client.getItemModelResolver()).figura$getModelComplexity(this.item, random);
      return this;
   }

   @LuaWhitelist
   public ItemTask item(Object item) {
      return this.setItem(item);
   }

   @LuaWhitelist
   @LuaMethodDoc("item_task.get_display_mode")
   public String getDisplayMode() {
      return this.displayMode.name();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "displayMode"),
      aliases = "displayMode",
      value = "item_task.set_display_mode"
   )
   public ItemTask setDisplayMode(@LuaNotNil String mode) {
      try {
         this.displayMode = ItemDisplayContext.valueOf(mode.toUpperCase(Locale.US));
         this.left = this.displayMode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || this.displayMode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
         return this;
      } catch (Exception ignored) {
         throw new LuaError("Illegal display mode: \"" + mode + "\".");
      }
   }

   @LuaWhitelist
   public ItemTask displayMode(@LuaNotNil String mode) {
      return this.setDisplayMode(mode);
   }

   @Override
   public String toString() {
      return this.name + " (Item Render Task)";
   }
}
