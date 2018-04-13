package sendesign.btmirror;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private static final String TAG = "CustomAdapter";
        private String[] mDataSet;


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
        }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_itemview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.getTextView().setText(mDataSet[position]);
        holder.getTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectedThread.ssid = ConnectedThread.wifiList[position];
                String toSet = ConnectedThread.ssid;
                WifiSetup.wifiSelected.setText(toSet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;

    }
}