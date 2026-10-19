package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(name = "BlockTask", value = "block_task")
public class BlockTask extends RenderTask {
   private BlockState block;
   private int cachedComplexity;

   public BlockTask(String name, Avatar owner, FiguraModelPart parent) {
      super(name, owner, parent);
   }

   @Override
   public void render(PoseStack poseStack, SubmitNodeCollector buffer, int light, int overlay) {
      poseStack.scale(16.0F, 16.0F, 16.0F);
      int newLight = this.customization.light != null ? this.customization.light : light;
      int newOverlay = this.customization.overlay != null ? this.customization.overlay : overlay;
      Minecraft client = Minecraft.getInstance();
      BlockModelRenderState renderState = new BlockModelRenderState();
      new BlockModelResolver(client.getModelManager()).update(renderState, this.block, BlockDisplayContext.create());
      renderState.submit(poseStack, buffer, newLight, newOverlay, 0);
   }

   @Override
   public int getComplexity() {
      return this.cachedComplexity;
   }

   @Override
   public boolean shouldRender() {
      return super.shouldRender() && this.block != null && !this.block.isAir();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "block"),
            @LuaMethodOverload(argumentTypes = BlockStateAPI.class, argumentNames = "block")
      },
      aliases = "block",
      value = "block_task.set_block"
   )
   public BlockTask setBlock(Object block) {
      this.block = LuaUtils.parseBlockState("block", block);
      Minecraft client = Minecraft.getInstance();
      RandomSource random = client.level != null ? client.level.getRandom() : RandomSource.create();
      BlockStateModel blockModel = client.getModelManager().getBlockStateModelSet().get(this.block);
      List<BlockStateModelPart> parts = new ArrayList<>();
      blockModel.collectParts(random, parts);
      this.cachedComplexity = parts.stream().mapToInt(p -> p.getQuads(null).size()).sum();

      for (Direction dir : Direction.values()) {
         this.cachedComplexity = this.cachedComplexity + parts.stream().mapToInt(p -> p.getQuads(dir).size()).sum();
      }

      return this;
   }

   @LuaWhitelist
   public BlockTask block(Object block) {
      return this.setBlock(block);
   }

   @Override
   public String toString() {
      return this.name + " (Block Render Task)";
   }
}
