/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/

package org.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;

import android.os.Bundle;
import android.os.Parcelable;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;


public class PuzzleView extends View {
   
   private static final String TAG = "Sudoku";

   
   private static final String SELX = "selX"; 
   private static final String SELY = "selY";
   private static final String VIEW_STATE = "viewState";
   private static final int ID = 42; 

   
   private float width;    // width of one tile
   private float height;   // height of one tile
   private int selX;       // X index of selection
   private int selY;       // Y index of selection
   private final Rect selRect = new Rect();

   private final Game game;
   
   public PuzzleView(Context context) {
      
      super(context);
      this.game = (Game) context;
      setFocusable(true);
      setFocusableInTouchMode(true);
      
      // ...
      setId(ID); 
   }

   @Override
   protected Parcelable onSaveInstanceState() { 
      Parcelable p = super.onSaveInstanceState();
      Log.d(TAG, "onSaveInstanceState");
      Bundle bundle = new Bundle();
      bundle.putInt(SELX, selX);
      bundle.putInt(SELY, selY);
      bundle.putParcelable(VIEW_STATE, p);
      return bundle;
   }
   @Override
   protected void onRestoreInstanceState(Parcelable state) { 
      Log.d(TAG, "onRestoreInstanceState");
      Bundle bundle = (Bundle) state;
      select(bundle.getInt(SELX), bundle.getInt(SELY));
      super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
   }
   

   @Override
   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      width = w / 9f;
      height = h / 9f;
      getRect(selX, selY, selRect);
      Log.d(TAG, "onSizeChanged: width " + width + ", height "
            + height);
      super.onSizeChanged(w, h, oldw, oldh);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      // Draw the background...
      Paint background = new Paint();
      background.setColor(getResources().getColor(
            R.color.puzzle_background));
      canvas.drawRect(0, 0, getWidth(), getHeight(), background);

      
      // Draw the board...
      
      // Define colors for the grid lines
      Paint dark = new Paint();
      dark.setColor(getResources().getColor(R.color.puzzle_dark));

      Paint hilite = new Paint();
      hilite.setColor(getResources().getColor(R.color.puzzle_hilite));

      Paint light = new Paint();
      light.setColor(getResources().getColor(R.color.puzzle_light));

      // Draw the minor grid lines
      for (int i = 0; i < 9; i++) {
         canvas.drawLine(0, i * height, getWidth(), i * height,
               light);
         canvas.drawLine(0, i * height + 1, getWidth(), i * height
               + 1, hilite);
         canvas.drawLine(i * width, 0, i * width, getHeight(),
               light);
         canvas.drawLine(i * width + 1, 0, i * width + 1,
               getHeight(), hilite);
      }

      // Draw the major grid lines
      for (int i = 0; i < 9; i++) {
         if (i % 3 != 0)
            continue;
         canvas.drawLine(0, i * height, getWidth(), i * height,
               dark);
         canvas.drawLine(0, i * height + 1, getWidth(), i * height
               + 1, hilite);
         canvas.drawLine(i * width, 0, i * width, getHeight(), dark);
         canvas.drawLine(i * width + 1, 0, i * width + 1,
               getHeight(), hilite);
      }

      // Draw the numbers...
      // Define color and style for numbers
      Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
      foreground.setColor(getResources().getColor(
            R.color.puzzle_foreground));
      foreground.setStyle(Style.FILL);
      foreground.setTextSize(height * 0.75f);
      foreground.setTextScaleX(width / height);
      foreground.setTextAlign(Paint.Align.CENTER);

      // Draw the number in the center of the tile
      FontMetrics fm = foreground.getFontMetrics();
      // Centering in X: use alignment (and X at midpoint)
      float x = width / 2;
      // Centering in Y: measure ascent/descent first
      float y = height / 2 - (fm.ascent + fm.descent) / 2;
      for (int i = 0; i < 9; i++) {
         for (int j = 0; j < 9; j++) {
            canvas.drawText(this.game.getTileString(i, j), i
                  * width + x, j * height + y, foreground);
         }
      }

      
      if (Prefs.getHints(getContext())) {
         // Draw the hints...
         
         // Pick a hint color based on #moves left
         Paint hint = new Paint();
         int c[] = { getResources().getColor(R.color.puzzle_hint_0),
               getResources().getColor(R.color.puzzle_hint_1),
               getResources().getColor(R.color.puzzle_hint_2), };
         Rect r = new Rect();
         for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
               int movesleft = 9 - game.getUsedTiles(i, j).length;
               if (movesleft < c.length) {
                  getRect(i, j, r);
                  hint.setColor(c[movesleft]);
                  canvas.drawRect(r, hint);
               }
            }
         }
         
      }
      

      // Draw the selection...
      Log.d(TAG, "selRect=" + selRect);
      Paint selected = new Paint();
      selected.setColor(getResources().getColor(
            R.color.puzzle_selected));
      canvas.drawRect(selRect, selected);
   }

   @Override
   public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() != MotionEvent.ACTION_DOWN)
         return super.onTouchEvent(event);

      select((int) (event.getX() / width),
            (int) (event.getY() / height));
      game.showKeypadOrError(selX, selY);
      Log.d(TAG, "onTouchEvent: x " + selX + ", y " + selY);
      return true;
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event="
            + event);
      switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_UP:
         select(selX, selY - 1);
         break;
      case KeyEvent.KEYCODE_DPAD_DOWN:
         select(selX, selY + 1);
         break;
      case KeyEvent.KEYCODE_DPAD_LEFT:
         select(selX - 1, selY);
         break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
         select(selX + 1, selY);
         break;
      case KeyEvent.KEYCODE_0:
      case KeyEvent.KEYCODE_SPACE: setSelectedTile(0); break;
      case KeyEvent.KEYCODE_1:     setSelectedTile(1); break;
      case KeyEvent.KEYCODE_2:     setSelectedTile(2); break;
      case KeyEvent.KEYCODE_3:     setSelectedTile(3); break;
      case KeyEvent.KEYCODE_4:     setSelectedTile(4); break;
      case KeyEvent.KEYCODE_5:     setSelectedTile(5); break;
      case KeyEvent.KEYCODE_6:     setSelectedTile(6); break;
      case KeyEvent.KEYCODE_7:     setSelectedTile(7); break;
      case KeyEvent.KEYCODE_8:     setSelectedTile(8); break;
      case KeyEvent.KEYCODE_9:     setSelectedTile(9); break;
      case KeyEvent.KEYCODE_ENTER:
      case KeyEvent.KEYCODE_DPAD_CENTER:
         game.showKeypadOrError(selX, selY);
         break;
      default:
         return super.onKeyDown(keyCode, event);
      }
      return true;
   }

   public void setSelectedTile(int tile) {
      if (game.setTileIfValid(selX, selY, tile)) {
         invalidate();// may change hints
      } else {
         // Number is not valid for this tile
         Log.d(TAG, "setSelectedTile: invalid: " + tile);
         startAnimation(AnimationUtils.loadAnimation(game,
               R.anim.shake));
      }
   }

   private void select(int x, int y) {
      invalidate(selRect);
      selX = Math.min(Math.max(x, 0), 8);
      selY = Math.min(Math.max(y, 0), 8);
      getRect(selX, selY, selRect);
      invalidate(selRect);
   }

   private void getRect(int x, int y, Rect rect) {
      rect.set((int) (x * width), (int) (y * height), (int) (x
            * width + width), (int) (y * height + height));
   }
   
   // ...
}

