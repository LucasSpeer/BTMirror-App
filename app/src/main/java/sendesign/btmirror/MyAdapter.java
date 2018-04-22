package sendesign.btmirror;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private static final String TAG = "CustomAdapter";
        private String[] mDataSet;
        private TextView clickedBackground;
        private TextView unclickedBackground;
        private TextView wifiOptions[];
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
                        ConnectedThread.ssid = ConnectedThread.wifiList[getAdapterPosition()];
                    }
                });
                textView = (TextView) v.findViewById(R.id.simple_text);
            }
            public TextView getTextView() {
                return textView;
            }
        }

        public MyAdapter(String[] dataSet){
            mDataSet = dataSet;
            wifiOptions = new TextView[dataSet.length];
        }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_itemview, parent, false);
        clickedBackground = view.findViewById(R.id.clciked_text);
        unclickedBackground = view.findViewById(R.id.simple_text);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.getTextView().setText(mDataSet[position]);
            wifiOptions[position] = holder.getTextView();
            holder.getTextView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectedThread.ssid = ConnectedThread.wifiList[position];
                    String toSet = ConnectedThread.ssid;
                    WifiSetup.wifiSelected.setText(toSet);
                    for(int i = 0; i < wifiOptions.length; i++){
                        wifiOptions[i].setBackground(unclickedBackground.getBackground());
                    }
                    v.setBackground(clickedBackground.getBackground());
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
