package org.figuramc.figura.lua.api.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Stack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.permissions.Permissions;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(value = "buffer", name = "Buffer")
public class FiguraBuffer implements AutoCloseable {
   private static final int CAPACITY_INCREASE_STEP = 512;
   private final Avatar parent;
   private int length = 0;
   private int position = 0;
   private byte[] buf;
   private boolean isClosed;

   public FiguraBuffer(Avatar parent) {
      this.parent = parent;
      if (parent.openBuffers.size() > this.getMaxBuffersCount()) {
         parent.noPermissions.add(Permissions.BUFFERS_COUNT);
         throw new LuaError("You have exceed the max amount of open buffers");
      }

      if (512 > this.getMaxCapacity()) {
         parent.noPermissions.add(Permissions.BUFFER_SIZE);
         throw new LuaError("Unable to create buffer because max capacity is less than default buffer size (512)");
      }

      this.buf = new byte[512];
      parent.openBuffers.add(this);
   }

   public FiguraBuffer(Avatar parent, int cap) {
      this.parent = parent;
      if (cap > this.getMaxCapacity()) {
         parent.noPermissions.add(Permissions.BUFFER_SIZE);
         throw new LuaError("Unable to create a buffer with capacity %s");
      }

      this.buf = new byte[cap];
   }

   private void ensureBufCapacity(int cap) {
      if (cap > this.getMaxCapacity()) {
         throw new LuaError("Can't increase this buffer capacity to %s, max capacity is %s".formatted(cap, this.getMaxCapacity()));
      }

      if (cap > this.buf.length) {
         this.buf = Arrays.copyOf(this.buf, Math.min(this.buf.length + 512, this.getMaxCapacity()));
      }
   }

