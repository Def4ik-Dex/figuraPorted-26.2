package org.figuramc.figura.ducks;

import java.util.UUID;

public interface ChannelHandleAccessor {
   UUID getOwner();

   void setOwner(UUID var1);

   String getName();

   void setName(String var1);
}
