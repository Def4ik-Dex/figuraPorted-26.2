package org.figuramc.figura.lua.api.action_wheel;

import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;

@LuaWhitelist
@LuaTypeDoc(name = "Action", value = "wheel_action")
public class Action {
   public static final FiguraVec3 HOVER_COLOR = FiguraVec3.of(1.0, 1.0, 1.0);
   public static final FiguraVec3 TOGGLE_COLOR = FiguraVec3.of(0.0, 1.0, 0.0);
   protected String title;
   protected String toggleTitle;
   protected ItemStack item;
   protected ItemStack hoverItem;
   protected ItemStack toggleItem;
   protected FiguraVec3 color;
   protected FiguraVec3 hoverColor;
   protected FiguraVec3 toggleColor;
   protected Action.TextureData texture;
   protected Action.TextureData hoverTexture;
   protected Action.TextureData toggleTexture;
   protected boolean toggled = false;
   @LuaWhitelist
   @LuaFieldDoc("wheel_action.left_click")
   public LuaFunction leftClick;
   @LuaWhitelist
   @LuaFieldDoc("wheel_action.right_click")
   public LuaFunction rightClick;
   @LuaWhitelist
   @LuaFieldDoc("wheel_action.toggle")
   public LuaFunction toggle;
   @LuaWhitelist
   @LuaFieldDoc("wheel_action.untoggle")
   public LuaFunction untoggle;
   @LuaWhitelist
   @LuaFieldDoc("wheel_action.scroll")
   public LuaFunction scroll;

   public void execute(Avatar avatar, boolean left) {
      LuaFunction function = left ? this.leftClick : this.rightClick;
      if (function != null) {
         avatar.run(function, avatar.tick, this);
      }

      if (left) {
         function = this.toggled ? (this.untoggle == null ? this.toggle : this.untoggle) : this.toggle;
         if (function != null) {
            this.toggled = !this.toggled;
            avatar.run(function, avatar.tick, this.toggled, this);
         }
      }
   }

   public void mouseScroll(Avatar avatar, double delta) {
      if (this.scroll != null) {
         avatar.run(this.scroll, avatar.tick, delta, this);
      }
   }

   public ItemStack getItem(boolean selected) {
      ItemStack ret = null;
      if (selected) {
         ret = this.hoverItem;
      }

      if (ret == null && this.toggled) {
         ret = this.toggleItem;
      }

      if (ret == null) {
         ret = this.item;
      }

      return ret;
   }

   public FiguraVec3 getColor(boolean selected) {
      if (selected) {
         return this.hoverColor == null ? HOVER_COLOR : this.hoverColor;
      } else if (this.toggled) {
         return this.toggleColor == null ? TOGGLE_COLOR : this.toggleColor;
      } else {
         return this.color;
      }
   }

   public Action.TextureData getTexture(boolean selected) {
      Action.TextureData ret = null;
      if (selected) {
         ret = this.hoverTexture;
      }

      if (ret == null && this.toggled) {
         ret = this.toggleTexture;
      }

      if (ret == null) {
         ret = this.texture;
      }

      return ret;
   }

   @LuaWhitelist
   @LuaMethodDoc("wheel_action.get_title")
   public String getTitle() {
      return this.toggled ? (this.toggleTitle == null ? this.title : this.toggleTitle) : this.title;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {@LuaMethodOverload, @LuaMethodOverload(argumentTypes = String.class, argumentNames = "title")},
      aliases = "title",
      value = "wheel_action.set_title"
   )
   public Action setTitle(String title) {
      this.title = title;
      return this;
   }

   @LuaWhitelist
   public Action title(String title) {
      return this.setTitle(title);
   }

