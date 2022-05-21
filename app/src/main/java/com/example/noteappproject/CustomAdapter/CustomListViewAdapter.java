package com.example.noteappproject.CustomAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityListViewItemNoteItemBinding;

import java.util.List;

public class CustomListViewAdapter extends RecyclerView.Adapter<CustomListViewAdapter.MyListViewHolder> {

    private final Context context;
    private List<NoteItem> dataSource;

    private IItemClick itemClick;

    public interface IItemClick {

        void onClick(NoteItem noteItem);

        void onLongClick(NoteItem noteItem, CardView cardView);
    }

    public CustomListViewAdapter(Context context, List<NoteItem> dataSource, IItemClick itemClick) {
        this.context = context;
        this.dataSource = dataSource;
        this.itemClick = itemClick;
    }

    @NonNull
    @Override
    public MyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityListViewItemNoteItemBinding viewRoot = ActivityListViewItemNoteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyListViewHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListViewHolder holder, int position) {
        holder.bindData(this.dataSource.get(position), position);
    }

    @Override
    public int getItemCount() {
        return this.dataSource.size();
    }

    protected class MyListViewHolder extends RecyclerView.ViewHolder {

        private final ActivityListViewItemNoteItemBinding binding;

        public MyListViewHolder(@NonNull ActivityListViewItemNoteItemBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }

        private void bindData(NoteItem noteItem, int position) {

            if (noteItem.isPinned()) {
                this.binding.imageViewPin.setImageResource(R.drawable.ic_pin);
            } else {
                this.binding.imageViewPin.setImageResource(0);
            }

            if (noteItem.getSubtitle().trim().isEmpty()) {
                this.binding.subtitle.setVisibility(View.GONE);
            } else {
                this.binding.subtitle.setText(noteItem.getSubtitle());
            }

            if (noteItem.getColor() != null) {
                this.binding.mainCardView.setCardBackgroundColor(Color.parseColor(noteItem.getColor()));
            } else {
                this.binding.mainCardView.setCardBackgroundColor(Color.parseColor("#333333"));
            }

            if (noteItem.getPasswordNote().isEmpty()) {
                this.binding.imageViewPassword.setImageResource(0);
            } else {
                this.binding.imageViewPassword.setImageResource(R.drawable.ic_lock);
            }

            this.binding.label.setText(noteItem.getLabel());
            this.binding.textContent.setText(noteItem.getText_content());
            this.binding.timeCreate.setText(noteItem.getDate());


            this.binding.mainCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClick.onClick(noteItem);
                }
            });

            this.binding.mainCardView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    itemClick.onLongClick(noteItem, binding.mainCardView);
                    return true;
                }
            });
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterListInListView(List<NoteItem> noteItemArrayList) {
        dataSource = noteItemArrayList;
        notifyDataSetChanged();
    }
}

