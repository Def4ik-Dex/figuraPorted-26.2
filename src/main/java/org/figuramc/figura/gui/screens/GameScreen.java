package org.figuramc.figura.gui.screens;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.Label;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.joml.Matrix3x2fStack;

public class GameScreen extends AbstractPanelScreen {
   private static final int[][] RULES = new int[][]{{0, 0, 0, 1, 0, 0, 0, 0, 0}, {0, 0, 1, 1, 0, 0, 0, 0, 0}};
   private Label keys;
   private Label stats;
   private GameScreen.Grid grid;
   private boolean paused = false;
   private static int scale = 5;

   protected GameScreen(Screen parentScreen) {
      super(parentScreen, Component.empty());
   }

   @Override
   public Class<? extends Screen> getSelectedPanel() {
      return (Class<? extends Screen>)this.parentScreen.getClass();
   }

   @Override
   protected void init() {
      super.init();
      this.removeWidget(this.panels);
      this.addRenderableOnly(this.grid = new GameScreen.Grid(this.width, this.height));
      this.addRenderableWidget(
         new Button(
            this.width - 20, 4, 16, 16, 0, 0, 16, FiguraIdentifier.of("textures/gui/search_clear.png"), 48, 16, FiguraText.of("gui.done"), bx -> this.onClose()
         )
      );
      this.addRenderableWidget(
         this.keys = new Label(
            Component.empty()
               .append(Component.literal("[R]").withStyle(FiguraMod.getAccentColor()))
               .append(" restart, ")
               .append(Component.literal("[P]").withStyle(FiguraMod.getAccentColor()))
               .append(" pause, ")
               .append(Component.literal("[SPACE]").withStyle(FiguraMod.getAccentColor()))
               .append(" step")
               .append("\n")
               .append(Component.literal("[F1]").withStyle(FiguraMod.getAccentColor()))
               .append(" hide text, ")
               .append(Component.literal("[Scroll]").withStyle(FiguraMod.getAccentColor()))
               .append(" scale (restarts)"),
            4,
            4,
            0
         )
      );
      this.addRenderableWidget(this.stats = new Label("", 4, this.keys.getRawY() + this.keys.getHeight(), 0));
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.paused) {
         this.grid.tick();
      }

      this.stats
         .setText(
            Component.literal("Generation")
               .append(Component.literal(" " + this.grid.gen).withStyle(FiguraMod.getAccentColor()))
               .append(", Scale")
               .append(Component.literal(" " + scale).withStyle(FiguraMod.getAccentColor()))
         );
   }

   @Override
   public boolean keyPressed(KeyEvent keyEvent) {
      int keyCode = keyEvent.key();
      switch (keyCode) {
         case 32:
            this.grid.tick();
            break;
         case 80:
            this.paused = !this.paused;
            break;
         case 82:
            this.grid.init();
            break;
         case 290:
            this.keys.setVisible(!this.keys.isVisible());
            this.stats.setVisible(!this.stats.isVisible());
            break;
         default:
            return super.keyPressed(keyEvent);
      }

      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
      scale = (int)Math.max(1.0, scale + Math.signum(amount));
      this.grid.init();
      return true;
   }

   private static class Cell {
      private final int x;
      private final int y;
      private int alive;
      private int future;
      private final ArrayList<GameScreen.Cell> neighbors = new ArrayList<>();
      private int color = -1;

      private Cell(int x, int y, int alive) {
         this.x = x;
         this.y = y;
         this.alive = alive;
         this.future = alive;
      }

      private void addNeighbor(GameScreen.Cell cell) {
         this.neighbors.add(cell);
         cell.neighbors.add(this);
      }

      private void update() {
         int neigh = 0;

         for (GameScreen.Cell cell : this.neighbors) {
            neigh += cell.alive;
         }

         this.future = GameScreen.RULES[this.alive][neigh];
      }

      private void render(GuiGraphicsExtractor gui) {
         this.alive = this.future;
         if (this.alive == 1) {
            gui.fill(this.x, this.y, this.x + 1, this.y + 1, this.color);
         }
      }
   }

   private static class Grid implements Renderable {
      private GameScreen.Cell[][] grid;
      private final int width;
      private final int height;
      private long gen = 0L;

      private Grid(int width, int height) {
         this.width = width;
         this.height = height;
         this.init();
      }

      private void init() {
         this.gen = 0L;
         int width = this.width / GameScreen.scale;
         int height = this.height / GameScreen.scale;
         this.grid = new GameScreen.Cell[width][height];

         for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
               GameScreen.Cell cell = new GameScreen.Cell(i, j, (int)Math.round(Math.random()));
               if (i > 0) {
                  if (j > 0) {
                     cell.addNeighbor(this.grid[i - 1][j - 1]);
                  }

                  cell.addNeighbor(this.grid[i - 1][j]);
                  if (j < height - 1) {
                     cell.addNeighbor(this.grid[i - 1][j + 1]);
                  }
               }

               if (j > 0) {
                  cell.addNeighbor(this.grid[i][j - 1]);
               }

               this.grid[i][j] = cell;
            }
         }
      }

      private void tick() {
         this.gen++;

         for (GameScreen.Cell[] cells : this.grid) {
            for (GameScreen.Cell cell : cells) {
               cell.update();
            }
         }
      }

      public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
         Matrix3x2fStack pose = gui.pose();
         pose.pushMatrix();
         pose.scale(GameScreen.scale, GameScreen.scale);

         for (GameScreen.Cell[] cells : this.grid) {
            for (GameScreen.Cell cell : cells) {
               cell.render(gui);
            }
         }

         pose.popMatrix();
      }
   }
}
