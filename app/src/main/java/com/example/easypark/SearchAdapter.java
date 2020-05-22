package com.example.easypark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    //Declarations
    Context context;
    ArrayList<String> pName;

    private OnNoteListener mOnNoteListener;

    class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView lot_name;
        OnNoteListener onNoteListener;
        //Set the name of the lot to the adapter
        public SearchViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            lot_name = itemView.findViewById(R.id.textlot);
            this.onNoteListener = onNoteListener;
            itemView.setOnClickListener(this);
        }
        //Get the location of the adapter when it is clicked
        @Override
        public void onClick(View view) {
            onNoteListener.onNoteClick(getAdapterPosition());
        }
    }
    //Display adapter constructor
    public SearchAdapter(Context context, ArrayList<String> pName, OnNoteListener onNoteListener) {
        this.context = context;
        this.pName = pName;
        this.mOnNoteListener = onNoteListener;
    }
    //Display setting function
    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_list_items, parent, false);
        return new SearchAdapter.SearchViewHolder(view, mOnNoteListener);
    }
    //Setting text in the display
    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            holder.lot_name.setText(pName.get(position));
    }
    //Get the counter of the items being displayed
    @Override
    public int getItemCount() {
        return pName.size();
    }
    //On click interface
    public interface OnNoteListener{
        void onNoteClick(int position);
    }
}
