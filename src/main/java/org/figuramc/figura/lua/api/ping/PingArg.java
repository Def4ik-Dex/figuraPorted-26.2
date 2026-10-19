package org.figuramc.figura.lua.api.ping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.matrix.FiguraMatrix;
import org.figuramc.figura.math.vector.FiguraVector;
import org.figuramc.figura.utils.MathUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class PingArg {
   private static final int NIL = 0;
   private static final int BOOL_TRUE = 1;
   private static final int BOOL_FALSE = 2;
   private static final int DOUBLE = 3;
   private static final int STRING = 4;
   private static final int TABLE = 5;
   private static final int VECTOR_2 = 6;
   private static final int VECTOR_3 = 7;
   private static final int VECTOR_4 = 8;
   private static final int MATRIX_2 = 9;
   private static final int MATRIX_3 = 10;
   private static final int MATRIX_4 = 11;
   private static final int INT_1B = 12;
   private static final int INT_2B = 13;
   private static final int INT_3B = 14;
   private static final int INT_4B = 15;
   private final Varargs args;

   public PingArg(Varargs args) {
      this.args = args;
   }

   public byte[] toByteArray() {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(baos);

         for (int i = 0; i < this.args.narg(); i++) {
            LuaValue arg = this.args.arg(i + 1);
            writeArg(arg, dos);
         }

         return baos.toByteArray();
      } catch (Exception e) {
         throw new LuaError("Failed to write ping! " + e.getMessage());
      }
   }

   private static void writeArg(LuaValue val, DataOutputStream dos) throws IOException {
      if (val.isboolean()) {
         writeBool(val.checkboolean(), dos);
      } else if (val instanceof LuaString valStr) {
         dos.writeByte(4);
         writeString(valStr, dos);
      } else if (val.isint()) {
         writeInt(val.checkint(), dos);
      } else if (val.isnumber()) {
         dos.writeByte(3);
         dos.writeDouble(val.checkdouble());
      } else if (val.istable()) {
         dos.writeByte(5);
         writeTable(val.checktable(), dos);
      } else if (val.isuserdata(FiguraVector.class)) {
         writeVec((FiguraVector<?, ?>)val.checkuserdata(), dos);
      } else if (val.isuserdata(FiguraMatrix.class)) {
         writeMat((FiguraMatrix<?, ?>)val.checkuserdata(), dos);
      } else {
         dos.writeByte(0);
      }
   }

   private static void writeBool(boolean value, DataOutputStream dos) throws IOException {
      dos.writeByte(value ? 1 : 2);
   }

   private static void writeInt(int value, DataOutputStream dos) throws IOException {
      if (-128 <= value && value <= 127) {
         dos.writeByte(12);
         dos.writeByte((byte)value);
      } else if (-32768 <= value && value <= 32767) {
         dos.writeByte(13);
         dos.writeShort((short)value);
      } else if (-8388608 <= value && value < 8388608) {
         dos.writeByte(14);
         dos.writeShort((short)(value >> 8));
         dos.writeByte((byte)(value & 0xFF));
      } else {
         dos.writeByte(15);
         dos.writeInt(value);
      }
   }

   private static void writeString(LuaString string, DataOutputStream dos) throws IOException {
      int strLen = Math.min(string.length(), 65535);
      dos.writeShort((short)strLen);
      string.write(dos, 0, strLen);
   }

   private static void writeTable(LuaTable table, DataOutputStream dos) throws IOException {
      writeInt(table.keyCount(), dos);

      for (LuaValue key : table.keys()) {
         writeArg(key, dos);
         writeArg(table.get(key), dos);
      }
   }

   private static void writeVec(FiguraVector<?, ?> vector, DataOutputStream dos) throws IOException {
      dos.writeByte(switch (vector.size()) {
         case 2 -> 6;
         case 3 -> 7;
         case 4 -> 8;
         default -> throw new UnsupportedOperationException("Cannot write ping for vector size of " + vector.size());
      });

      for (int i = 0; i < vector.size(); i++) {
         dos.writeDouble(vector.index(i));
      }
   }

   private static void writeMat(FiguraMatrix<?, ?> matrix, DataOutputStream dos) throws IOException {
      dos.writeByte(switch (matrix.cols()) {
         case 2 -> 9;
         case 3 -> 10;
         case 4 -> 11;
         default -> throw new UnsupportedOperationException("Cannot write ping for matrix column of size " + matrix.cols());
      });

      for (int i = 0; i < matrix.cols(); i++) {
         FiguraVector<?, ?> vec = matrix.getColumn(i + 1);

         for (int o = 0; o < matrix.cols(); o++) {
            dos.writeDouble(vec.index(o));
         }
      }
   }

   public static LuaValue[] fromByteArray(byte[] bytes, Avatar owner) {
      try {
         DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
         List<LuaValue> luaValues = new ArrayList<>();

         while (dis.available() > 0) {
            luaValues.add(readArg(dis, owner));
         }

         return luaValues.toArray(new LuaValue[0]);
      } catch (Exception e) {
         FiguraMod.LOGGER.warn("Failed to read " + owner.owner + " ping!", e);
         return null;
      }
   }

   private static LuaValue readArg(DataInputStream dis, Avatar owner) throws IOException {
      byte type = dis.readByte();

      return (LuaValue)(switch (type) {
         case 1 -> LuaValue.valueOf(true);
         case 2 -> LuaValue.valueOf(false);
         case 3 -> LuaValue.valueOf(dis.readDouble());
         case 4 -> LuaValue.valueOf(dis.readNBytes(dis.readUnsignedShort()));
         case 5 -> readTable(dis, owner);
         case 6, 7, 8 -> owner.luaRuntime.typeManager.javaToLua(readVec(dis, type)).arg1();
         case 9, 10, 11 -> owner.luaRuntime.typeManager.javaToLua(readMat(dis, type)).arg1();
         case 12, 13, 14, 15 -> LuaValue.valueOf(readInt(dis, type));
         default -> LuaValue.NIL;
      });
   }

   private static int readInt(DataInputStream dis, byte type) throws IOException {
      return switch (type) {
         case 12 -> dis.readByte();
         case 13 -> dis.readShort();
         case 14 -> dis.readShort() << 8 | dis.readByte() & 0xFF;
         case 15 -> dis.readInt();
         default -> 0;
      };
   }

   private static LuaValue readTable(DataInputStream dis, Avatar owner) throws IOException {
      int size = readInt(dis, dis.readByte());
      LuaTable table = new LuaTable();

      for (int i = 0; i < size; i++) {
         table.set(readArg(dis, owner), readArg(dis, owner));
      }

      return table;
   }

   private static FiguraVector<?, ?> readVec(DataInputStream dis, byte type) throws IOException {
      byte size = switch (type) {
         case 6 -> 2;
         case 7 -> 3;
         case 8 -> 4;
         default -> throw new UnsupportedOperationException("Cannot read vector of unknown type " + type);
      };
      double[] array = new double[size];

      for (int i = 0; i < size; i++) {
         array[i] = dis.readDouble();
      }

      return MathUtils.sizedVector(array);
   }

   private static FiguraMatrix<?, ?> readMat(DataInputStream dis, byte type) throws IOException {
      byte size = switch (type) {
         case 9 -> 2;
         case 10 -> 3;
         case 11 -> 4;
         default -> throw new UnsupportedOperationException("Cannot read matrix of unknown type " + type);
      };
      FiguraVector<?, ?>[] vectors = new FiguraVector[size];

      for (int i = 0; i < size; i++) {
         double[] array = new double[size];

         for (int o = 0; o < size; o++) {
            array[o] = dis.readDouble();
         }

         vectors[i] = MathUtils.sizedVector(array);
      }

      return MathUtils.sizedMat(vectors);
   }
}
