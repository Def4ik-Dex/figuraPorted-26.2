package org.figuramc.figura.model.rendering.texture;

import java.lang.reflect.Type;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.model.TextureCustomization;
import org.jetbrains.annotations.Nullable;

public class FiguraTextureSet {
   public final String name;
   public final FiguraTexture[] textures = new FiguraTexture[4];

   public FiguraTextureSet(String name, FiguraTexture mainData, FiguraTexture emissiveData, FiguraTexture specularData, FiguraTexture normalData) {
      this.name = name;
      this.textures[0] = mainData;
      this.textures[1] = emissiveData;
      this.textures[2] = specularData;
      this.textures[3] = normalData;
   }

   public void clean() {
      for (FiguraTexture texture : this.textures) {
         if (texture != null) {
            texture.closeFromRenderThread();
         }
      }
   }

   public void uploadIfNeeded() {
      for (FiguraTexture texture : this.textures) {
         if (texture != null) {
            texture.uploadIfDirty(false, false);
         }
      }
   }

   public int getWidth() {
      for (FiguraTexture texture : this.textures) {
         if (texture != null) {
            return texture.getWidth();
         }
      }

      return -1;
   }

   public int getHeight() {
      for (FiguraTexture texture : this.textures) {
         if (texture != null) {
            return texture.getHeight();
         }
      }

      return -1;
   }

   public Identifier getOverrideTexture(UUID owner, TextureCustomization pair) {
      FiguraTextureSet.OverrideType type;
      if (pair != null && (type = pair.getOverrideType()) != null) {
         return switch (type) {
            case SKIN, CAPE, ELYTRA -> {
               ClientPacketListener connection = Minecraft.getInstance().getConnection();
               if (connection == null) {
                  yield null;
               } else {
                  PlayerInfo info = connection.getPlayerInfo(owner);
                  if (info == null) {
                     yield null;
                  } else {
                     Identifier var18 = switch (type) {
                        case CAPE -> info.getSkin().cape().texturePath();
                        case ELYTRA -> info.getSkin().elytra() == null ? Identifier.withDefaultNamespace("elytra") : info.getSkin().elytra().texturePath();
                        default -> info.getSkin().body().texturePath();
                     };
                     yield var18;
                  }
               }
            }
            case RESOURCE -> {
               Identifier var15;
               try {
                  var15 = Identifier.parse(String.valueOf(pair.getValue()));
               } catch (Exception ignored) {
                  var15 = MissingTextureAtlasSprite.getLocation();
                  yield var15;
               }

               yield var15;
            }
            case PRIMARY -> {
               Identifier var13 = this.textures[0] == null ? null : this.textures[0].getLocation();
               yield var13;
            }
            case SECONDARY -> {
               Identifier var12 = this.textures[1] == null ? null : this.textures[1].getLocation();
               yield var12;
            }
            case SPECULAR -> {
               Identifier var11 = this.textures[2] == null ? null : this.textures[2].getLocation();
               yield var11;
            }
            case NORMAL -> {
               Identifier var10 = this.textures[3] == null ? null : this.textures[3].getLocation();
               yield var10;
            }
            case CUSTOM -> {
               Identifier var9;
               try {
                  var9 = ((FiguraTexture)pair.getValue()).getLocation();
               } catch (Exception ignored) {
                  var9 = MissingTextureAtlasSprite.getLocation();
                  yield var9;
               }

               yield var9;
            }
         };
      } else {
         return null;
      }
   }

   public enum OverrideType {
      SKIN,
      CAPE,
      ELYTRA,
      RESOURCE(String.class, "String"),
      PRIMARY,
      SECONDARY,
      SPECULAR,
      NORMAL,
      CUSTOM(FiguraTexture.class, "Texture");

      @Nullable
      public final Type argumentType;
      @Nullable
      public final String typeName;

      OverrideType() {
         this.argumentType = null;
         this.typeName = null;
      }

      OverrideType(@Nullable Type argumentType, String typeName) {
         this.argumentType = argumentType;
         this.typeName = typeName;
      }
   }
}
