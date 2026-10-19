package org.figuramc.figura.parsers;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.figuramc.figura.FiguraMod;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Block;
import org.luaj.vm2.ast.Exp;
import org.luaj.vm2.ast.FuncArgs;
import org.luaj.vm2.ast.FuncBody;
import org.luaj.vm2.ast.Name;
import org.luaj.vm2.ast.NameScope;
import org.luaj.vm2.ast.ParList;
import org.luaj.vm2.ast.Stat;
import org.luaj.vm2.ast.TableConstructor;
import org.luaj.vm2.ast.TableField;
import org.luaj.vm2.ast.Variable;
import org.luaj.vm2.ast.Visitor;
import org.luaj.vm2.ast.Exp.AnonFuncDef;
import org.luaj.vm2.ast.Exp.BinopExp;
import org.luaj.vm2.ast.Exp.Constant;
import org.luaj.vm2.ast.Exp.FieldExp;
import org.luaj.vm2.ast.Exp.IndexExp;
import org.luaj.vm2.ast.Exp.MethodCall;
import org.luaj.vm2.ast.Exp.NameExp;
import org.luaj.vm2.ast.Exp.ParensExp;
import org.luaj.vm2.ast.Exp.UnopExp;
import org.luaj.vm2.ast.Exp.VarExp;
import org.luaj.vm2.ast.Exp.VarargsExp;
import org.luaj.vm2.ast.Stat.Assign;
import org.luaj.vm2.ast.Stat.Break;
import org.luaj.vm2.ast.Stat.FuncDef;
import org.luaj.vm2.ast.Stat.GenericFor;
import org.luaj.vm2.ast.Stat.Goto;
import org.luaj.vm2.ast.Stat.IfThenElse;
import org.luaj.vm2.ast.Stat.Label;
import org.luaj.vm2.ast.Stat.LocalAssign;
import org.luaj.vm2.ast.Stat.LocalFuncDef;
import org.luaj.vm2.ast.Stat.NumericFor;
import org.luaj.vm2.ast.Stat.RepeatUntil;
import org.luaj.vm2.ast.Stat.Return;
import org.luaj.vm2.ast.Stat.WhileDo;

public class LuaScriptBuilderVisitor extends Visitor {
   private static final char[] chars = new char[63];
   private final StringBuilder builder;
   private final Map<Variable, String> vars = new HashMap<>();
   private final Stack<NameScope> scopes = new Stack<>();
   private boolean blockStandalone = false;

   public LuaScriptBuilderVisitor() {
      this(new StringBuilder());
   }

   public LuaScriptBuilderVisitor(StringBuilder builder) {
      this.builder = builder;
   }

   private static String makeName(int count) {
      StringBuilder res = new StringBuilder();
      int pow = 52;
      int i = 0;
      if (count > pow) {
         pow++;

         while (count >= pow) {
            count -= pow;
            pow *= 63;
            i++;
         }
      }

      for (int j = 0; j < i; j++) {
         res.insert(0, chars[count % 63]);
         count /= 63;
      }

      res.insert(0, chars[count]);
      return res.toString();
   }

   private void pushScope(NameScope scope) {
      label33:
      for (Variable variable : scope.namedVariables.values()) {
         if (variable.isLocal()) {
            for (Variable var : this.vars.keySet().stream().sorted(Comparator.comparing(varx -> varx.name)).toList()) {
               if (var == variable) {
                  continue label33;
               }

               if (var.name.equals(variable.name)) {
                  this.vars.put(variable, this.vars.get(var));
                  continue label33;
               }
            }

            this.vars.putIfAbsent(variable, makeName(this.vars.size()));
         }
      }

      this.scopes.push(scope);
   }

   private void popScope() {
      NameScope scope = this.scopes.pop();

      for (Variable variable : scope.namedVariables.values()) {
         if (variable.definingScope == scope) {
            this.vars.remove(variable);
         }
      }
   }

