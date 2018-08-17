package com.customdice.app.tutorial;

import android.graphics.Point;
import android.view.View;

/**
 * Created by Benjamin on 6/9/2015.
 *
 * Gets the middle position of the view passed in
 */
public class Targetter {

    private final View view;

    public Targetter(View view) {
        this.view = view;
    }

    public Point getBot() {

        int[] position = new int[2];

        if(view !=  null) {

            view.getLocationInWindow(position);
            int x = position[0] + view.getWidth() / 2;
            int y = position[1] + view.getHeight();
            return new Point(x, y);

        } else {

            System.out.println("T| null");

        }
        return null;
    }
}
