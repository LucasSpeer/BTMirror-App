package sendesign.btmirror;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private static final String TAG = "CustomAdapter";
        private String[] mDataSet;              //String array of wifi networks to be shown
        private TextView clickedBackground;     //for highlighting the selection
        private TextView unclickedBackground;   //for unhighlighting
        private TextView wifiOptions[];         //TextView array for looping through to highlight/unhighlight
    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
        /**
         * Provide a reference to the type of views that you are using (custom ViewHolder)
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            public ViewHolder(View v) {
                super(v);
                // Define click listener for the ViewHolder's View.
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ConnectedThread.ssid = ConnectedThread.wifiList[getAdapterPosition()];  //when a option is clicked set the SSID variable in ConnectedThread
                    }
                });
                textView = (TextView) v.findViewById(R.id.simple_text);     //Get the xml element to display the text in
            }
            public TextView getTextView() {
                return textView;
            }
        }

        public MyAdapter(String[] dataSet){
            mDataSet = dataSet;
            wifiOptions = new TextView[dataSet.length]; //initialize the TextView array when the adapter is first created
        }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_itemview, parent, false);
        clickedBackground = view.findViewById(R.id.clciked_text);       //get the TextViews from item_simple_itemview.xml for highlighting/unhighlighting
        unclickedBackground = view.findViewById(R.id.simple_text);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.getTextView().setText(mDataSet[position]);
            wifiOptions[position] = holder.getTextView();       //Add each TextView into the wifiOptions array
            holder.getTextView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectedThread.ssid = ConnectedThread.wifiList[position];
                    String toSet = ConnectedThread.ssid;    //Get the SSID selected
                    WifiSetup.wifiSelected.setText(toSet);  //set the text showing the current choice
                    for(int i = 0; i < wifiOptions.length; i++) {
                        if (wifiOptions[i] != null) {
                            wifiOptions[i].setBackground(unclickedBackground.getBackground());  //Unhighlight all options
                        }
                    }
                    v.setBackground(clickedBackground.getBackground()); //Highlight the selected option
                }
            });

    }

    @Override
    public int getItemCount() {
        if(ConnectedThread.wifiList != null){
            return ConnectedThread.wifiList.length;
        }
        else return 0;

    }
}