   public void visit(NameScope scope) {
      this.pushScope(scope);
   }

   public void visit(Block block) {
      boolean thisStandalone = this.blockStandalone;
      this.blockStandalone = true;
      if (thisStandalone) {
         this.newlineIfName("do");
      }

      LuaScriptBuilderVisitor.ScopedBody b = new LuaScriptBuilderVisitor.ScopedBody(block.scope);

      try {
         if (block.stats != null) {
            for (Stat element : block.stats) {
               element.accept(this);
            }
         }
      } catch (Throwable var7) {
         try {
            b.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      b.close();
      if (thisStandalone) {
         this.newlineIfName("end");
      }
   }

   public void visit(Assign stat) {
      this.visitVars(stat.vars);
      this.builder.append('=');
      this.visitExps(stat.exps);
   }

   public void visit(Break breakstat) {
      this.newlineIfName("break");
   }

   public void visit(FuncDef stat) {
      this.newlineIfName("function");
      if (stat.name.name != null) {
         this.visit(stat.name.name);
      }

      if (stat.name.dots != null) {
         for (String s : stat.name.dots) {
            this.builder.append(".").append(s);
         }
      }

      if (stat.name.method != null) {
         this.builder.append(":").append(stat.name.method);
      }

      stat.body.accept(this);
   }

   public void visit(GenericFor stat) {
      try (LuaScriptBuilderVisitor.ScopedBody b = new LuaScriptBuilderVisitor.ScopedBody(stat.scope)) {
         this.newlineIfName("for");
         this.visitNames(stat.names);
         this.spaceIfName("in");
         this.visitExps(stat.exps);
         this.spaceIfName("do");
         this.blockStandalone = false;
         stat.block.accept(this);
         this.newlineIfName("end");
      }
   }

   public void visit(IfThenElse stat) {
      this.newlineIfName("if");
      stat.ifexp.accept(this);
      this.spaceIfName("then");
      this.blockStandalone = false;
      stat.ifblock.accept(this);
      if (stat.elseifblocks != null) {
         int i = 0;

         for (int n = stat.elseifblocks.size(); i < n; i++) {
            this.newlineIfName("elseif");
            ((Exp)stat.elseifexps.get(i)).accept(this);
            this.spaceIfName("then");
            this.blockStandalone = false;
            ((Block)stat.elseifblocks.get(i)).accept(this);
         }
      }

      if (stat.elseblock != null) {
         this.newlineIfName("else");
         this.blockStandalone = false;
         stat.elseblock.accept(this);
      }

      this.newlineIfName("end");
   }

   public void visit(LocalAssign stat) {
      this.newlineIfName("local");
      this.visitNames(stat.names);
      if (stat.values != null) {
         this.builder.append('=');
      }

      this.visitExps(stat.values);
   }

   public void visit(LocalFuncDef stat) {
      this.newlineIfName("local function");
      super.visit(stat);
   }

   public void visit(NumericFor stat) {
      try (LuaScriptBuilderVisitor.ScopedBody b = new LuaScriptBuilderVisitor.ScopedBody(stat.scope)) {
         this.newlineIfName("for ");
         this.visit(stat.name);
         this.builder.append("=");
         stat.initial.accept(this);
         this.builder.append(",");
         stat.limit.accept(this);
         if (stat.step != null) {
            this.builder.append(",");
            stat.step.accept(this);
         }

         this.spaceIfName("do");
         this.blockStandalone = false;
         stat.block.accept(this);
         this.newlineIfName("end");
      }
   }

   public void visit(RepeatUntil stat) {
      this.newlineIfName("repeat");
      this.blockStandalone = false;
      stat.block.accept(this);
      this.newlineIfName("until");
      stat.exp.accept(this);
   }

   public void visit(Return stat) {
      this.newlineIfName("return");
      super.visit(stat);
   }

   public void visit(WhileDo stat) {
      this.newlineIfName("while");
      stat.exp.accept(this);
      this.spaceIfName("do");
      this.blockStandalone = false;
      stat.block.accept(this);
      this.newlineIfName("end");
   }

   public void visit(FuncBody body) {
      try (LuaScriptBuilderVisitor.ScopedBody b = new LuaScriptBuilderVisitor.ScopedBody(body.scope)) {
         body.parlist.accept(this);
         this.blockStandalone = false;
         body.block.accept(this);
         this.newlineIfName("end");
      }
   }

   public void visit(FuncArgs args) {
      List<Exp> exps = args.exps;
      if (exps != null && exps.size() == 1 && exps.get(0) instanceof Constant constant && constant.value instanceof LuaString) {
         constant.accept(this);
      } else {
         this.builder.append("(");
         super.visit(args);
         this.builder.append(")");
      }
   }

   public void visit(TableField field) {
      if (field.name != null || field.index != null) {
         if (field.name != null) {
            this.visit(field.name);
         } else {
            this.builder.append("[");
            field.index.accept(this);
            this.builder.append("]");
         }

         this.builder.append('=');
      }

      field.rhs.accept(this);
   }

   public void visit(AnonFuncDef exp) {
      this.newlineIfName("function");
      super.visit(exp);
   }

   public void visit(BinopExp exp) {
      exp.lhs.accept(this);
      switch (exp.op) {
         case 13:
            this.builder.append("+");
            break;
         case 14:
            this.builder.append("-");
            break;
         case 15:
            this.builder.append("*");
            break;
         case 16:
            this.builder.append("/");
            break;
         case 17:
            this.builder.append("%");
            break;
         case 18:
            this.builder.append("^");
            break;
         case 19:
         case 20:
         case 21:
         case 23:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         default:
            throw new IllegalStateException("unhandled operator: " + exp.op);
         case 22:
            this.builder.append("..");
            break;
         case 24:
            this.builder.append("==");
            break;
         case 25:
            this.builder.append("<");
            break;
         case 26:
            this.builder.append("<=");
            break;
         case 59:
            this.spaceIfName("or");
            break;
         case 60:
            this.spaceIfName("and");
            break;
         case 61:
            this.builder.append("~=");
            break;
         case 62:
            this.builder.append(">=");
            break;
         case 63:
            this.builder.append(">");
      }

      exp.rhs.accept(this);
   }

   public void visit(Constant exp) {
      LuaValue value = exp.value;
      if (value instanceof LuaString str) {
         String input = new String(str.m_bytes, str.m_offset, str.m_length, StandardCharsets.UTF_8);
         int sdq = 0;

         for (char c : input.toCharArray()) {
            if (c == '\'') {
               sdq--;
            }

            if (c == '"') {
               sdq++;
            }
         }

         char quote = (char)(sdq <= 0 ? 34 : 39);
         input = input.replaceAll("\\r(?=\\n)", "");
         input = input.replaceAll("\\r", "\n");
         input = input.replaceAll("\\\\", "\\\\\\\\");
         input = input.replaceAll("\\n", "\\\\n");
         input = input.replaceAll(String.valueOf(quote), "\\\\" + quote);
         this.builder.append(quote).append(input).append(quote);
      } else {
         this.spaceIfName(String.valueOf(value));
      }
   }

   public void visit(FieldExp exp) {
      exp.lhs.accept(this);
      this.builder.append(".");
      this.visit(exp.name);
   }

   public void visit(IndexExp exp) {
      exp.lhs.accept(this);
      this.builder.append("[");
      exp.exp.accept(this);
      this.builder.append("]");
   }

   public void visit(MethodCall exp) {
      exp.lhs.accept(this);
      this.builder.append(":").append(exp.name);
      exp.args.accept(this);
   }

   public void visit(NameExp exp) {
      this.visit(exp.name);
   }

   public void visit(ParensExp exp) {
      this.builder.append("(");
      super.visit(exp);
      this.builder.append(")");
   }

   public void visit(UnopExp exp) {
      switch (exp.op) {
         case 19:
            this.builder.append("-");
            break;
         case 20:
            this.spaceIfName("not");
            break;
         case 21:
            this.builder.append("#");
            break;
         default:
            throw new IllegalStateException("unhandled op " + exp.op);
      }

      super.visit(exp);
   }

   public void visit(VarargsExp exp) {
      this.builder.append("...");
   }

   public void visit(ParList pars) {
      this.builder.append("(");
      super.visit(pars);
      if (pars.isvararg) {
         if (pars.names != null && !pars.names.isEmpty()) {
            this.builder.append(',');
         }

         this.builder.append("...");
      }

      this.builder.append(")");
   }

   public void visit(TableConstructor table) {
      this.builder.append("{");
      if (table.fields != null) {
         Iterator<TableField> iterator = table.fields.iterator();

         while (iterator.hasNext()) {
            iterator.next().accept(this);
            if (iterator.hasNext()) {
               this.builder.append(";");
            }
         }
      }

      this.builder.append("}");
   }

   public void visitVars(List<VarExp> vars) {
      if (vars != null) {
         Iterator<VarExp> iterator = vars.iterator();

         while (iterator.hasNext()) {
            iterator.next().accept(this);
            if (iterator.hasNext()) {
               this.builder.append(",");
            }
         }
      }
   }

   public void visitExps(List<Exp> exps) {
      if (exps != null) {
         Iterator<Exp> iterator = exps.iterator();

         while (iterator.hasNext()) {
            iterator.next().accept(this);
            if (iterator.hasNext()) {
               this.builder.append(",");
            }
         }
      }
   }

   public void visitNames(List<Name> names) {
      if (names != null) {
         this.spaceIfName();
         Iterator<Name> iterator = names.iterator();

         while (iterator.hasNext()) {
            this.visit(iterator.next());
            if (iterator.hasNext()) {
               this.builder.append(",");
            }
         }
      }
   }

   public void visit(Name name) {
      this.visit(this.vars.getOrDefault(name.variable != null && name.variable.isLocal() ? name.variable : null, name.name));
   }

   public void visit(String name) {
      this.spaceIfName(name);
   }

   public void visit(Goto gotostat) {
      this.newlineIfName("goto ").append(gotostat.name);
   }

   public void visit(Label label) {
      this.builder.append("::").append(label.name).append("::");
   }

   private StringBuilder newlineIfName(String next) {
      return this.charIfName(next, (char)(FiguraMod.debugModeEnabled() ? '\n' : ' '));
   }

   private void spaceIfName() {
      this.spaceIfName("");
   }

   private StringBuilder spaceIfName(String next) {
      return this.charIfName(next, ' ');
   }

   private StringBuilder charIfName(String next, char c) {
      int length = this.builder.length();
      char ch = length > 0 ? this.builder.charAt(length - 1) : '\u0000';
      if (length > 0 && (ch == '_' || Character.isLetterOrDigit(ch))) {
         this.builder.append(c);
      }

      return this.builder.append(next);
   }

   public String getString() {
      return this.builder.toString();
   }

   public int length() {
      return this.builder.length();
   }

   static {
      chars[0] = '_';

      for (int i = 0; i < 26; i++) {
         chars[1 + i] = (char)(97 + i);
      }

      for (int i = 0; i < 26; i++) {
         chars[27 + i] = (char)(65 + i);
      }

      for (int i = 0; i < 10; i++) {
         chars[53 + i] = (char)(48 + i);
      }
   }

   private class ScopedBody implements AutoCloseable {
      private final boolean push;

      ScopedBody(NameScope scope) {
         this.push = scope != null;
         if (this.push) {
            LuaScriptBuilderVisitor.this.pushScope(scope);
         }
      }

      @Override
      public void close() {
         if (this.push) {
            LuaScriptBuilderVisitor.this.popScope();
         }
      }
   }
}
