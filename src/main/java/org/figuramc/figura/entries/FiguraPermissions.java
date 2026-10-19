package org.figuramc.figura.entries;

import java.util.Collection;
import org.figuramc.figura.permissions.Permissions;

public interface FiguraPermissions {
   String getTitle();

   Collection<Permissions> getPermissions();
}
