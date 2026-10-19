package org.figuramc.figura.gui.widgets.avatar;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FileTexture;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

public class AvatarWidget extends AbstractAvatarWidget {
   public static final Identifier MISSING_ICON = FiguraIdentifier.of("textures/gui/unknown_icon.png");

   public AvatarWidget(final int depth, int width, final LocalAvatarFetcher.AvatarPath avatar, final AvatarList parent) {
      super(depth, width, 24, avatar, parent);
      final AvatarWidget instance = this;
      final Component description = Emojis.applyEmojis(Component.literal(avatar.getDescription()));
      this.button = new Button(this.getX(), this.getY(), width, 24, this.getName(), null, button -> {
         AvatarManager.loadLocalAvatar(avatar.getPath());
         AvatarList.selectedEntry = avatar.getTheActualPathForThis();
      }) {
         @Override
         public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
            super.extractContents(gui, mouseX, mouseY, delta);
            if (instance.isOf(AvatarList.selectedEntry)) {
               UIHelper.fillOutline(gui, this.getX(), this.getY(), this.getWidth(), this.getHeight(), -1);
            }
         }

         @Override
         protected void renderText(GuiGraphicsExtractor gui, float delta) {
            Font font = Minecraft.getInstance().font;
            int space = 6 * depth;
            int widthx = this.getWidth() - 26 - space;
            int x = this.getX() + 2 + space;
            int y = this.getY() + 2;
            FileTexture texture = avatar.getIcon();
            Identifier icon = texture == null ? AvatarWidget.MISSING_ICON : texture.getLocation();
            UIHelper.blit(gui, x, y, 20, 20, icon);
            Component parsedName = TextUtils.trimToWidthEllipsis(
               font, this.getMessage(), widthx, TextUtils.ELLIPSIS.copy().withStyle(this.getMessage().getStyle())
            );
            gui.text(font, parsedName, x + 22, y, UIHelper.adjustColor(-1));
            Component parsedDescription = TextUtils.trimToWidthEllipsis(font, description, widthx, TextUtils.ELLIPSIS.copy().withStyle(description.getStyle()));
            gui.text(font, parsedDescription, x + 22, y + 9 + 1, UIHelper.adjustColor(TextColor.GRAY.getValue()));
            if (parsedName != this.getMessage() || parsedDescription != description) {
               Component tooltip = instance.getName();
               if (!description.getString().isBlank()) {
                  tooltip = tooltip.copy().append("\n\n").append(description);
               }

               this.setTooltip(tooltip);
            }
         }

         @Override
         public boolean isMouseOver(double mouseX, double mouseY) {
            return parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
         }

         @Override
         public void setHovered(boolean hovered) {
            if (!hovered && UIHelper.getContext() == AvatarWidget.this.context && AvatarWidget.this.context.isVisible()) {
               hovered = true;
            }

            super.setHovered(hovered);
         }
      };
      this.button.shouldHaveBackground(false);
      this.children.add(this.button);
   }
}