   private byte[] readNBytes(int count) {
      byte[] arr = new byte[Math.min(count, this.available())];

      int b;
      int i;
      for (i = 0; i < count && (b = this.read()) != -1; i++) {
         arr[i] = (byte)b;
      }

      if (i < count) {
         byte[] newArr = new byte[i];
         System.arraycopy(arr, 0, newArr, 0, i);
         arr = newArr;
      }

      return arr;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read")
   public int read() {
      this.checkIsClosed();
      if (this.position == this.length) {
         return -1;
      }

      int v = this.buf[this.position] & 255;
      this.position++;
      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_short")
   public int readShort() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(2);
      short v = 0;

      for (int i = 0; i < bytes.length; i++) {
         v |= (short)((bytes[bytes.length - 1 - i] & 255) << i * 8);
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_ushort")
   public int readUShort() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(2);
      int v = 0;

      for (int i = 0; i < bytes.length; i++) {
         v |= (bytes[bytes.length - 1 - i] & 255) << i * 8;
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_int")
   public int readInt() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(4);
      int v = 0;

      for (int i = 0; i < bytes.length; i++) {
         v |= (bytes[bytes.length - 1 - i] & 255) << i * 8;
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_long")
   public long readLong() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(8);
      long v = 0L;

      for (int i = 0; i < bytes.length; i++) {
         v |= (long)(bytes[bytes.length - 1 - i] & 0xFF) << i * 8;
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_float")
   public float readFloat() {
      this.checkIsClosed();
      return Float.intBitsToFloat(this.readInt());
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_double")
   public double readDouble() {
      this.checkIsClosed();
      return Double.longBitsToDouble(this.readLong());
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_short_le")
   public int readShortLE() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(2);
      short v = 0;

      for (int i = 0; i < bytes.length; i++) {
         v |= (short)((bytes[i] & 255) << i * 8);
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_ushort_le")
   public int readUShortLE() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(2);
      int v = 0;

      for (int i = 0; i < bytes.length; i++) {
         v |= (bytes[i] & 255) << i * 8;
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_int_le")
   public int readIntLE() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(4);
      int v = 0;

      for (int i = 0; i < bytes.length; i++) {
         v |= (bytes[i] & 255) << i * 8;
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_long_le")
   public long readLongLE() {
      this.checkIsClosed();
      byte[] bytes = this.readNBytes(8);
      long v = 0L;

      for (int i = 0; i < bytes.length; i++) {
         v |= (long)(bytes[i] & 0xFF) << i * 8;
      }

      return v;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_float_le")
   public float readFloatLE() {
      this.checkIsClosed();
      return Float.intBitsToFloat(this.readIntLE());
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.read_double_le")
   public double readDoubleLE() {
      this.checkIsClosed();
      return Double.longBitsToDouble(this.readLongLE());
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.read_string",
      overloads = {
            @LuaMethodOverload(returnType = String.class),
            @LuaMethodOverload(argumentNames = "length", argumentTypes = Integer.class, returnType = String.class),
            @LuaMethodOverload(argumentNames = {"length", "encoding"}, argumentTypes = {Integer.class, String.class}, returnType = String.class)
      }
   )
   public String readString(Integer length, String encoding) {
      this.checkIsClosed();
      length = length == null ? this.available() : Math.max(length, 0);
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
      byte[] strBuf = this.readNBytes(length);
      return new String(strBuf, charset);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.read_base_64",
      overloads = {
            @LuaMethodOverload(returnType = String.class),
            @LuaMethodOverload(argumentNames = "length", argumentTypes = Integer.class, returnType = String.class)
      }
   )
   public String readBase64(Integer length) {
      length = length == null ? this.available() : Math.max(length, 0);
      byte[] strBuf = this.readNBytes(length);
      return Base64.getEncoder().encodeToString(strBuf);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.read_byte_array",
      overloads = {
            @LuaMethodOverload(returnType = String.class),
            @LuaMethodOverload(argumentNames = "length", argumentTypes = Integer.class, returnType = String.class)
      }
   )
   public LuaString readByteArray(Integer length) {
      length = length == null ? this.available() : Math.max(length, 0);
      byte[] strBuf = this.readNBytes(length);
      return LuaString.valueOf(strBuf);
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_byte", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Integer.class))
   public void write(@LuaNotNil int val) {
      this.checkIsClosed();
      if (this.position == this.length) {
         this.length++;
         this.ensureBufCapacity(this.length);
      }

      this.buf[this.position] = (byte)(val & 0xFF);
      this.position++;
   }

   private void writeBytes(byte[] bytes) {
      for (byte aByte : bytes) {
         this.write(aByte & 255);
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_short", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Integer.class))
   public void writeShort(@LuaNotNil Integer val) {
      this.checkIsClosed();
      if (val >= -32768 && val <= 32767) {
         short s = (short)val.intValue();
         this.write(s >> 8 & 0xFF);
         this.write(s & 255);
      } else {
         throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, (short)-32768, (short)32767));
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_ushort", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Integer.class))
   public void writeUShort(@LuaNotNil Integer val) {
      this.checkIsClosed();
      if (val >= 0 && val <= 65535) {
         char s = (char)val.intValue();
         this.write(s >> '\b' & 0xFF);
         this.write(s & 255);
      } else {
         throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, 0, 65535));
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_int", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Integer.class))
   public void writeInt(@LuaNotNil Integer val) {
      this.checkIsClosed();
      this.write(val >> 24 & 0xFF);
      this.write(val >> 16 & 0xFF);
      this.write(val >> 8 & 0xFF);
      this.write(val & 0xFF);
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_long", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Long.class))
   public void writeLong(@LuaNotNil Long val) {
      this.checkIsClosed();
      this.write((int)(val >> 56 & 255L));
      this.write((int)(val >> 48 & 255L));
      this.write((int)(val >> 40 & 255L));
      this.write((int)(val >> 32 & 255L));
      this.write((int)(val >> 24 & 255L));
      this.write((int)(val >> 16 & 255L));
      this.write((int)(val >> 8 & 255L));
      this.write((int)(val & 255L));
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_float", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Float.class))
   public void writeFloat(@LuaNotNil Float val) {
      this.checkIsClosed();
      this.writeInt(Float.floatToIntBits(val));
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_double", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Double.class))
   public void writeDouble(@LuaNotNil Double val) {
      this.checkIsClosed();
      this.writeLong(Double.doubleToLongBits(val));
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_short_le", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Integer.class))
   public void writeShortLE(@LuaNotNil Integer val) {
      this.checkIsClosed();
      if (val >= -32768 && val <= 32767) {
         short s = (short)val.intValue();
         this.write(s & 255);
         this.write(s >> 8 & 0xFF);
      } else {
         throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, (short)-32768, (short)32767));
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_ushort_le", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Integer.class))
   public void writeUShortLE(@LuaNotNil Integer val) {
      this.checkIsClosed();
      if (val >= 0 && val <= 65535) {
         char s = (char)val.intValue();
         this.write(s & 255);
         this.write(s >> '\b' & 0xFF);
      } else {
         throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, 0, 65535));
      }
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_int_le", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Integer.class))
   public void writeIntLE(@LuaNotNil Integer val) {
      this.checkIsClosed();
      this.write(val & 0xFF);
      this.write(val >> 8 & 0xFF);
      this.write(val >> 16 & 0xFF);
      this.write(val >> 24 & 0xFF);
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_long_le", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Long.class))
   public void writeLongLE(@LuaNotNil Long val) {
      this.checkIsClosed();
      this.write((int)(val & 255L));
      this.write((int)(val >> 8 & 255L));
      this.write((int)(val >> 16 & 255L));
      this.write((int)(val >> 24 & 255L));
      this.write((int)(val >> 32 & 255L));
      this.write((int)(val >> 40 & 255L));
      this.write((int)(val >> 48 & 255L));
      this.write((int)(val >> 56 & 255L));
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_float_le", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Float.class))
   public void writeFloatLE(@LuaNotNil Float val) {
      this.checkIsClosed();
      this.writeIntLE(Float.floatToIntBits(val));
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.write_double_le", overloads = @LuaMethodOverload(argumentNames = "val", argumentTypes = Double.class))
   public void writeDoubleLE(@LuaNotNil Double val) {
      this.checkIsClosed();
      this.writeLongLE(Double.doubleToLongBits(val));
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.write_string",
      overloads = {
            @LuaMethodOverload(argumentTypes = String.class, argumentNames = "val", returnType = Integer.class),
            @LuaMethodOverload(argumentTypes = {String.class, String.class}, argumentNames = {"val", "encoding"}, returnType = Integer.class)
      }
   )
   public int writeString(@LuaNotNil String val, String encoding) {
      this.checkIsClosed();
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
      byte[] strBytes = val.getBytes(charset);
      this.writeBytes(strBytes);
      return strBytes.length;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.write_base_64",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "base64", returnType = Integer.class)
   )
   public int writeBase64(@LuaNotNil String base64String) {
      this.checkIsClosed();
      byte[] base64Bytes = Base64.getDecoder().decode(base64String);
      this.writeBytes(base64Bytes);
      return base64Bytes.length;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.write_byte_array",
      overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "array", returnType = Integer.class)
   )
   public int writeByteArray(@LuaNotNil LuaValue val) {
      this.checkIsClosed();
      if (val instanceof LuaString byteArray) {
         byte[] bytes = new byte[byteArray.length()];
         byteArray.copyInto(0, bytes, 0, bytes.length);
         this.writeBytes(bytes);
         return bytes.length;
      } else {
         throw new LuaError("Expected string, got %s".formatted(val.typename()));
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.get_length")
   public int getLength() {
      this.checkIsClosed();
      return this.length;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.get_position")
   public int getPosition() {
      this.checkIsClosed();
      return this.position;
   }

   @LuaWhitelist
   @LuaMethodDoc(value = "buffer.set_position", overloads = @LuaMethodOverload(argumentNames = "position", argumentTypes = Integer.class))
   public void setPosition(@LuaNotNil Integer position) {
      this.checkIsClosed();
      this.position = Math.max(Math.min(position, this.length), 0);
   }

   @LuaWhitelist
   @LuaMethodDoc("clear")
   public void clear() {
      this.checkIsClosed();
      this.position = 0;
      this.length = 0;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.available")
   public int available() {
      this.checkIsClosed();
      return this.length - this.position;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.get_max_capacity")
   public int getMaxCapacity() {
      return this.parent.permissions.get(Permissions.BUFFER_SIZE);
   }

   private int getMaxBuffersCount() {
      return this.parent.permissions.get(Permissions.BUFFERS_COUNT);
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.read_from_stream",
      overloads = @LuaMethodOverload(
         argumentTypes = {FiguraInputStream.class, Integer.class},
         argumentNames = {"stream", "amount"},
         returnType = Integer.class
      )
   )
   public int readFromStream(@LuaNotNil FiguraInputStream stream, Integer amount) {
      this.checkIsClosed();
      if (amount == null) {
         amount = this.getMaxCapacity() - this.position;
      } else {
         amount = Math.max(Math.min(amount, this.getMaxCapacity() - this.position), 0);
      }

      int i;
      for (i = 0; i < amount; i++) {
         int b = stream.read();
         if (b == -1) {
            break;
         }

         this.write(b);
      }

      return i;
   }

   @LuaWhitelist
   @LuaMethodDoc(
      value = "buffer.write_to_stream",
      overloads = @LuaMethodOverload(
         argumentTypes = {FiguraOutputStream.class, Integer.class},
         argumentNames = {"stream", "amount"},
         returnType = Integer.class
      )
   )
   public int writeToStream(@LuaNotNil FiguraOutputStream stream, Integer amount) {
      this.checkIsClosed();
      if (amount == null) {
         amount = this.available();
      } else {
         amount = Math.max(Math.min(amount, this.available()), -1);
      }

      for (int i = 0; i < amount; i++) {
         stream.write(this.read());
      }

      return amount;
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.close")
   @Override
   public void close() {
      this.baseClose();
      this.parent.openBuffers.remove(this);
   }

   public void baseClose() {
      if (!this.isClosed) {
         this.isClosed = true;
         this.buf = null;
      }
   }

   @LuaWhitelist
   @LuaMethodDoc("buffer.is_closed")
   public boolean isClosed() {
      return this.isClosed;
   }

   private void checkIsClosed() {
      if (this.isClosed) {
         throw new LuaError("This byte buffer is closed and cant be used anymore");
      }
   }

   @Override
   public String toString() {
      return "Buffer";
   }

   public FiguraBuffer.FiguraBufferInputStream asInputStream() {
      return new FiguraBuffer.FiguraBufferInputStream(this);
   }

   public static class FiguraBufferInputStream extends InputStream {
      private final FiguraBuffer parent;
      private final Stack<FiguraBuffer.FiguraBufferInputStream.Mark> marks = new Stack<>();

      public FiguraBufferInputStream(FiguraBuffer figuraBuffer) {
         this.parent = figuraBuffer;
      }

      @Override
      public int read() throws IOException {
         if (!this.marks.empty()) {
            FiguraBuffer.FiguraBufferInputStream.Mark m = this.marks.peek();
            if (this.parent.getPosition() > m.pos + m.readLimit) {
               return -1;
            }
         }

         return this.parent.read();
      }

      @Override
      public boolean markSupported() {
         return true;
      }

      @Override
      public void mark(int readlimit) {
         this.marks.push(new FiguraBuffer.FiguraBufferInputStream.Mark(this.parent.position, readlimit));
      }

      @Override
      public void reset() throws IOException {
         FiguraBuffer.FiguraBufferInputStream.Mark m = this.marks.pop();
         this.parent.setPosition(m.pos);
      }

      @Override
      public long skip(long n) throws IOException {
         long d = Math.min(this.parent.getLength() - this.parent.getPosition(), n);
         this.parent.setPosition((int)(this.parent.getPosition() + d));
         return d;
      }

      @Override
      public int available() throws IOException {
         return this.parent.available();
      }

      private record Mark(int pos, int readLimit) {
      }
   }
}
