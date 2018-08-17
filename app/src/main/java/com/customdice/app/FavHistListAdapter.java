package com.customdice.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Benjamin on 4/15/2015.
 *
 * This adapter provides the layouts for favorites in the fav list
 */
public class FavHistListAdapter extends ArrayAdapter<Integer> {

    private Activity host;

    Context context;

    static int idCount = 0;
    Integer id;

    private InterCom com;

    CheckedTextView yellow;
    CheckedTextView pink;
    CheckedTextView amber;
    CheckedTextView teal;
    CheckedTextView indigo;
    CheckedTextView green;
    CheckedTextView grey;
    CheckedTextView white;

    public FavHistListAdapter(Activity activity, int resource) {
        super(activity, resource);
        this.host = activity;
        com = (InterCom) activity;
        id = idCount;
        idCount++;
    }

    @Override
    public int getCount() {
        return com.getFavorites().size();
    }

    @Override
    public long getItemId(int position) {
        return (long) com.getFavorites().get(position).getID();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final FavViewHolderItem favViewHolder;

        context = parent.getContext();

        LayoutInflater inflater = host.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.fav_list_item, parent, false);
            favViewHolder = new FavViewHolderItem(convertView);
            convertView.setTag(favViewHolder);
        } else {
            favViewHolder = (FavViewHolderItem) convertView.getTag();
        }
        favViewHolder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.getAnimateRemovalFav(position);
                com.notifyFavHist();
            }
        });
        favViewHolder.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adds numbers into FavRoll Fragment
                com.uploadFav(position);
            }
        });
        favViewHolder.favColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDialog(position);
                v.invalidate();
            }
        });

        favViewHolder.upload.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                com.rollFavoriteFav(position);
                return true;
            }
        });

        favViewHolder.dropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu(v, position);
            }
        });

        com.getFavorites().get(position).setForFavList(true);
        favViewHolder.favHistoryText.setText(com.getFavorites().get(position).toFavString());
        favViewHolder.name.setText(com.getFavorites().get(position).getName());
        favViewHolder.favColor.setBackgroundColor(com.getFavorites().get(position).getColor());
        com.getFavorites().get(position).setForFavList(false);

        ArrayList<Integer> mods = com.getFavorites().get(position).getModifications();

        // Reset all visibiltiy
        favViewHolder.reroll_ones.setVisibility(View.GONE);
        favViewHolder.remove_lowest.setVisibility(View.GONE);
        favViewHolder.reroll_lowest.setVisibility(View.GONE);

        for(Integer x : mods) {
            switch(x) {
                case 0:
                    favViewHolder.reroll_ones.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    favViewHolder.remove_lowest.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    favViewHolder.reroll_lowest.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        return convertView;
    }

    static class FavViewHolderItem {
        TextView name;
        TextView favHistoryText;
        RelativeLayout upload;
        ImageView close;
        TextView favColor;
        ImageView dropDown;

        ImageView reroll_ones;
        ImageView remove_lowest;
        ImageView reroll_lowest;

        FavViewHolderItem(View v){
            this.name = (TextView) v.findViewById(R.id.favname);
            this.favHistoryText = (TextView) v.findViewById(R.id.favhistorytext);
            this.close = (ImageView) v.findViewById(R.id.close);
            this.favColor = (TextView) v.findViewById(R.id.favcolor);
            this.dropDown = (ImageView) v.findViewById(R.id.drop_down);
            this.upload = (RelativeLayout) v.findViewById(R.id.favlayout);
            this.reroll_ones = (ImageView) v.findViewById(R.id.reroll_ones_img);
            this.remove_lowest = (ImageView) v.findViewById(R.id.remove_lowest_img);
            this.reroll_lowest = (ImageView) v.findViewById(R.id.reroll_lowest_img);
        }
    }

    public void nameDialog(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(host);

        builder.setTitle("Change the name");

        LayoutInflater inflater = host.getLayoutInflater();
        final View v = inflater.inflate(R.layout.fav_name, null, false);
        builder.setView(v);

        final EditText name = (EditText) v.findViewById(R.id.name);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Gets the user's input text and assigns it to the prevRoll name
                com.getFavorites().get(position).setName(name.getText().toString());
                // User clicked Add
                closeKeyboard(v);
                dialog.dismiss();
                notifyDataSetChanged();
                // Figure out how to DiceHistAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Cancel
                closeKeyboard(v);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        name.requestFocus();
    }

    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    // Color Dialog //TODO make a new color selector
    public void newDialog(final int position) {

        final int currentColor = com.getFavorites().get(position).getColor();

        AlertDialog.Builder builder = new AlertDialog.Builder(host);

        builder.setTitle("Change the color");

        LayoutInflater inflater = host.getLayoutInflater();
        View v = inflater.inflate(R.layout.fav_colors, null, false);
        builder.setView(v);

        yellow = (CheckedTextView) v.findViewById(R.id.yellow);
        pink = (CheckedTextView) v.findViewById(R.id.pink);
        amber = (CheckedTextView) v.findViewById(R.id.amber);
        teal = (CheckedTextView) v.findViewById(R.id.teal);
        indigo = (CheckedTextView) v.findViewById(R.id.blue);
        green = (CheckedTextView) v.findViewById(R.id.green);
        grey = (CheckedTextView) v.findViewById(R.id.grey);
        white = (CheckedTextView) v.findViewById(R.id.white);

        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(yellow);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.yellow));
            }
        });
        pink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(pink);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.pink));
            }
        });
        amber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(amber);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.amber));
            }
        });
        teal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(teal);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.teal));
            }
        });
        indigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(indigo);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.indigo));
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(green);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.green));
            }
        });
        grey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(grey);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.grey));
            }
        });
        white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeChecked(white);
                com.getFavorites().get(position).setColor(v.getResources().getColor(R.color.white));
            }
        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Add
                dialog.dismiss();
                notifyDataSetChanged();
                // Figure out how to DiceHistAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Cancel
                com.getFavorites().get(position).setColor(currentColor);
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void changeChecked(CheckedTextView v) {
        CheckedTextView[] checkedTextViews = {yellow, pink, amber, teal, indigo, green, grey, white};
        for(CheckedTextView ctv : checkedTextViews) {
            ctv.setChecked(false);
        }
        v.setChecked(true);
    }

    public void menu(final View v, final int position) {
        try {

            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_change_color:
                            newDialog(position);
                            v.invalidate();
                            return true;
                        case R.id.menu_change_name:
                            nameDialog(position);
                            v.invalidate();
                            return true;
                        case R.id.menu_upload_to_roller:
                            com.uploadFav(position);
                            return true;
                        case R.id.menu_add_highlighting:
                            com.addHighlight(position, true); // forFav so set as true
                            com.notifyFavHist();
                            return true;
                        case R.id.menu_add_rule:
                            com.addRule(position);
                            com.notifyFavHist();
                            com.uploadFav(position);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.inflate(R.menu.menu_fav);
            popupMenu.show();

        } catch (NoClassDefFoundError e) {
            alternativeMenu(v, position);
        }
    }

    public void alternativeMenu(final View v, final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        // Set items
        dialog.setItems(R.array.menu_fav, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        newDialog(position);
                        v.invalidate();
                        break;
                    case 1:
                        nameDialog(position);
                        v.invalidate();
                        break;
                    case 2:
                        com.uploadFav(position);
                        break;
                    case 3:
                        com.addHighlight(position, true); // forFav so set as true
                        com.notifyFavHist();
                        break;
                    case 4:
                        com.addRule(position);
                        com.notifyFavHist();
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.create().show();
    }
}

