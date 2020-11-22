package org.thoughtcrime.securesms.components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import org.thoughtcrime.securesms.R;

import java.sql.Time;

public class TypingIndicatorView extends LinearLayout {

  class TypingDot
  {
    public View dot;
    public int id;
    public boolean up;

    TypingDot(int id)
    {
      this.id = id;
    }
  };

  private static final long DURATION   = 300;
  private static final long PRE_DELAY  = 500;
  private static final long POST_DELAY = 500;
  private static final long  CYCLE_DURATION = 1500;
  private static final long  DOT_DURATION   = 600;
  private static final float MIN_ALPHA      = 0.4f;
  private static final float MIN_SCALE      = 0.75f;

  private boolean isActive;
  private long    startTime;

  private TypingDot dot1 = new TypingDot(1);
  private TypingDot dot2 = new TypingDot(2);
  private TypingDot dot3 = new TypingDot(3);

  public TypingIndicatorView(Context context) {
    super(context);
    initialize(null);
  }

  public TypingIndicatorView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize(attrs);
  }

  private void initialize(@Nullable AttributeSet attrs) {
    int nightModeFlag = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    int tint = 0;

    inflate(getContext(), R.layout.typing_indicator_view, this);

    setWillNotDraw(false);

    dot1.dot = findViewById(R.id.typing_dot1);
    dot1.up = false;

    dot2.dot = findViewById(R.id.typing_dot2);
    dot2.up = false;

    dot3.dot = findViewById(R.id.typing_dot3);
    dot3.up = false;

    if (attrs != null) {
      TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.TypingIndicatorView, 0, 0);
      switch (nightModeFlag)
      {
        case Configuration.UI_MODE_NIGHT_YES:
          tint = typedArray.getColor(R.styleable.TypingIndicatorView_typingIndicator_tint, Color.WHITE);
          break;
        case Configuration.UI_MODE_NIGHT_NO:
          tint = typedArray.getColor(R.styleable.TypingIndicatorView_typingIndicator_tint, Color.BLACK);
          break;
      }

      typedArray.recycle();

      // make typing dots color the color of the conversation

      dot1.dot.getBackground().setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
      dot2.dot.getBackground().setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
      dot3.dot.getBackground().setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (!isActive) {
      super.onDraw(canvas);
      return;
    }

    long timeInCycle = (System.currentTimeMillis() - startTime) % CYCLE_DURATION;

    render(dot1, timeInCycle, 0);
    render(dot2, timeInCycle, 150);
    render(dot3, timeInCycle, 300);

    super.onDraw(canvas);
    postInvalidate();
  }

  private void render(TypingDot dot, long timeInCycle, long start) {
    long end  = start + DOT_DURATION;
    long peak = start + (DOT_DURATION / 2);

    if (timeInCycle < start || timeInCycle > end)
    {
      renderDefault(dot);
    }
    else if (timeInCycle < peak)
    {
      if (!dot.up)
      {
        renderBounceUp(dot, timeInCycle);
      }
    }
    else
    {
      if (dot.up)
      {
        renderBounceDown(dot, timeInCycle);
      }
    }
  }

  private void renderBounceUp(TypingDot dot, long timeInCycle) { // added by KingCurry9812
    Log.d("yparam", dot.id+" up");

    ObjectAnimator move_up = ObjectAnimator.ofFloat(dot.dot, "translationY", 15f);
    move_up.setDuration(500);
    move_up.start();

    dot.up = true;
  }

  private void renderBounceDown(TypingDot dot, long timeInCycle) { // added by KingCurry9812
    Log.d("yparam", dot.id+" down");

    ObjectAnimator move_down = ObjectAnimator.ofFloat(dot.dot, "translationY", -15f);
    move_down.setDuration(500);
    move_down.start();

    dot.up = false;
  }

  private void renderDefault(TypingDot dot) {
    ObjectAnimator move_up = ObjectAnimator.ofFloat(dot.dot, "translationY", 100f);
    move_up.setDuration(500);
    move_up.start();

//    if (!dot.up)
//    {
//      ObjectAnimator move_up = ObjectAnimator.ofFloat(dot.dot, "translationY", 100f);
//      move_up.setDuration(500);
//      move_up.start();
//    }
//    else
//    {
//      ObjectAnimator move_down = ObjectAnimator.ofFloat(dot.dot, "translationY", -100f);
//      move_down.setDuration(500);
//      move_down.start();
//    }
  }

  private void renderFadeIn(View dot, long timeInCycle, long fadeInStart) {
    float percent = (float) (timeInCycle - fadeInStart) / 300;
    dot.setAlpha(MIN_ALPHA + (1 - MIN_ALPHA) * percent);
    dot.setScaleX(MIN_SCALE + (1 - MIN_SCALE) * percent);
    dot.setScaleY(MIN_SCALE + (1 - MIN_SCALE) * percent);
  }

  private void renderFadeOut(View dot, long timeInCycle, long fadeOutStart) {
    float percent = (float) (timeInCycle - fadeOutStart) / 300;
    dot.setAlpha(1 - (1 - MIN_ALPHA) * percent);
    dot.setScaleX(1 - (1 - MIN_SCALE) * percent);
    dot.setScaleY(1 - (1 - MIN_SCALE) * percent);
  }

  public void startAnimation() {
    isActive  = true;
    startTime = System.currentTimeMillis();

    postInvalidate();
  }

  public void stopAnimation() {
    isActive = false;
  }
}
