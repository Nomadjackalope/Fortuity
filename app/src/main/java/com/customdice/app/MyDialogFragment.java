package com.customdice.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Benjamin on 4/15/2015.
 *
 * Dialog that pops up for creating custom dice
 */
public class MyDialogFragment extends DialogFragment {

    InterCom com;

    AlertDialog alertDialog;

    private boolean modOn;

    private EditText sides;
    private EditText dice;
    private EditText mod;
    private Switch modOrDie;

    boolean sidesEmpty;
    boolean diceEmpty;
    boolean modEmpty;

    public static MyDialogFragment newInstance(Activity activity) {
        MyDialogFragment frag = new MyDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.mod_or_die)
                .setPositiveButton(R.string.add,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(modOn) {
                                    if(!modEmpty) {
                                        int[] x = new int[2];
                                        x[0] = 0;
                                        x[1] = Integer.parseInt(mod.getText().toString());

                                        com.closeDialog(false, x);
                                    } else {
                                        int[] x = new int[2];
                                        com.closeDialog(true, x);
                                    }
                                } else {
                                    if (!sidesEmpty && !diceEmpty) {
                                        int[] x = new int[2];
                                        x[0] = Integer.parseInt(sides.getText().toString());
                                        x[1] = Integer.parseInt(dice.getText().toString());

                                        com.closeDialog(false, x);
                                    } else {
                                        int[] x = new int[2];
                                        com.closeDialog(true, x);
                                    }
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_die, null);

        com = (InterCom) getActivity();

        sides = (EditText) v.findViewById(R.id.sides);
        dice = (EditText) v.findViewById(R.id.dice);
        mod = (EditText) v.findViewById(R.id.mod);

        modOrDie = (Switch) v.findViewById(R.id.switch1);

        modOrDie.setTextOn(getText(R.string.mod));
        modOrDie.setTextOff(getText(R.string.die));

        modOrDie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    modOn = true;
                    sides.setVisibility(View.GONE);
                    dice.setVisibility(View.GONE);
                    mod.setVisibility(View.VISIBLE);
                    setAdd();
                } else {
                    modOn = false;
                    sides.setVisibility(View.VISIBLE);
                    dice.setVisibility(View.VISIBLE);
                    mod.setVisibility(View.GONE);
                    setAdd();
                }
            }
        });

        builder.setView(v);

        alertDialog = builder.create();

        sidesEmpty = true;
        diceEmpty = true;
        modEmpty = true;
        setTextWatch();

        return alertDialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        modOn = false;
        setAdd();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public void closeDialog() {
        dismiss();
    }

    public void setTextWatch() {

        sides.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Integer value = parseInt(s);

                if(value != null) {
                    if (s.length() > 8) {
                        setDiceEmpty(true);
                    } else if (s.length() <= 0) {
                        setSidesEmpty(true);
                    } else if (s.toString().equals("-")) {
                        setSidesEmpty(true);
                    } else if (value > 100
                            || value < 2) {
                        setSidesEmpty(true);
                    } else {
                        setSidesEmpty(false);
                    }
                } else {
                    setSidesEmpty(true);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Integer value = parseInt(s);

                if(value != null) {
                    if (s.length() > 8) {
                        setDiceEmpty(true);
                    } else if (s.length() <= 0) {
                        setDiceEmpty(true);
                    } else if (s.toString().equals("-")) {
                        setDiceEmpty(true);
                    } else if (value > 100
                            || value == 0) {
                        setDiceEmpty(true);
                    } else {
                        setDiceEmpty(false);
                    }
                } else {
                    setDiceEmpty(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mod.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Integer value = parseInt(s);

                if(value != null) {
                    if (s.length() > 8) {
                        setDiceEmpty(true);
                    } else if (s.length() <= 0) {
                        setModEmpty(true);
                    } else if (s.toString().equals("-")) {
                        setModEmpty(true);
                    } else if (value > 100
                            || value == 0
                            || value < -100) {
                        setModEmpty(true);
                    } else {
                        setModEmpty(false);
                    }
                } else {
                    setModEmpty(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setSidesEmpty(boolean state) {
        sidesEmpty = state;
        setAdd();
    }

    public void setDiceEmpty(boolean state) {
        diceEmpty = state;
        setAdd();
    }

    public void setModEmpty(boolean state) {
        modEmpty = state;
        setAdd();
    }

    public void setAdd() {
        if (modOn) {
            if (!modEmpty) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            } else {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        } else {
            if (!sidesEmpty && !diceEmpty) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            } else {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }

    public Integer parseInt(CharSequence s) {
        Integer value;

        try {
            value = Integer.parseInt(s.toString());
        } catch (NumberFormatException n) {
            value = null;
        }

        return value;
    }
}

