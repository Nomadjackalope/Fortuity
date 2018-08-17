package com.customdice.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;


/**
 * Created by Benjamin on 6/2/2015.
 *
 */
public class UndoView {

    private View undoView;
    private TextView messageView;

    private CharSequence undoMessage;

    private InterCom com;

    private Handler handler = new Handler();

    public UndoView(View undoBarView, InterCom interCom) {
        undoView = undoBarView;

        com = interCom;

        messageView = (TextView) undoView.findViewById(R.id.undobar_message);
        undoView.findViewById(R.id.undobar_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideUndoBar();
                        com.onUndo();
                    }
                });

        hideUndoBar();
    }

    public void showUndoBar(CharSequence message) {
        messageView.setText(message);
        undoView.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hide);
        handler.postDelayed(hide, undoView.getResources().getInteger(R.integer.undobar_hide_delay));
    }

    public void hideUndoBar() {
        undoView.setVisibility(View.INVISIBLE);
    }

    private Runnable hide = new Runnable() {
        @Override
        public void run() {
            hideUndoBar();
        }
    };

    public void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("undo_message", undoMessage);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        undoMessage = savedInstanceState.getCharSequence("undo_message",
                undoView.getResources().getText(R.string.undobar_sample_message));

    }

}
