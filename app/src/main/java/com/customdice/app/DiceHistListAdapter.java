package com.customdice.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Benjamin on 4/15/2015.
 *
 * This adapter provides the layouts for favorites and standard rolls in the dice history list
 */
public class DiceHistListAdapter extends ArrayAdapter<Integer> {

    private Activity host;

    private final static int FAVORITE = 1;
    private final static int REGULAR_ROLL = 0;

    static int idCount = 0;
    Integer id;

    private InterCom com;

    Context context;

    public DiceHistListAdapter(Activity activity, int resource) {
        super(activity, resource);
        this.host = activity;
        com = (InterCom) activity;

        id = idCount;
        idCount++;
    }

    @Override
    public int getCount() {
        return com.getDiceHistories().size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    // Returns which view, fav or standard, the list item should be
    @Override
    public int getItemViewType(int pos) {
        if(com.getDiceHistories().get(pos).getFave()) { //use instanceof FavFragment or ... later
            return FAVORITE;
        } else {
            return REGULAR_ROLL;
        }
    }

    @Override
    public long getItemId(int position) {
        return (long) com.getDiceHistories().get(position).getID();
    }

    // Handles the clicks on diceHistories items
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolderItem viewHolder;
        FavViewHolderItem favViewHolder;

        context = parent.getContext();

        LayoutInflater inflater = host.getLayoutInflater();

        switch (getItemViewType(position)) {

            case FAVORITE:

                //null our other view holders
                //viewHolder = null;

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.dice_list_fav_item, parent, false);
                    favViewHolder = new FavViewHolderItem(convertView);
                    convertView.setTag(favViewHolder);
                } else {
                    favViewHolder = (FavViewHolderItem) convertView.getTag();
                }
                favViewHolder.close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(position == 0) {
                            if(!com.getNewRoll()) {
                                com.setNewRoll(true);
                                com.getAnimateRemoval(position);

                            } else {
                                com.getAnimateRemoval(position);
                            }
                        } else {
                            com.getAnimateRemoval(position);
                        }
                        com.notifyDiceHist();
                    }
                });
                favViewHolder.favViewState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        com.changeView(position);
                        // Used to test roll statistics
                        //System.out.println("DHLA| " + com.getDiceHistories().get(position).toString());
                        com.notifyDiceHist();
                    }
                });
                favViewHolder.favViewState.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        com.rollFavorite(position);
                        return true;
                    }
                });
                favViewHolder.dropDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu(v, position);
                    }
                });
                favViewHolder.name.setText(com.getDiceHistories().get(position).getName());

                // If the view state is not individual
                if(!com.getDiceHistories().get(position).getViewState()) {
                    favViewHolder.historyText.setText(com.getDiceHistories().get(position).toString());
                } else {
                    favViewHolder.historyText.setText(com.getDiceHistories().get(position).toSpanString());
                }
                favViewHolder.color.setBackgroundColor(com.getDiceHistories().get(position).getColor());
                break;

            case REGULAR_ROLL:

                //null our other view holders
                //favViewHolder = null;

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.dice_list_item, parent, false);
                    viewHolder = new ViewHolderItem(convertView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolderItem) convertView.getTag();
                }
                viewHolder.close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(position == 0) {
                            if(!com.getNewRoll()) {
                                com.setNewRoll(true);
                                com.getAnimateRemoval(position);

                            } else {
                                com.getAnimateRemoval(position);
                            }
                        } else {
                            com.getAnimateRemoval(position);
                        }
                        com.notifyDiceHist();
                    }
                });
                viewHolder.viewState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        com.changeView(position);

                        com.notifyDiceHist();
                    }
                });
                viewHolder.historyText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        com.changeView(position);

                        com.notifyDiceHist();
                    }
                });
                viewHolder.dropDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu(v, position);
                    }
                });

                // If the view state is not individual
                if(!com.getDiceHistories().get(position).getViewState()) {
                    viewHolder.historyText.setText(com.getDiceHistories().get(position).toString());

                } else {
                    viewHolder.historyText.setText(com.getDiceHistories().get(position).toSpanString());
                }
                break;
        }


        return convertView;

    }

    static class ViewHolderItem {
        TextView historyText;
        ImageView close;
        ImageView dropDown;
        RelativeLayout viewState;

        ViewHolderItem(View v) {
            this.historyText = (TextView) v.findViewById(R.id.historytext);
            this.close = (ImageView) v.findViewById(R.id.close);
            this.dropDown = (ImageView) v.findViewById(R.id.drop_down);
            this.viewState = (RelativeLayout) v.findViewById(R.id.view_state);
        }
    }

    static class FavViewHolderItem {
        TextView name;
        TextView historyText;
        ImageView close;
        TextView color;
        ImageView dropDown;
        RelativeLayout favViewState;

        FavViewHolderItem(View v){
            this.name = (TextView) v.findViewById(R.id.favname);
            this.historyText = (TextView) v.findViewById(R.id.favhistorytext);
            this.close = (ImageView) v.findViewById(R.id.close);
            this.color = (TextView) v.findViewById(R.id.favcolor);
            this.dropDown = (ImageView) v.findViewById(R.id.drop_down);
            this.favViewState = (RelativeLayout) v.findViewById(R.id.fav_view_state);
        }
    }

    public void menu(View v, final int position) {
        try {
            // Android 4.2.2, especially on Samsung devices, crash here. See bugreport:
            // https://code.google.com/p/android/issues/detail?id=78377
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_reroll_ones:
                            com.rerollOnes(position);
                            com.notifyDiceHist();
                            return true;
                        case R.id.menu_create_fav:
                            com.uploadNewFav(position);
                            return true;
                        case R.id.menu_change_view:
                            com.changeView(position);
                            com.notifyDiceHist();
                            return true;
                        case R.id.menu_remove_lowest:
                            com.removeLowest(position);
                            com.notifyDiceHist();
                            return true;
                        case R.id.menu_reroll_all_lowest:
                            com.rerollAllLowest(position);
                            com.notifyDiceHist();
                            return true;
                        case R.id.menu_add_highlighting:
                            com.addHighlight(position, false); // Not forFav so set as false
                            com.notifyDiceHist();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.inflate(R.menu.menu_main);
            popupMenu.show();

        } catch (NoClassDefFoundError e) {
            alternativeMenu(position);
        }

    }

    public void alternativeMenu(final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        // Set items
        dialog.setItems(R.array.menu_main, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        com.uploadNewFav(position);
                        break;
                    case 1:
                        com.changeView(position);
                        com.notifyDiceHist();
                        break;
                    case 2:
                        com.rerollOnes(position);
                        com.notifyDiceHist();
                        break;
                    case 3:
                        com.removeLowest(position);
                        com.notifyDiceHist();
                        break;
                    case 4:
                        com.rerollAllLowest(position);
                        com.notifyDiceHist();
                        break;
                    case 5:
                        com.addHighlight(position, false); // Not forFav so set as false
                        com.notifyDiceHist();
                        break;
                    default:
                        break;
                }
            }
        });

        dialog.create().show();
    }

}

