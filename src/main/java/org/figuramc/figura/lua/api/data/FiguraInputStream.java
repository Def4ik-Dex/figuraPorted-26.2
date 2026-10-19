package org.figuramc.figura.lua.api.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;

@LuaWhitelist
@LuaTypeDoc(name = "InputStream", value = "input_stream")
public class FiguraInputStream extends InputStream {
   private final InputStream sourceStream;
   private final boolean asyncOnly;
   private final Avatar parent;

   public FiguraInputStream(Avatar parent, InputStream sourceStream) {
      this(parent, sourceStream, false);
   }

   public FiguraInputStream(Avatar parent, InputStream sourceStream, boolean asyncOnly) {
      this.sourceStream = sourceStream;
      this.asyncOnly = asyncOnly;
      this.parent = parent;
      parent.openInputStreams.add(this);
   }

   @LuaWhitelist
   @LuaMethodDoc("input_stream.read")
   @Override
   public int read() {
      try {
         if (this.asyncOnly) {
            throw new IOException("This stream supports only async read");
         } else {
            return this.sourceStream.read();
         }
      } catch (IOException e) {
         throw new LuaError(e);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("input_stream.read_async")
   public FiguraFuture<LuaString> readAsync(Integer limit) {
      int finalLimit = limit != null ? limit : this.available();
      FiguraFuture<LuaString> future = new FiguraFuture<>();
      CompletableFuture.<LuaString>supplyAsync(() -> {
         try {
            byte[] buf = new byte[finalLimit];
            int len = this.sourceStream.read(buf);
            if (len == -1) {
               return LuaString.valueOf("");
            }

            if (len < buf.length) {
               buf = Arrays.copyOf(buf, len);
            }

            return LuaString.valueOf(buf);
         } catch (IOException e) {
            throw new LuaError(e);
         }
      }).whenCompleteAsync(future::handle);
      return future;
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "input_stream.skip", overloads = @LuaMethodOverload(argumentNames = "n", argumentTypes = Long.class, returnType = Long.class))
   @Override
   public long skip(long n) throws IOException {
      return this.sourceStream.skip(n);
   }

   @LuaWhitelist
   @LuaMethodDoc("input_stream.available")
   @Override
   public int available() {
      try {
         return this.sourceStream.available();
      } catch (IOException e) {
         throw new LuaError(e);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("input_stream.close")
   @Override
   public void close() throws IOException {
      this.sourceStream.close();
      this.parent.openInputStreams.remove(this);
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "input_stream.mark", overloads = @LuaMethodOverload(argumentNames = "readLimit", argumentTypes = Integer.class))
   @Override
   public void mark(int readlimit) {
      this.sourceStream.mark(readlimit);
   }

   @LuaWhitelist
   @LuaMethodDoc("input_stream.reset")
   @Override
   public void reset() throws IOException {
      this.sourceStream.reset();
   }

   @LuaWhitelist
   @LuaMethodDoc("input_stream.mark_supported")
   @Override
   public boolean markSupported() {
      return this.sourceStream.markSupported();
   }

   @LuaWhitelist
   @LuaMethodDoc("input_stream.is_async_only")
   public boolean isAsyncOnly() {
      return this.asyncOnly;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "input_stream.transfer_to",
      overloads = @LuaMethodOverload(argumentNames = "out", argumentTypes = FiguraOutputStream.class, returnType = Long.class)
   )
   @Override
   public long transferTo(OutputStream out) throws IOException {
      return this.sourceStream.transferTo(out);
   }

   @Override
   public String toString() {
      return "InputStream";
   }
}
