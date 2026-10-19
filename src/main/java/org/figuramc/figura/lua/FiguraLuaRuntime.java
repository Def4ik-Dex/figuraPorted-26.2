package org.figuramc.figura.lua;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.api.AvatarAPI;
import org.figuramc.figura.lua.api.HostAPI;
import org.figuramc.figura.lua.api.RendererAPI;
import org.figuramc.figura.lua.api.TextureAPI;
import org.figuramc.figura.lua.api.action_wheel.ActionWheelAPI;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.entity.NullEntity;
import org.figuramc.figura.lua.api.event.EventsAPI;
import org.figuramc.figura.lua.api.event.LuaEvent;
import org.figuramc.figura.lua.api.keybind.KeybindAPI;
import org.figuramc.figura.lua.api.nameplate.NameplateAPI;
import org.figuramc.figura.lua.api.ping.PingAPI;
import org.figuramc.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.PathUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseStringLib;

public class FiguraLuaRuntime {
   public EntityAPI<?> entityAPI;
   public EventsAPI events;
   public VanillaModelAPI vanilla_model;
   public KeybindAPI keybinds;
   public HostAPI host;
   public NameplateAPI nameplate;
   public RendererAPI renderer;
   public ActionWheelAPI action_wheel;
   public AvatarAPI avatar_meta;
   public PingAPI ping;
   public TextureAPI texture;
   public final Avatar owner;
   private final Globals userGlobals = new Globals();
   private final LuaFunction setHookFunction;
   private final LuaFunction getInfoFunction;
   protected final Map<String, String> scripts = new HashMap<>();
   private final Map<String, Varargs> loadedScripts = new HashMap<>();
   private final Stack<String> loadingScripts = new Stack<>();
   public final LuaTypeManager typeManager = new LuaTypeManager();
   private final VarArgFunction require = new VarArgFunction() {
      public Varargs invoke(Varargs arg) {
         Path path = PathUtils.getPath(arg.checkstring(1));
         Path dir = PathUtils.getWorkingDirectory(FiguraLuaRuntime.this.getInfoFunction);
         String scriptName = PathUtils.computeSafeString(PathUtils.isAbsolute(path) ? path : dir.resolve(path));
         if (FiguraLuaRuntime.this.loadingScripts.contains(scriptName)) {
            throw new LuaError("Detected circular dependency in script " + FiguraLuaRuntime.this.loadingScripts.peek());
         } else {
            return FiguraLuaRuntime.this.initializeScript(scriptName);
         }
      }

      public String tojstring() {
         return "function: require";
      }
   };
   private final TwoArgFunction listFiles = new TwoArgFunction() {
      public LuaValue call(LuaValue folderPath, LuaValue includeSubfolders) {
         Path path = PathUtils.getPath(folderPath);
         Path dir = PathUtils.getWorkingDirectory(FiguraLuaRuntime.this.getInfoFunction);
         Path targetPath = (PathUtils.isAbsolute(path) ? path : dir.resolve(path)).normalize();
         boolean subFolders = !includeSubfolders.isnil() && includeSubfolders.checkboolean();
         int i = 1;
         LuaTable table = new LuaTable();

         for (String s : FiguraLuaRuntime.this.scripts.keySet()) {
            Path scriptPath = PathUtils.getPath(s);
            if (scriptPath.startsWith(targetPath)) {
               Path result = targetPath.relativize(scriptPath);
               if (subFolders || result.getNameCount() == 1) {
                  table.set(i++, LuaValue.valueOf(s.replace('/', '.')));
               }
            }
         }

         return table;
      }
   };
   private static final Function<FiguraLuaRuntime, LuaValue> loadstringConstructor = runtime -> new VarArgFunction() {
      public Varargs invoke(Varargs args) {
         try {
            LuaValue val = args.arg(1);
            InputStream ld;
            if (val.isfunction()) {
               ld = new FuncStream(val.checkfunction());
            } else {
               if (!val.isstring()) {
                  throw new LuaError("chunk source is neither a string nor function");
               }

               ld = new ByteArrayInputStream(val.checkstring().m_bytes);
            }

            val = args.arg(2);
            String chunkName = val.isstring() ? val.tojstring() : "loadstring";
            val = args.arg(3);
            LuaTable environment = (LuaTable)(val.istable() ? val.checktable() : runtime.userGlobals);
            return runtime.userGlobals.load(ld, chunkName, "t", environment);
         } catch (LuaError e) {
            return varargsOf(NIL, e.getMessageObject());
         }
      }

      public String tojstring() {
         return "function: loadstring";
      }

      private static class FuncStream extends InputStream {
         private final LuaFunction function;
         private String string = "";
         private int index = 0;

         public FuncStream(LuaFunction function) {
            this.function = function;
         }

         @Override
         public int read() {
            if (++this.index >= this.string.length()) {
               this.index = 0;
               Varargs result = this.function.invoke();
               if (!result.isstring(1) || result.arg1().length() < 1) {
                  return -1;
               }

               this.string = new String(result.checkstring(1).m_bytes, StandardCharsets.UTF_8);
            }

            return this.string.charAt(this.index);
         }
      }
   };
   private final ZeroArgFunction onReachedLimit = new ZeroArgFunction() {
      public LuaValue call() {
         FiguraMod.LOGGER
            .warn("Avatar {} bypassed resource limits with {} instructions", FiguraLuaRuntime.this.owner.owner, FiguraLuaRuntime.this.getInstructions());
         LuaError error = new LuaError("Script overran resource limits!");
         FiguraLuaRuntime.this.owner.noPermissions.add(Permissions.INIT_INST);
         FiguraLuaRuntime.this.setInstructionLimit(1);
         throw error;
      }
   };

