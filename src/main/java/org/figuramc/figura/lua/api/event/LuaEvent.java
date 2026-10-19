package org.figuramc.figura.lua.api.event;

import com.google.common.collect.HashMultimap;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMetamethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;

@LuaWhitelist
@LuaTypeDoc(name = "Event", value = "event")
public class LuaEvent {
   private static final int MAX_FUNCTIONS = 1024;
   private final boolean piped;
   private final Deque<LuaFunction> functions = new ConcurrentLinkedDeque<>();
   private final Deque<LuaFunction> queue = new ConcurrentLinkedDeque<>();
   private final Deque<LuaFunction> removalQueue = new ConcurrentLinkedDeque<>();
   private final HashMultimap<String, LuaFunction> names = HashMultimap.create();

   public LuaEvent() {
      this(false);
   }

   public LuaEvent(boolean piped) {
      this.piped = piped;
   }

   protected void flushQueue() {
      for (LuaFunction function : this.removalQueue) {
         this.functions.removeFirstOccurrence(function);
      }

      this.removalQueue.clear();

      for (LuaFunction function : this.queue) {
         this.functions.addLast(function);
      }

      this.queue.clear();
   }

   public Varargs call(Varargs args) {
      this.flushQueue();
      if (this.piped) {
         return this.callPiped(args);
      }

      LuaTable result = new LuaTable();

      for (LuaFunction function : this.functions) {
         FiguraMod.pushProfiler(function.name());
         Varargs val = function.invoke(args);

         for (int i = 0; i < val.narg(); i++) {
            result.insert(0, val.arg(i + 1));
         }

         FiguraMod.popProfiler();
      }

      return result.unpack();
   }

   private Varargs callPiped(Varargs args) {
      Varargs vars = args;

      for (LuaFunction function : this.functions) {
         FiguraMod.pushProfiler(function.name());
         vars = function.invoke(vars);
         FiguraMod.popProfiler();
      }

      return vars;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "func"),
            @LuaMethodOverload(argumentTypes = {LuaFunction.class, String.class}, argumentNames = {"func", "name"})
      },
      value = "event.register"
   )
   public LuaEvent register(@LuaNotNil LuaFunction func, String name) {
      if (this.__len() >= 1024) {
         throw new LuaError("Reached maximum limit of 1024 functions in one event!");
      }

      this.queue.addLast(func);
      if (name != null) {
         this.names.put(name, func);
      }

      return this;
   }

   @LuaWhitelist
   @LuaMethodDoc("event.clear")
   public void clear() {
      this.functions.clear();
      this.queue.clear();
      this.removalQueue.clear();
      this.names.clear();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      overloads = {
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "name"),
            @LuaMethodOverload(argumentTypes = LuaFunction.class, argumentNames = "function")
      },
      value = "event.remove"
   )
   public int remove(@LuaNotNil Object toRemove) {
      this.flushQueue();
      if (toRemove instanceof LuaFunction func) {
         this.removalQueue.add(func);
         this.names.values().remove(func);
         return 1;
      } else if (toRemove instanceof String name) {
         int removed = 0;

         for (LuaFunction function : this.names.removeAll(name)) {
            if (this.removalQueue.add(function)) {
               removed++;
            }
         }

         return removed;
      } else {
         throw new LuaError("Illegal argument to remove(): " + toRemove.getClass().getSimpleName());
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "name"), value = "event.get_registered_count")
   public int getRegisteredCount(@LuaNotNil String name) {
      return this.names.get(name).size();
   }

   @LuaWhitelist
   @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(types = {int.class, LuaEvent.class}))
   public int __len() {
      return this.functions.size() + this.queue.size();
   }

   @Override
   public String toString() {
      return "Event";
   }
}
