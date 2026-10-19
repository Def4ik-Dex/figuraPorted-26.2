package org.figuramc.figura.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
   private static final Pattern PATTERN = Pattern.compile(
      "^(?<major>0|[1-9]\\d*)(?:\\.(?<minor>0|[1-9]\\d*)(?:\\.(?<patch>0|[1-9]\\d*))?)?(?:-(?<pre>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][\\da-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][\\da-zA-Z-]*))*))?(?:\\+(?<build>[\\da-zA-Z-]+(?:\\.[\\da-zA-Z-]+)*))?$"
   );
   private final String src;
   public final int major;
   public final int minor;
   public final int patch;
   public final String pre;
   public final String build;
   public final boolean invalid;

   public Version(String version) {
      int major = 0;
      int minor = 0;
      int patch = 0;
      String pre = null;
      String build = null;
      boolean invalid = true;

      try {
         Matcher matcher = PATTERN.matcher(version);
         if (matcher.matches()) {
            major = Integer.parseInt(matcher.group("major"));
            String m = matcher.group("minor");
            if (m != null) {
               minor = Integer.parseInt(m);
            }

            String p = matcher.group("patch");
            if (p != null) {
               patch = Integer.parseInt(p);
            }

            pre = matcher.group("pre");
            build = matcher.group("build");
            invalid = false;
         }
      } catch (Exception var11) {
      }

      this.src = version;
      this.major = major;
      this.minor = minor;
      this.patch = patch;
      this.pre = pre == null ? "" : pre;
      this.build = build == null ? "" : build;
      this.invalid = invalid;
   }

   public int compareTo(Version o) {
      if (!this.invalid && !o.invalid) {
         int ret;
         if (this.major != o.major) {
            ret = this.major - o.major;
         } else if (this.minor != o.minor) {
            ret = this.minor - o.minor;
         } else if (this.patch != o.patch) {
            ret = this.patch - o.patch;
         } else if (this.pre.isBlank() && !o.pre.isBlank()) {
            ret = 1;
         } else if (!this.pre.isBlank() && o.pre.isBlank()) {
            ret = -1;
         } else {
            String[] split1 = this.pre.split("\\.");
            String[] split2 = o.pre.split("\\.");
            ret = 0;

            for (int i = 0; i < split1.length; i++) {
               if (i >= split2.length) {
                  ret = 1;
                  break;
               }

               String s1 = split1[i];
               String s2 = split2[i];

               try {
                  int i1 = Integer.parseInt(s1);
                  int i2 = Integer.parseInt(s2);
                  ret = i1 - i2;
               } catch (Exception ignored) {
                  ret = s1.compareTo(s2);
               }

               if (ret != 0) {
                  break;
               }
            }

            if (split1.length < split2.length) {
               ret = -1;
            }
         }

         return (int)Math.signum(ret);
      } else {
         return 0;
      }
   }

   public String noBuildString() {
      if (this.invalid) {
         return this.src;
      }

      String ver = this.major + "." + this.minor + "." + this.patch;
      if (!this.pre.isBlank()) {
         ver = ver + "-" + this.pre;
      }

      return ver;
   }

   @Override
   public String toString() {
      if (this.invalid) {
         return this.src;
      }

      String ver = this.major + "." + this.minor + "." + this.patch;
      if (!this.pre.isBlank()) {
         ver = ver + "-" + this.pre;
      }

      if (!this.build.isBlank()) {
         ver = ver + "+" + this.build;
      }

      return ver;
   }
}
