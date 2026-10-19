package org.figuramc.figura.font;

import com.google.gson.JsonObject;
import org.figuramc.figura.utils.JsonUtils;

public class EmojiMetadata {
   public final int frames;
   public final int frameTime;
   public final int width;
   private int frameTimer;
   private int curFrame;

   public EmojiMetadata(int frames, int frameTime, int width) {
      this.frames = frames;
      this.frameTime = frameTime;
      this.width = width;
   }

   public EmojiMetadata(JsonObject entry) {
      this(entry.get("frames").getAsInt(), entry.get("frametime").getAsInt(), JsonUtils.getIntOrDefault(entry, "width", 8));
   }

   public void tickAnimation() {
      this.frameTimer++;
      if (this.frameTimer >= this.frameTime) {
         this.frameTimer = this.frameTimer - this.frameTime;
         this.curFrame = (this.curFrame + 1) % this.frames;
      }
   }

   public int getCurrentFrame() {
      return this.curFrame;
   }
}
