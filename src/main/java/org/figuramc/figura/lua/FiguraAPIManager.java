package org.figuramc.figura.lua;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.entries.FiguraAPI;
import org.figuramc.figura.lua.api.AnimationAPI;
import org.figuramc.figura.lua.api.AvatarAPI;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.lua.api.ConfigAPI;
import org.figuramc.figura.lua.api.FileAPI;
import org.figuramc.figura.lua.api.HostAPI;
import org.figuramc.figura.lua.api.RaycastAPI;
import org.figuramc.figura.lua.api.RendererAPI;
import org.figuramc.figura.lua.api.TextureAPI;
import org.figuramc.figura.lua.api.TextureAtlasAPI;
import org.figuramc.figura.lua.api.action_wheel.Action;
import org.figuramc.figura.lua.api.action_wheel.ActionWheelAPI;
import org.figuramc.figura.lua.api.action_wheel.Page;
import org.figuramc.figura.lua.api.data.DataAPI;
import org.figuramc.figura.lua.api.data.FiguraBuffer;
import org.figuramc.figura.lua.api.data.FiguraFuture;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.api.data.FiguraOutputStream;
import org.figuramc.figura.lua.api.data.ResourcesAPI;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.entity.LivingEntityAPI;
import org.figuramc.figura.lua.api.entity.NullEntity;
import org.figuramc.figura.lua.api.entity.PlayerAPI;
import org.figuramc.figura.lua.api.entity.ViewerAPI;
import org.figuramc.figura.lua.api.event.EventsAPI;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.figuramc.figura.lua.api.json.FiguraJsonArray;
import org.figuramc.figura.lua.api.json.FiguraJsonBuilder;
import org.figuramc.figura.lua.api.json.FiguraJsonObject;
import org.figuramc.figura.lua.api.json.FiguraJsonSerializer;
import org.figuramc.figura.lua.api.json.JsonAPI;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.lua.api.keybind.KeybindAPI;
import org.figuramc.figura.lua.api.math.MatricesAPI;
import org.figuramc.figura.lua.api.math.VectorsAPI;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.lua.api.nameplate.NameplateAPI;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomizationGroup;
import org.figuramc.figura.lua.api.net.HttpRequestsAPI;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
import org.figuramc.figura.lua.api.particle.LuaParticle;
import org.figuramc.figura.lua.api.particle.ParticleAPI;
import org.figuramc.figura.lua.api.ping.PingAPI;
import org.figuramc.figura.lua.api.ping.PingFunction;
import org.figuramc.figura.lua.api.sound.LuaSound;
import org.figuramc.figura.lua.api.sound.SoundAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaGroupPart;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelPart;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.lua.api.world.BiomeAPI;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.matrix.FiguraMat2;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendertasks.BlockTask;
import org.figuramc.figura.model.rendertasks.EntityTask;
import org.figuramc.figura.model.rendertasks.ItemTask;
import org.figuramc.figura.model.rendertasks.RenderTask;
import org.figuramc.figura.model.rendertasks.SpriteTask;
import org.figuramc.figura.model.rendertasks.TextTask;

