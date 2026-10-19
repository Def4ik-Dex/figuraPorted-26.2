package org.figuramc.figura.entries;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import org.figuramc.figura.lua.api.event.LuaEvent;

public interface FiguraEvent {
   String getID();

   Collection<Pair<String, LuaEvent>> getEvents();
}
