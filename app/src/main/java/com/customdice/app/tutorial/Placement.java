package com.customdice.app.tutorial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.customdice.app.R;

/**
 * Created by Benjamin on 6/9/2015.
 *
 *
 */
public class Placement extends RelativeLayout {

    final private ImageView triangle;
    final private Context context;

    TextView textView;

    private final int DELAY = 300;

    private CharSequence newText = "";

    public Placement(Context context) {
        super(context);

        this.context = context;

        textView = (TextView) LayoutInflater.from(context).inflate(R.layout.demo_textview, null, false);

        triangle = (ImageView) LayoutInflater.from(context).inflate(R.layout.triangle, null, false);
    }

    // User interface
    public void setTarget(View view) {
        clearViews(view);
    }

    // Run after views have been removed
    private void setTargetView(View view) {
        Targetter targetter = new Targetter(view);

        textView.setText(newText);

        //Find whether I should getTop or getBot

        Point point = targetter.getBot();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if(newText.length() > 150) {
            textView.setMaxWidth(size.x * 8 / 10);
        } else if(size.x * 2 / 3 < 400) {
            textView.setMaxWidth(size.x * 2 / 3);
        } else {
            textView.setMaxWidth(400);
        }

        //System.out.println("MA| text: " + textView.getText());

        textView.measure(getMeasuredWidth(), getMeasuredHeight());

        /*System.out.println("Pmt| getmeas. width: " + getMeasuredWidth());
        System.out.println("Pmt| getmeas. height: " + getMeasuredHeight());

        System.out.println("Pmt| meas. width: " + textView.getMeasuredWidth());
        System.out.println("Pmt| meas. height: " + textView.getMeasuredHeight());

        System.out.println("Pmt| point x, y: " + point.x + ", " + point.y);

        System.out.println("Pmt| size.x: " + size.x);
        System.out.println("Pmt| size.y: " + size.y);*/


       LayoutParams params = new LayoutParams(textView.getMeasuredWidth(), textView.getMeasuredHeight());
        if (point.x - textView.getMeasuredWidth() / 2 < 0) {
            params.leftMargin = 10; // temporary margin
        } else if(point.x + textView.getMeasuredWidth() / 2 > size.x) {
            params.leftMargin = size.x - textView.getMeasuredWidth() - 10;
        } else {
            params.leftMargin = point.x - textView.getMeasuredWidth() / 2;
        }
        params.topMargin = -8;
        params.addRule(RelativeLayout.BELOW, R.id.demo_triangle);

        addView(textView, params);


        LayoutParams triParams = new LayoutParams(30, 30);
        triangle.setId(R.id.demo_triangle);
        triParams.leftMargin = point.x - 15;
        triParams.topMargin = point.y;

        addView(triangle, triParams);

        showView();

    }

    public void setText(final CharSequence message) {
        newText = message;
    }

    public void overrideClickListener(OnClickListener clickListener) {
        textView.setOnClickListener(clickListener);
    }

    private static void insertPlacement(Placement placement, Activity activity) {
        ((ViewGroup) activity.getWindow().getDecorView()).addView(placement);

    }

    public void showView() {
        ObjectAnimator animation = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
        animation.setDuration(DELAY);
        animation.start();
    }

    public void clearViews(final View view) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        animation.setDuration(DELAY);
        animation.start();

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeAllViews();
                setTargetView(view);
            }
        });
    }

    public void end() {
        ObjectAnimator animation = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        animation.setDuration(DELAY);
        animation.start();

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeAllViews();
            }
        });
    }

    public static class Builder {

        final Placement placement;
        private final Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;
            this.placement = new Placement(activity);
        }

        public Placement build() {
            insertPlacement(placement, activity);
            return placement;
        }

        public Builder setTarget(View view) {
            placement.setTargetView(view);
            return this;
        }

        public Builder setText(CharSequence message) {
            placement.setText(message);
            return this;
        }

        public Builder setOnClickListener(OnClickListener clickListener) {
            placement.overrideClickListener(clickListener);
            return this;
        }
    }

}