   public FiguraLuaRuntime(Avatar avatar, Map<String, String> scripts) {
      this.owner = avatar;
      this.scripts.putAll(scripts);
      this.userGlobals.load(new JseBaseLib());
      this.userGlobals.load(new Bit32Lib());
      this.userGlobals.load(new TableLib());
      this.userGlobals.load(new JseStringLib());
      this.userGlobals.load(new JseMathLib());
      LuaC.install(this.userGlobals);
      this.userGlobals.load(new DebugLib());
      LuaTable debugLib = this.userGlobals.get("debug").checktable();
      this.setHookFunction = debugLib.get("sethook").checkfunction();
      this.getInfoFunction = debugLib.get("getinfo").checkfunction();
      this.setupFiguraSandbox();
      FiguraAPIManager.setupTypesAndAPIs(this);
      this.setUser(null);
      this.loadExtraLibraries();
      LuaTable figuraMetatables = new LuaTable();
      this.typeManager.dumpMetatables(figuraMetatables);
      this.setGlobal("figuraMetatables", figuraMetatables);
   }

   public void registerClass(Class<?> clazz) {
      this.typeManager.generateMetatableFor(clazz);
   }

   public void setGlobal(String name, Object obj) {
      this.userGlobals.rawset(name, this.typeManager.javaToLua(obj).arg1());
   }

   public void setUser(Entity user) {
      Object val;
      if (user == null) {
         this.entityAPI = null;
         val = NullEntity.INSTANCE;
      } else {
         val = this.entityAPI = EntityAPI.wrap(user);
      }

      this.userGlobals.rawset("user", this.typeManager.javaToLua(val).arg1());
      this.userGlobals.rawset("player", this.userGlobals.rawget("user"));
   }

   public Entity getUser() {
      return this.entityAPI != null && this.entityAPI.isLoaded() ? this.entityAPI.getEntity() : null;
   }

