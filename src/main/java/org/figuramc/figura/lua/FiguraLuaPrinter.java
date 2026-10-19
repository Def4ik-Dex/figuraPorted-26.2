package org.figuramc.figura.lua;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.world.entity.EntityType;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class FiguraLuaPrinter {
   public static DecimalFormat df;
   private static final Function<FiguraLuaRuntime, LuaValue> PRINT_FUNCTION = runtime -> new VarArgFunction() {
      public Varargs invoke(Varargs args) {
         if (!Configs.LOG_OTHERS.value && !FiguraMod.isLocal(runtime.owner.owner)) {
            return NIL;
         }

         MutableComponent text = Component.empty();

         for (int i = 0; i < args.narg(); i++) {
            text.append(FiguraLuaPrinter.getPrintText(runtime.typeManager, args.arg(i + 1), true, false)).append("\t");
         }

         FiguraLuaPrinter.sendLuaMessage(text, runtime.owner.entityName);
         return LuaValue.valueOf(text.getString());
      }

      public String tojstring() {
         return "function: print";
      }
   };
   private static final Function<FiguraLuaRuntime, LuaValue> PRINT_JSON_FUNCTION = runtime -> new VarArgFunction() {
      public Varargs invoke(Varargs args) {
         boolean local = FiguraMod.isLocal(runtime.owner.owner);
         if (!Configs.LOG_OTHERS.value && !local) {
            return NIL;
         }

         TextUtils.allowScriptEvents = true;
         MutableComponent text = Component.empty();

         for (int i = 0; i < args.narg(); i++) {
            String jsonString = args.arg(i + 1).tojstring();
            System.out.println(jsonString);
            text.append(TextUtils.tryParseJson(jsonString));
         }

         TextUtils.allowScriptEvents = false;
         if (!local) {
            FiguraLuaPrinter.sendLuaChatMessage(TextUtils.removeClickableObjects(text));
         } else {
            FiguraLuaPrinter.sendLuaChatMessage(text);
         }

         return LuaValue.valueOf(text.getString());
      }

      public String tojstring() {
         return "function: printJson";
      }
   };
   private static final Function<FiguraLuaRuntime, LuaValue> PRINT_TABLE_FUNCTION = runtime -> new VarArgFunction() {
      public Varargs invoke(Varargs args) {
         if (!Configs.LOG_OTHERS.value && !FiguraMod.isLocal(runtime.owner.owner)) {
            return NIL;
         }

         boolean silent = false;
         MutableComponent text = Component.empty();
         if (args.narg() > 0) {
            int depth = args.arg(2).isnumber() ? args.arg(2).checkint() : 1;
            text.append(FiguraLuaPrinter.tableToText(runtime.typeManager, args.arg(1), depth, 1, true));
            silent = args.arg(3).isboolean() && args.arg(3).checkboolean();
         }

         if (!silent) {
            FiguraLuaPrinter.sendLuaMessage(text, runtime.owner.entityName);
         }

         return LuaValue.valueOf(text.getString());
      }

      public String tojstring() {
         return "function: printTable";
      }
   };
   private static final LinkedList<Component> chatQueue = new LinkedList<>();
   private static final int MAX_CHARS_QUEUED = 10000000;
   private static int charsQueued = 0;
   private static final int MAX_CHARS_PER_TICK = 10000;

   public static void updateDecimalFormatting() {
      int config = Configs.LOG_NUMBER_LENGTH.value;
      df = new DecimalFormat("0" + (config > 0 ? "." + "#".repeat(config) : ""));
      df.setRoundingMode(RoundingMode.DOWN);
   }

   public static void loadPrintFunctions(FiguraLuaRuntime runtime) {
      LuaValue print = PRINT_FUNCTION.apply(runtime);
      runtime.setGlobal("print", print);
      runtime.setGlobal("log", print);
      LuaValue printJson = PRINT_JSON_FUNCTION.apply(runtime);
      runtime.setGlobal("printJson", printJson);
      runtime.setGlobal("logJson", printJson);
      LuaValue printTable = PRINT_TABLE_FUNCTION.apply(runtime);
      runtime.setGlobal("printTable", printTable);
      runtime.setGlobal("logTable", printTable);
   }

   public static void sendLuaMessage(Object message, String owner) {
      MutableComponent component = Component.empty()
         .append(Component.literal("[lua] ").withStyle(ColorUtils.Colors.LUA_LOG.style))
         .append(Component.literal(owner))
         .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_LOG.style))
         .append((Component)(message instanceof Component c ? c : Component.literal(message.toString())))
         .append(Component.literal("\n"));
      if (Configs.LOG_LOCATION.value == 0) {
         sendLuaChatMessage(component);
      } else {
         FiguraMod.LOGGER.info(component.getString());
      }
   }

   public static void sendLuaError(LuaError error, Avatar owner) {
      String message = error.toString()
         .replace("org.luaj.vm2.LuaError: ", "")
         .replace("\n\t[Java]: in ?", "")
         .replace("'<eos>' expected", "Expected end of script");
      if (owner.minify) {
         message = message + "\nscript:\n\tscript heavily minified! - cannot look for line numbers!";
      } else {
         try {
            String[] split = message.split(":", 2);
            if (split.length > 1 && owner.luaRuntime != null) {
               String left = "[string \"";
               int sub = split[0].indexOf(left);
               String name = sub == -1 ? split[0] : split[0].substring(sub + left.length(), split[0].indexOf("\"]"));
               String src = owner.luaRuntime.scripts.get(name);
               if (src != null) {
                  int line = Integer.parseInt(split[1].split("\\D", 2)[0]);
                  String str = src.split("\n")[line - 1].trim();
                  if (str.length() > 96) {
                     str = str.substring(0, 96) + " [...]";
                  }

                  message = message + "\nscript:\n\t" + str;
               }
            }
         } catch (Exception var10) {
         }
      }

      MutableComponent component = Component.empty()
         .append(Component.literal("[error] ").withStyle(ColorUtils.Colors.LUA_ERROR.style))
         .append(Component.literal(owner.entityName))
         .append(Component.literal(" : " + message).withStyle(ColorUtils.Colors.LUA_ERROR.style))
         .append(Component.literal("\n"));
      owner.errorText = TextUtils.replaceTabs(Component.literal(message).withStyle(ColorUtils.Colors.LUA_ERROR.style));
      if ((owner.entityType != null || Configs.LOG_OTHERS.value || FiguraMod.isLocal(owner.owner))
         && owner.permissions.getCategory() != Permissions.Category.BLOCKED) {
         chatQueue.offer(component);
         FiguraMod.LOGGER.error("", error);
      }
   }

   public static void sendPingMessage(Avatar owner, String ping, int size, LuaValue[] args) {
      int config = Configs.LOG_PINGS.value;
      if (config != 0 && (config != 1 || owner.isHost)) {
         MutableComponent text = Component.empty()
            .append(Component.literal("[ping] ").withStyle(ColorUtils.Colors.LUA_PING.style))
            .append(Component.literal(owner.entityName))
            .append(Component.literal(" : ").withStyle(ColorUtils.Colors.LUA_PING.style))
            .append(ping)
            .append(Component.literal(" :: ").withStyle(ColorUtils.Colors.LUA_PING.style))
            .append(size + " bytes")
            .append(Component.literal(" :: ").withStyle(ColorUtils.Colors.LUA_PING.style));

         for (LuaValue arg : args) {
            text.append(getPrintText(owner.luaRuntime.typeManager, arg, true, false)).append("\t");
         }

         text.append(Component.literal("\n"));
         if (Configs.LOG_LOCATION.value == 0) {
            sendLuaChatMessage(text);
         } else {
            FiguraMod.LOGGER.info(text.getString());
         }
      }
   }

   private static Component tableToText(LuaTypeManager typeManager, LuaValue value, int depth, int indent, boolean hasTooltip) {
      if (value.isuserdata()) {
         return userdataToText(typeManager, value, depth, indent, hasTooltip);
      }

      if (value.istable() && depth > 0) {
         MutableComponent text = Component.empty()
            .append(Component.literal("table:").withStyle(getTypeColor(value)))
            .append(Component.literal(" {\n").withStyle(ChatFormatting.GRAY));
         String spacing = "\t".repeat(indent - 1);
         LuaTable table = value.checktable();

         for (LuaValue key : table.keys()) {
            text.append(getTableEntry(typeManager, spacing, key, table.get(key), hasTooltip, depth, indent));
         }

         text.append(spacing).append(Component.literal("}").withStyle(ChatFormatting.GRAY));
         return text;
      } else {
         return getPrintText(typeManager, value, hasTooltip, true);
      }
   }

   private static Component userdataToText(LuaTypeManager typeManager, LuaValue value, int depth, int indent, boolean hasTooltip) {
      if (value.isuserdata() && depth > 0) {
         MutableComponent text = Component.empty()
            .append(Component.literal("userdata:").withStyle(getTypeColor(value)))
            .append(Component.literal(" {\n").withStyle(ChatFormatting.GRAY));
         String spacing = "\t".repeat(indent - 1);
         Object data = value.checkuserdata();
         Class<?> clazz = data.getClass();
         if (clazz.isAnnotationPresent(LuaWhitelist.class)) {
            Set<String> fields = new HashSet<>();

            for (Field field : clazz.getFields()) {
               String name = field.getName();
               if (field.isAnnotationPresent(LuaWhitelist.class) && !fields.contains(name)) {
                  try {
                     Object obj = field.get(data);
                     text.append(getTableEntry(typeManager, spacing, LuaValue.valueOf(name), typeManager.javaToLua(obj).arg1(), hasTooltip, depth, indent));
                     fields.add(name);
                  } catch (Exception e) {
                     FiguraMod.LOGGER.error("", e);
                  }
               }
            }

            Set<String> methods = new HashSet<>();

            for (Method method : clazz.getMethods()) {
               String name = method.getName();
               if (method.isAnnotationPresent(LuaWhitelist.class) && !name.startsWith("__") && !methods.contains(name)) {
                  text.append(getTableEntry(typeManager, spacing, LuaValue.valueOf(name), typeManager.getWrapper(method), hasTooltip, depth, indent));
                  methods.add(name);
               }
            }
         }

         text.append(spacing).append(Component.literal("}").withStyle(ChatFormatting.GRAY));
         return text;
      } else {
         return getPrintText(typeManager, value, hasTooltip, true);
      }
   }

   private static MutableComponent getTableEntry(
      LuaTypeManager typeManager, String spacing, LuaValue key, LuaValue value, boolean hasTooltip, int depth, int indent
   ) {
      MutableComponent text = Component.empty().append(spacing).append("\t");
      text.append(Component.literal("[").withStyle(ChatFormatting.GRAY))
         .append(getPrintText(typeManager, key, hasTooltip, true))
         .append(Component.literal("] = ").withStyle(ChatFormatting.GRAY));
      if (!value.istable() && !value.isuserdata()) {
         text.append(getPrintText(typeManager, value, hasTooltip, true));
      } else {
         text.append(tableToText(typeManager, value, depth - 1, indent + 1, hasTooltip));
      }

      text.append("\n");
      return text;
   }

   private static MutableComponent getPrintText(LuaTypeManager typeManager, LuaValue value, boolean hasTooltip, boolean quoteStrings) {
      String ret;
      if (!(value instanceof LuaString) && value.isnumber()) {
         Double d = value.checkdouble();
         ret = d == Math.rint(d) ? value.tojstring() : df.format(d);
      } else {
         ret = value.tojstring();
         if (value.isstring() && quoteStrings) {
            ret = "\"" + ret + "\"";
         }
      }

      MutableComponent text = Component.literal(ret).withStyle(getTypeColor(value));
      if (hasTooltip && (value.istable() || value.isuserdata())) {
         Component table = TextUtils.replaceTabs(tableToText(typeManager, value, 1, 1, false));
         text.withStyle(Style.EMPTY.withHoverEvent(new ShowText(table)));
      }

      return text;
   }

   private static Style getTypeColor(LuaValue value) {
      if (value.istable()) {
         return ColorUtils.Colors.AWESOME_BLUE.style;
      } else if (!(value instanceof LuaString) && value.isnumber()) {
         return ColorUtils.Colors.FIGURA_BLUE.style;
      } else if (value.isnil()) {
         return ColorUtils.Colors.LUA_ERROR.style;
      } else if (value.isboolean()) {
         return ColorUtils.Colors.LUA_PING.style;
      } else if (value.isfunction()) {
         return Style.EMPTY.withColor(ChatFormatting.GREEN);
      } else if (value.isuserdata()) {
         return Style.EMPTY.withColor(ChatFormatting.YELLOW);
      } else {
         return value.isthread() ? Style.EMPTY.withColor(ChatFormatting.GOLD) : Style.EMPTY.withColor(ChatFormatting.WHITE);
      }
   }

   private static void sendLuaChatMessage(Component message) throws LuaError {
      charsQueued = charsQueued + message.getString().length();
      if (charsQueued > 10000000) {
         chatQueue.clear();
         charsQueued = 0;
         throw new LuaError("Chat overflow: printing too much!");
      }

      chatQueue.offer(message);
   }

   public static void clearPrintQueue() {
      chatQueue.clear();
   }

   public static void printChatFromQueue() {
      if (!chatQueue.isEmpty()) {
         MutableComponent toPrint = Component.empty();
         int i = 10000;

         while (i > 0) {
            Component text = chatQueue.poll();
            if (text == null) {
               break;
            }

            int len = text.getString().length();
            if (len <= i) {
               i -= len;
               toPrint.append(text);
            } else {
               chatQueue.offerFirst(TextUtils.substring(text, i, len));
               chatQueue.offerFirst(TextUtils.substring(text, 0, i));
            }
         }

         String print = toPrint.getString();
         if (!print.isEmpty()) {
            charsQueued = charsQueued - print.length();
            FiguraMod.sendChatMessage((Component)(print.endsWith("\n") ? TextUtils.substring(toPrint, 0, print.length() - 1) : toPrint));
         }
      }
   }

   static {
      updateDecimalFormatting();
   }
}