   @LuaWhitelist
   @LuaMethodDoc("wheel_action.get_color")
   public FiguraVec3 getColor() {
      return this.color;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "color"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b"})
      },
      aliases = "color",
      value = "wheel_action.set_color"
   )
   public Action setColor(Object x, Double y, Double z) {
      this.color = LuaUtils.nullableVec3("color", x, y, z);
      return this;
   }

   @LuaWhitelist
   public Action color(Object x, Double y, Double z) {
      return this.setColor(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc("wheel_action.get_hover_color")
   public FiguraVec3 getHoverColor() {
      return this.hoverColor;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "color"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b"})
      },
      aliases = "hoverColor",
      value = "wheel_action.set_hover_color"
   )
   public Action setHoverColor(Object x, Double y, Double z) {
      this.hoverColor = LuaUtils.nullableVec3("hoverColor", x, y, z);
      return this;
   }

   @LuaWhitelist
   public Action hoverColor(Object x, Double y, Double z) {
      return this.setHoverColor(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = ItemStackAPI.class, argumentNames = "item"),
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "item")
      },
      aliases = "item",
      value = "wheel_action.set_item"
   )
   public Action setItem(Object item) {
      this.item = LuaUtils.parseItemStack("item", item);
      return this;
   }

   @LuaWhitelist
   public Action item(Object item) {
      return this.setItem(item);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = ItemStackAPI.class, argumentNames = "item"),
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "item")
      },
      aliases = "hoverItem",
      value = "wheel_action.set_hover_item"
   )
   public Action setHoverItem(Object item) {
      this.hoverItem = LuaUtils.parseItemStack("hoverItem", item);
      return this;
   }

   @LuaWhitelist
   public Action hoverItem(Object item) {
      return this.setHoverItem(item);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraTexture.class, argumentNames = "texture"),
            @LuaMethodOverload(argumentTypes = {FiguraTexture.class, Double.class, Double.class}, argumentNames = {"texture", "u", "v"}),
            @LuaMethodOverload(
               argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class},
               argumentNames = {"texture", "u", "v", "width", "height"}
            ),
            @LuaMethodOverload(
               argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class, Double.class},
               argumentNames = {"texture", "u", "v", "width", "height", "scale"}
            )
      },
      aliases = "texture",
      value = "wheel_action.set_texture"
   )
   public Action setTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
      this.texture = new Action.TextureData(texture, u, v, width, height, scale);
      return this;
   }

   @LuaWhitelist
   public Action texture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
      return this.setTexture(texture, u, v, width, height, scale);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraTexture.class, argumentNames = "texture"),
            @LuaMethodOverload(argumentTypes = {FiguraTexture.class, Double.class, Double.class}, argumentNames = {"texture", "u", "v"}),
            @LuaMethodOverload(
               argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class},
               argumentNames = {"texture", "u", "v", "width", "height"}
            ),
            @LuaMethodOverload(
               argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class, Double.class},
               argumentNames = {"texture", "u", "v", "width", "height", "scale"}
            )
      },
      aliases = "hoverTexture",
      value = "wheel_action.set_hover_texture"
   )
   public Action setHoverTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
      this.hoverTexture = new Action.TextureData(texture, u, v, width, height, scale);
      return this;
   }

   @LuaWhitelist
   public Action hoverTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
      return this.setHoverTexture(texture, u, v, width, height, scale);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "leftFunction"),
      aliases = "onLeftClick",
      value = "wheel_action.set_on_left_click"
   )
   public Action setOnLeftClick(LuaFunction function) {
      this.leftClick = function;
      return this;
   }

   @LuaWhitelist
   public Action onLeftClick(LuaFunction function) {
      return this.setOnLeftClick(function);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "rightFunction"),
      aliases = "onRightClick",
      value = "wheel_action.set_on_right_click"
   )
   public Action setOnRightClick(LuaFunction function) {
      this.rightClick = function;
      return this;
   }

   @LuaWhitelist
   public Action onRightClick(LuaFunction function) {
      return this.setOnRightClick(function);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "leftFunction"),
      aliases = "onToggle",
      value = "wheel_action.set_on_toggle"
   )
   public Action setOnToggle(LuaFunction function) {
      this.toggle = function;
      return this;
   }

   @LuaWhitelist
   public Action onToggle(LuaFunction function) {
      return this.setOnToggle(function);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "rightFunction"),
      aliases = "onUntoggle",
      value = "wheel_action.set_on_untoggle"
   )
   public Action setOnUntoggle(LuaFunction function) {
      this.untoggle = function;
      return this;
   }

   @LuaWhitelist
   public Action onUntoggle(LuaFunction function) {
      return this.setOnUntoggle(function);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "scrollFunction"),
      aliases = "onScroll",
      value = "wheel_action.set_on_scroll"
   )
   public Action setOnScroll(LuaFunction function) {
      this.scroll = function;
      return this;
   }

   @LuaWhitelist
   public Action onScroll(LuaFunction function) {
      return this.setOnScroll(function);
   }

   @LuaWhitelist
   @LuaMethodDoc("wheel_action.get_toggle_title")
   public String getToggleTitle() {
      return this.toggleTitle;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "title"),
      aliases = "toggleTitle",
      value = "wheel_action.set_toggle_title"
   )
   public Action setToggleTitle(String title) {
      this.toggleTitle = title;
      return this;
   }

   @LuaWhitelist
   public Action toggleTitle(String title) {
      return this.setToggleTitle(title);
   }

   @LuaWhitelist
   @LuaMethodDoc("wheel_action.get_toggle_color")
   public FiguraVec3 getToggleColor() {
      return this.toggleColor;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec3.class, argumentNames = "color"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class, Double.class}, argumentNames = {"r", "g", "b"})
      },
      aliases = "toggleColor",
      value = "wheel_action.set_toggle_color"
   )
   public Action setToggleColor(Object x, Double y, Double z) {
      this.toggleColor = LuaUtils.nullableVec3("toggleColor", x, y, z);
      return this;
   }

   @LuaWhitelist
   public Action toggleColor(Object x, Double y, Double z) {
      return this.setToggleColor(x, y, z);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = ItemStackAPI.class, argumentNames = "item"),
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "item")
      },
      aliases = "toggleItem",
      value = "wheel_action.set_toggle_item"
   )
   public Action setToggleItem(Object item) {
      this.toggleItem = LuaUtils.parseItemStack("toggleItem", item);
      return this;
   }

   @LuaWhitelist
   public Action toggleItem(Object item) {
      return this.setToggleItem(item);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = FiguraTexture.class, argumentNames = "texture"),
            @LuaMethodOverload(argumentTypes = {FiguraTexture.class, Double.class, Double.class}, argumentNames = {"texture", "u", "v"}),
            @LuaMethodOverload(
               argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class},
               argumentNames = {"texture", "u", "v", "width", "height"}
            ),
            @LuaMethodOverload(
               argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class, Double.class},
               argumentNames = {"texture", "u", "v", "width", "height", "scale"}
            )
      },
      aliases = "toggleTexture",
      value = "wheel_action.set_toggle_texture"
   )
   public Action setToggleTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
      this.toggleTexture = new Action.TextureData(texture, u, v, width, height, scale);
      return this;
   }

   @LuaWhitelist
   public Action toggleTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
      return this.setToggleTexture(texture, u, v, width, height, scale);
   }

   @LuaWhitelist
   @LuaMethodDoc("wheel_action.is_toggled")
   public boolean isToggled() {
      return this.toggled;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = @LuaMethodOverload(argumentTypes = Boolean.class, argumentNames = "bool"),
      aliases = "toggled",
      value = "wheel_action.set_toggled"
   )
   public Action setToggled(boolean bool) {
      this.toggled = bool;
      return this;
   }

   @LuaWhitelist
   public Action toggled(boolean bool) {
      return this.setToggled(bool);
   }

   @LuaWhitelist
   public Object __index(String arg) {
      if (arg == null) {
         return null;
      }

      return switch (arg) {
         case "leftClick" -> this.leftClick;
         case "rightClick" -> this.rightClick;
         case "toggle" -> this.toggle;
         case "untoggle" -> this.untoggle;
         case "scroll" -> this.scroll;
         default -> null;
      };
   }

   @LuaWhitelist
   public void __newindex(@LuaNotNil String key, Object value) {
      LuaFunction func = value instanceof LuaFunction f ? f : null;
      switch (key) {
         case "leftClick":
            this.leftClick = func;
            break;
         case "rightClick":
            this.rightClick = func;
            break;
         case "toggle":
            this.toggle = func;
            break;
         case "untoggle":
            this.untoggle = func;
            break;
         case "scroll":
            this.scroll = func;
            break;
         default:
            throw new LuaError("Cannot assign value on key \"" + key + "\"");
      }
   }

   @Override
   public String toString() {
      return this.title == null ? "Action Wheel Action" : "Action Wheel Action (" + this.title + ")";
   }

   public static class TextureData {
      public final FiguraTexture texture;
      public final double u;
      public final double v;
      public final double scale;
      public final int width;
      public final int height;

      public TextureData(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
         this.texture = texture;
         this.u = u;
         this.v = v;
         this.width = width == null ? texture.getWidth() : width;
         this.height = height == null ? texture.getHeight() : height;
         this.scale = scale == null ? 1.0 : scale;
      }
   }
}
