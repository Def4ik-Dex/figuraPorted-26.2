package org.figuramc.figura.lua.api.event;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.figuramc.figura.entries.FiguraEvent;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMetamethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;

@LuaWhitelist
@LuaTypeDoc(name = "EventsAPI", value = "events")
public class EventsAPI {
   @LuaWhitelist
   @LuaFieldDoc("events.entity_init")
   public final LuaEvent ENTITY_INIT = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.tick")
   public final LuaEvent TICK = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.world_tick")
   public final LuaEvent WORLD_TICK = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.render")
   public final LuaEvent RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.post_render")
   public final LuaEvent POST_RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.world_render")
   public final LuaEvent WORLD_RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.post_world_render")
   public final LuaEvent POST_WORLD_RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.chat_send_message")
   public final LuaEvent CHAT_SEND_MESSAGE = new LuaEvent(true);
   @LuaWhitelist
   @LuaFieldDoc("events.chat_receive_message")
   public final LuaEvent CHAT_RECEIVE_MESSAGE = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.skull_render")
   public final LuaEvent SKULL_RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.mouse_scroll")
   public final LuaEvent MOUSE_SCROLL = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.mouse_move")
   public final LuaEvent MOUSE_MOVE = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.mouse_press")
   public final LuaEvent MOUSE_PRESS = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.key_press")
   public final LuaEvent KEY_PRESS = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.char_typed")
   public final LuaEvent CHAR_TYPED = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.use_item")
   public final LuaEvent USE_ITEM = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.arrow_render")
   public final LuaEvent ARROW_RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.trident_render")
   public final LuaEvent TRIDENT_RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.item_render")
   public final LuaEvent ITEM_RENDER = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.on_play_sound")
   public final LuaEvent ON_PLAY_SOUND = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.resource_reload")
   public final LuaEvent RESOURCE_RELOAD = new LuaEvent();
   @LuaWhitelist
   @LuaFieldDoc("events.totem")
   public final LuaEvent TOTEM = new LuaEvent();
   @LuaFieldDoc("events.damage")
   public final LuaEvent DAMAGE = new LuaEvent();
   private final Map<String, LuaEvent> events = new HashMap<>();
   private static final List<FiguraEvent> ENTRYPOINTS = new ArrayList<>();

   public EventsAPI() {
      this.events.put("ENTITY_INIT", this.ENTITY_INIT);
      this.events.put("TICK", this.TICK);
      this.events.put("WORLD_TICK", this.WORLD_TICK);
      this.events.put("RENDER", this.RENDER);
      this.events.put("POST_RENDER", this.POST_RENDER);
      this.events.put("WORLD_RENDER", this.WORLD_RENDER);
      this.events.put("POST_WORLD_RENDER", this.POST_WORLD_RENDER);
      this.events.put("CHAT_SEND_MESSAGE", this.CHAT_SEND_MESSAGE);
      this.events.put("CHAT_RECEIVE_MESSAGE", this.CHAT_RECEIVE_MESSAGE);
      this.events.put("SKULL_RENDER", this.SKULL_RENDER);
      this.events.put("MOUSE_SCROLL", this.MOUSE_SCROLL);
      this.events.put("MOUSE_MOVE", this.MOUSE_MOVE);
      this.events.put("MOUSE_PRESS", this.MOUSE_PRESS);
      this.events.put("KEY_PRESS", this.KEY_PRESS);
      this.events.put("CHAR_TYPED", this.CHAR_TYPED);
      this.events.put("USE_ITEM", this.USE_ITEM);
      this.events.put("ARROW_RENDER", this.ARROW_RENDER);
      this.events.put("TRIDENT_RENDER", this.TRIDENT_RENDER);
      this.events.put("ITEM_RENDER", this.ITEM_RENDER);
      this.events.put("ON_PLAY_SOUND", this.ON_PLAY_SOUND);
      this.events.put("RESOURCE_RELOAD", this.RESOURCE_RELOAD);
      this.events.put("TOTEM", this.TOTEM);
      this.events.put("DAMAGE", this.DAMAGE);

      for (FiguraEvent entrypoint : ENTRYPOINTS) {
         String ID = entrypoint.getID().toUpperCase(Locale.US);

         for (Pair<String, LuaEvent> event : entrypoint.getEvents()) {
            String name = ID + "." + ((String)event.getFirst()).toUpperCase(Locale.US);
            this.events.put(name, (LuaEvent)event.getSecond());
         }
      }
   }

   public static void initEntryPoints(Set<FiguraEvent> set) {
      ENTRYPOINTS.addAll(set);
   }

   @LuaWhitelist
   @LuaMethodDoc("events.get_events")
   public Map<String, LuaEvent> getEvents() {
      return this.events;
   }

   @LuaWhitelist
   @LuaMetamethodDoc(
      overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {LuaEvent.class, EventsAPI.class, String.class}, comment = "events.__index.comment1")
   )
   public LuaEvent __index(String key) {
      return key == null ? null : this.events.get(key.toUpperCase(Locale.US));
   }

   @LuaWhitelist
   public void __newindex(@LuaNotNil String key, LuaFunction func) {
      LuaEvent event = this.__index(key.toUpperCase(Locale.US));
      if (event != null) {
         event.register(func, null);
      } else {
         throw new LuaError("Cannot assign value on key \"" + key + "\"");
      }
   }

   @Override
   public String toString() {
      return "EventsAPI";
   }
}