public class FiguraAPIManager {
   public static final Set<Class<?>> WHITELISTED_CLASSES = new HashSet<Class<?>>() {
      {
         this.add(FiguraVec2.class);
         this.add(FiguraVec3.class);
         this.add(FiguraVec4.class);
         this.add(FiguraMat2.class);
         this.add(FiguraMat3.class);
         this.add(FiguraMat4.class);
         this.add(NullEntity.class);
         this.add(EntityAPI.class);
         this.add(LivingEntityAPI.class);
         this.add(PlayerAPI.class);
         this.add(ViewerAPI.class);
         this.add(EventsAPI.class);
         this.add(LuaEvent.class);
         this.add(Vertex.class);
         this.add(FiguraModelPart.class);
         this.add(RenderTask.class);
         this.add(ItemTask.class);
         this.add(BlockTask.class);
         this.add(EntityTask.class);
         this.add(TextTask.class);
         this.add(SpriteTask.class);
         this.add(SoundAPI.class);
         this.add(LuaSound.class);
         this.add(ParticleAPI.class);
         this.add(LuaParticle.class);
         this.add(VanillaModelAPI.class);
         this.add(VanillaPart.class);
         this.add(VanillaGroupPart.class);
         this.add(VanillaModelPart.class);
         this.add(KeybindAPI.class);
         this.add(FiguraKeybind.class);
         this.add(NameplateAPI.class);
         this.add(NameplateCustomization.class);
         this.add(EntityNameplateCustomization.class);
         this.add(NameplateCustomizationGroup.class);
         this.add(ActionWheelAPI.class);
         this.add(Page.class);
         this.add(Action.class);
         this.add(VectorsAPI.class);
         this.add(MatricesAPI.class);
         this.add(WorldAPI.class);
         this.add(BiomeAPI.class);
         this.add(BlockStateAPI.class);
         this.add(ItemStackAPI.class);
         this.add(PingAPI.class);
         this.add(PingFunction.class);
         this.add(TextureAPI.class);
         this.add(FiguraTexture.class);
         this.add(AnimationAPI.class);
         this.add(Animation.class);
         this.add(HostAPI.class);
         this.add(RendererAPI.class);
         this.add(ClientAPI.class);
         this.add(AvatarAPI.class);
         this.add(ConfigAPI.class);
         this.add(TextureAtlasAPI.class);
         this.add(FiguraInputStream.class);
         this.add(FiguraOutputStream.class);
         this.add(DataAPI.class);
         this.add(FiguraBuffer.class);
         this.add(FileAPI.class);
         this.add(JsonAPI.class);
         this.add(FiguraJsonBuilder.class);
         this.add(FiguraJsonSerializer.class);
         this.add(FiguraJsonArray.class);
         this.add(FiguraJsonObject.class);
         this.add(ResourcesAPI.class);
         this.add(NetworkingAPI.class);
         this.add(HttpRequestsAPI.class);
         this.add(HttpRequestsAPI.HttpRequestBuilder.class);
         this.add(HttpRequestsAPI.HttpResponse.class);
         this.add(FiguraFuture.class);
         this.add(RaycastAPI.class);
      }
   };
   public static final Map<String, Function<FiguraLuaRuntime, Object>> API_GETTERS = new LinkedHashMap<String, Function<FiguraLuaRuntime, Object>>() {
      {
         this.put("events", r -> r.events = new EventsAPI());
         this.put("sounds", r -> new SoundAPI(r.owner));
         this.put("vanilla_model", r -> r.vanilla_model = new VanillaModelAPI(r.owner));
         this.put("keybinds", r -> r.keybinds = new KeybindAPI(r.owner));
         this.put("host", r -> r.host = new HostAPI(r.owner));
         this.put("nameplate", r -> r.nameplate = new NameplateAPI());
         this.put("renderer", r -> r.renderer = new RendererAPI(r.owner));
         this.put("action_wheel", r -> r.action_wheel = new ActionWheelAPI(r.owner));
         this.put("animations", r -> new AnimationAPI(r.owner));
         this.put("client", r -> ClientAPI.INSTANCE);
         this.put("particles", r -> new ParticleAPI(r.owner));
         this.put("avatar", r -> r.avatar_meta = new AvatarAPI(r.owner));
         this.put("vectors", r -> VectorsAPI.INSTANCE);
         this.put("matrices", r -> MatricesAPI.INSTANCE);
         this.put("world", r -> WorldAPI.INSTANCE);
         this.put("pings", r -> r.ping = new PingAPI(r.owner));
         this.put("textures", r -> r.texture = new TextureAPI(r.owner));
         this.put("config", r -> new ConfigAPI(r.owner));
         this.put("data", r -> new DataAPI(r.owner));
         this.put("file", r -> new FileAPI(r.owner));
         this.put("json", r -> JsonAPI.INSTANCE);
         this.put("resources", r -> new ResourcesAPI(r.owner));
         this.put("net", r -> new NetworkingAPI(r.owner));
         this.put("raycast", r -> new RaycastAPI(r.owner));
      }
   };
   private static final Set<FiguraAPI> ENTRYPOINTS = new HashSet<>();

   public static void initEntryPoints(Set<FiguraAPI> set) {
      for (FiguraAPI api : set) {
         ENTRYPOINTS.add(api);
         WHITELISTED_CLASSES.addAll(api.getWhitelistedClasses());
      }
   }

   public static void setupTypesAndAPIs(FiguraLuaRuntime runtime) {
      for (Class<?> clazz : WHITELISTED_CLASSES) {
         runtime.registerClass(clazz);
      }

      for (Entry<String, Function<FiguraLuaRuntime, Object>> entry : API_GETTERS.entrySet()) {
         runtime.setGlobal(entry.getKey(), entry.getValue().apply(runtime));
      }

      for (FiguraAPI api : ENTRYPOINTS) {
         String name = api.getName();
         if (name != null) {
            runtime.setGlobal(name, api.build(runtime.owner));
         }
      }
   }
}
