package com.example.spotify_wrapped;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WrapAdapter extends RecyclerView.Adapter<WrapAdapter.WrapViewHolder> {
    private Context context;
    private List<WrapData> wrapList;

    private static final String TAG = "WrapAdapter";

    public WrapAdapter(Context context, List<WrapData> wrapList) {
        this.context = context;
        this.wrapList = wrapList;
    }

    @NonNull
    @Override
    public WrapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wrap, parent, false);
        return new WrapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WrapViewHolder holder, int position) {
        WrapData wrapData = wrapList.get(position);
        holder.bind(wrapData);
    }

    @Override
    public int getItemCount() {
        return wrapList.size();
    }

    public class WrapViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;

        public WrapViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateButton);
        }

        public void bind(WrapData wrapData) {
            dateTextView.setText("Date: " + wrapData.getDate());
            dateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to IntroActivity with the corresponding wrapped's data
                    Intent intent = new Intent(context, SongActivity.class);
                    intent.putExtra(WrapData.WRAP_DATA_KEY, wrapData); // Pass the WrapData object to IntroActivity
                    context.startActivity(intent);
                }
            });
        }
    }
}
