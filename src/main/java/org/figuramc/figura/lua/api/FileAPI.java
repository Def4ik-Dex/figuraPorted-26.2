package org.figuramc.figura.lua.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.api.data.FiguraOutputStream;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

@LuaWhitelist
@LuaTypeDoc(name = "FileAPI", value = "file")
public class FileAPI {
   private final Avatar parent;
   private static final String WRITE_NOT_ALLOWED = "You are only allowed to write in the data folder! Anything else is read only!";

   public FileAPI(Avatar parent) {
      this.parent = parent;
   }

   private Path securityCheck(String path) {
      if (!this.parent.isHost) {
         throw new LuaError("You can't use FileAPI outside of host environment");
      } else {
         Path p = this.relativizePath(path);
         if (Files.isSymbolicLink(p)) {
            throw new LuaError("Symbolic links are not allowed in FileAPI %s!".formatted(path));
         } else if (!this.isPathAllowed(p)) {
            throw new LuaError("Path %s is not allowed in FileAPI".formatted(path));
         } else {
            return p;
         }
      }
   }

   private static Path dataPath() {
      return FiguraMod.getFiguraDirectory().resolve("data").toAbsolutePath().normalize();
   }

   private Path relativizePath(String path) {
      Path p = Path.of(path);
      return p.isAbsolute() ? p.normalize() : dataPath().resolve(path).toAbsolutePath().normalize();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "file.is_path_allowed",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = Boolean.class)
   )
   public boolean isPathAllowed(@LuaNotNil String path) {
      return this.isPathAllowed(this.relativizePath(path));
   }

   public boolean isPathAllowed(Path path) {
      return !Files.isSymbolicLink(path) && path.toAbsolutePath().startsWith(dataPath());
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.allowed", overloads = @LuaMethodOverload(returnType = Boolean.class))
   public boolean allowed() {
      return this.parent.isHost;
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.exists", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = Boolean.class))
   public boolean exists(@LuaNotNil String path) {
      Path p = this.securityCheck(path);
      File f = p.toFile();
      return f.exists();
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.is_file", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = Boolean.class))
   public boolean isFile(@LuaNotNil String path) {
      Path p = this.securityCheck(path);
      File f = p.toFile();
      return f.exists() && f.isFile();
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.is_directory", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = Boolean.class))
   public boolean isDirectory(@LuaNotNil String path) {
      Path p = this.securityCheck(path);
      File f = p.toFile();
      return f.exists() && f.isDirectory();
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "file.open_read_stream",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = FiguraInputStream.class)
   )
   public FiguraInputStream openReadStream(@LuaNotNil String path) {
      try {
         Path p = this.securityCheck(path);
         File f = p.toFile();
         FileInputStream fis = new FileInputStream(f);
         return new FiguraInputStream(this.parent, fis);
      } catch (FileNotFoundException e) {
         throw new LuaError(e);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "file.open_write_stream",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = FiguraOutputStream.class)
   )
   public FiguraOutputStream openWriteStream(@LuaNotNil String path) {
      try {
         Path p = this.securityCheck(path);
         if (!p.startsWith(dataPath())) {
            throw new LuaError("You are only allowed to write in the data folder! Anything else is read only!");
         }

         File f = p.toFile();
         FileOutputStream fos = new FileOutputStream(f);
         return new FiguraOutputStream(this.parent, fos);
      } catch (FileNotFoundException e) {
         throw new LuaError(e);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "file.read_string",
      overloads = @LuaMethodOverload(argumentTypes = {String.class, String.class}, argumentNames = {"path", "encoding"}, returnType = String.class)
   )
   public String readString(@LuaNotNil String path, String encoding) {
      try {
         Charset var10000;
         if (encoding == null) {
            var10000 = StandardCharsets.UTF_8;
         } else {
            switch (encoding.toLowerCase(Locale.US)) {
               case "utf_16":
               case "utf16":
                  var10000 = StandardCharsets.UTF_16;
                  break;
               case "utf_16be":
               case "utf16be":
                  var10000 = StandardCharsets.UTF_16BE;
                  break;
               case "utf_16le":
               case "utf16le":
                  var10000 = StandardCharsets.UTF_16LE;
                  break;
               case "ascii":
                  var10000 = StandardCharsets.US_ASCII;
                  break;
               case "iso_8859_1":
               case "iso88591":
                  var10000 = StandardCharsets.ISO_8859_1;
                  break;
               default:
                  var10000 = StandardCharsets.UTF_8;
            }
         }

         Charset charset = var10000;
         Path filePath = this.securityCheck(path);
         File file = filePath.toFile();
         byte[] data = com.google.common.io.Files.toByteArray(file);
         return new String(data, charset);
      } catch (IOException e) {
         throw new LuaError(e);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "file.write_string",
      overloads = @LuaMethodOverload(argumentTypes = {String.class, String.class, String.class}, argumentNames = {"path", "data", "encoding"})
   )
   public void writeString(@LuaNotNil String path, @LuaNotNil String data, String encoding) {
      try (FiguraOutputStream fos = this.openWriteStream(path)) {
         Charset var10000;
         if (encoding == null) {
            var10000 = StandardCharsets.UTF_8;
         } else {
            switch (encoding.toLowerCase(Locale.US)) {
               case "utf_16":
               case "utf16":
                  var10000 = StandardCharsets.UTF_16;
                  break;
               case "utf_16be":
               case "utf16be":
                  var10000 = StandardCharsets.UTF_16BE;
                  break;
               case "utf_16le":
               case "utf16le":
                  var10000 = StandardCharsets.UTF_16LE;
                  break;
               case "ascii":
                  var10000 = StandardCharsets.US_ASCII;
                  break;
               case "iso_8859_1":
               case "iso88591":
                  var10000 = StandardCharsets.ISO_8859_1;
                  break;
               default:
                  var10000 = StandardCharsets.UTF_8;
            }
         }

         Charset charset = var10000;
         fos.write(data.getBytes(charset));
      } catch (IOException e) {
         throw new LuaError(e);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.mkdir", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = Boolean.class))
   public boolean mkdir(@LuaNotNil String path) {
      Path p = this.securityCheck(path);
      if (!p.startsWith(dataPath())) {
         throw new LuaError("You are only allowed to write in the data folder! Anything else is read only!");
      }

      File f = p.toFile();
      return f.mkdir();
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.mkdirs", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = Boolean.class))
   public boolean mkdirs(@LuaNotNil String path) {
      Path p = this.securityCheck(path);
      if (!p.startsWith(dataPath())) {
         throw new LuaError("You are only allowed to write in the data folder! Anything else is read only!");
      }

      File f = p.toFile();
      return f.mkdirs();
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.delete", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = Boolean.class))
   public boolean delete(@LuaNotNil String path) {
      Path p = this.securityCheck(path);
      if (!p.startsWith(dataPath())) {
         throw new LuaError("You are only allowed to write in the data folder! Anything else is read only!");
      }

      File f = p.toFile();
      return f.delete();
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "file.list", overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "path", returnType = LuaTable.class))
   public ArrayList<String> list(@LuaNotNil String path) {
      Path p = this.securityCheck(path);
      File f = p.toFile();
      ArrayList<String> s = new ArrayList<>();
      if (f.exists() && f.isDirectory()) {
         Arrays.stream(f.list()).forEach(str -> s.add(str));
         return s;
      } else {
         return null;
      }
   }

   @Override
   public String toString() {
      return "FileAPI";
   }
}