   private void setupFiguraSandbox() {
      try (InputStream inputStream = FiguraMod.class.getResourceAsStream("/assets/figura/scripts/sandbox.lua")) {
         if (inputStream == null) {
            throw new IOException("Unable to get resource");
         }

         this.userGlobals.load(new String(inputStream.readAllBytes()), "sandbox").call();
      } catch (Exception e) {
         this.error(new LuaError("Failed to load builtin sandbox script:\n" + e.getMessage()));
      }

      LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);
   }

   private void loadExtraLibraries() {
      this.setGlobal("require", this.require);
      this.setGlobal("listFiles", this.listFiles);
      LuaValue loadstring = loadstringConstructor.apply(this);
      this.setGlobal("load", loadstring);
      this.setGlobal("loadstring", loadstring);
      FiguraLuaPrinter.loadPrintFunctions(this);
      FiguraLuaJson.loadFunctions(this);

      try (InputStream inputStream = FiguraMod.class.getResourceAsStream("/assets/figura/scripts/math.lua")) {
         if (inputStream == null) {
            throw new IOException("Unable to get resource");
         }

         this.userGlobals.load(new String(inputStream.readAllBytes()), "math").call();
      } catch (Exception e) {
         this.error(new LuaError("Failed to load builtin math script:\n" + e.getMessage()));
      }

      this.setGlobal("type", new OneArgFunction() {
         public LuaValue call(LuaValue arg) {
            if (arg.type() == 7) {
               return LuaString.valueOf(FiguraLuaRuntime.this.typeManager.getTypeName(arg.checkuserdata().getClass()));
            }

            if (arg.type() == 5 && arg.getmetatable() != null) {
               LuaValue __type = arg.getmetatable().rawget("__type");
               if (!__type.isnil()) {
                  return __type;
               }
            }

            return LuaString.valueOf(arg.typename());
         }

         public String tojstring() {
            return this.typename() + ": type";
         }
      });
      final LuaFunction globalPairs = this.userGlobals.get("pairs").checkfunction();
      this.setGlobal("pairs", new VarArgFunction() {
         public Varargs invoke(Varargs varargs) {
            LuaValue arg1 = varargs.arg1();
            int type = arg1.type();
            if ((type == 5 || type == 7) && arg1.getmetatable() != null) {
               LuaValue __pairs = arg1.getmetatable().rawget("__pairs");
               if (__pairs.isfunction()) {
                  return __pairs.invoke(varargs);
               }
            }

            return globalPairs.invoke(varargs);
         }

         public String tojstring() {
            return this.typename() + ": pairs";
         }
      });
      final LuaFunction globalIPairs = this.userGlobals.get("ipairs").checkfunction();
      this.setGlobal("ipairs", new VarArgFunction() {
         public Varargs invoke(Varargs varargs) {
            LuaValue arg1 = varargs.arg1();
            int type = arg1.type();
            if ((type == 5 || type == 7) && arg1.getmetatable() != null) {
               LuaValue __ipairs = arg1.getmetatable().rawget("__ipairs");
               if (__ipairs.isfunction()) {
                  return __ipairs.invoke(varargs);
               }
            }

            return globalIPairs.invoke(varargs);
         }

         public String tojstring() {
            return this.typename() + ": ipairs";
         }
      });
   }

   private Varargs initializeScript(String str) {
      Path path = PathUtils.getPath(str);
      String name = PathUtils.computeSafeString(path);
      Varargs val = this.loadedScripts.get(name);
      if (val != null) {
         return val;
      }

      String src = this.scripts.get(name);
      if (src == null) {
         throw new LuaError("Tried to require nonexistent script \"" + name + "\"!");
      }

      this.loadingScripts.push(name);
      String directory = PathUtils.computeSafeString(path.getParent());
      String fileName = PathUtils.computeSafeString(path.getFileName());
      Varargs value = this.userGlobals.load(src, name).invoke(LuaValue.varargsOf(LuaValue.valueOf(directory), LuaValue.valueOf(fileName)));
      if (value == LuaValue.NIL) {
         value = LuaValue.TRUE;
      }

      this.loadedScripts.put(name, value);
      this.loadingScripts.pop();
      return value;
   }

   public boolean init(ListTag autoScripts) {
      if (this.scripts.isEmpty()) {
         return false;
      }

      this.owner.luaRuntime = this;

      try {
         if (autoScripts == null) {
            for (String name : this.scripts.keySet()) {
               this.initializeScript(name);
            }
         } else {
            for (Tag name : autoScripts) {
               this.initializeScript((String)name.asString().get());
            }
         }

         return true;
      } catch (Exception | StackOverflowError e) {
         this.error(e);
         return false;
      }
   }

   public void error(Throwable e) {
      FiguraLuaPrinter.sendLuaError(parseError(e), this.owner);
      this.owner.scriptError = true;
      this.owner.luaRuntime = null;
      this.owner.clearParticles();
      this.owner.clearSounds();
      this.owner.closeBuffers();
      this.owner.closeStreams();
   }

   public static LuaError parseError(Throwable e) {
      return e instanceof LuaError lua ? lua : (e instanceof StackOverflowError ? new LuaError("Stack Overflow") : new LuaError(e));
   }

   public void setInstructionLimit(int limit) {
      this.userGlobals.running.state.bytecodes = 0;
      this.setHookFunction.invoke(LuaValue.varargsOf(this.onReachedLimit, LuaValue.EMPTYSTRING, LuaValue.valueOf(Math.max(limit, 1))));
   }

   public int getInstructions() {
      return this.userGlobals.running.state.bytecodes;
   }

   public void takeInstructions(int amount) {
      this.userGlobals.running.state.bytecodes += amount;
   }

   public LuaValue load(String name, String src) {
      return this.userGlobals.load(src, name, this.userGlobals);
   }

   public Varargs run(Object toRun, Avatar.Instructions limit, Object... args) {
      LuaValue[] values = new LuaValue[args.length];

      for (int i = 0; i < values.length; i++) {
         values[i] = this.typeManager.javaToLua(args[i]).arg1();
      }

      Varargs val = LuaValue.varargsOf(values);
      this.setInstructionLimit(limit.remaining);

      try {
         Varargs ret;
         if (toRun instanceof LuaEvent event) {
            ret = event.call(val);
         } else if (toRun instanceof String event) {
            ret = this.events.__index(event).call(val);
         } else {
            if (!(toRun instanceof LuaValue func)) {
               throw new IllegalArgumentException("Internal event error - Invalid type to run! (" + toRun.getClass().getSimpleName() + ")");
            }

            ret = func.invoke(val);
         }

         limit.use(this.getInstructions());
         return ret;
      } catch (Exception | StackOverflowError e) {
         this.error(e);
         return null;
      }
   }
}
