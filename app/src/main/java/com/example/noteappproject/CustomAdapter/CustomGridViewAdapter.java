package com.example.noteappproject.CustomAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteappproject.Models.NoteItem;
import com.example.noteappproject.PostLoginActivity.NoteActivity;
import com.example.noteappproject.R;
import com.example.noteappproject.databinding.ActivityGridViewItemNoteItemBinding;

import java.util.List;

public class CustomGridViewAdapter extends RecyclerView.Adapter<CustomGridViewAdapter.MyGridViewHolder> {

    private final Context context;
    private List<NoteItem> dataSource;

    private IItemClick itemClick;


    public interface IItemClick {

        void onClick(NoteItem noteItem);

        void onLongClick(NoteItem noteItem, CardView cardView);
    }

    public CustomGridViewAdapter(Context context, List<NoteItem> dataSource, IItemClick itemClick) {
        this.context = context;
        this.dataSource = dataSource;
        this.itemClick = itemClick;
    }

    @NonNull
    @Override
    public MyGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityGridViewItemNoteItemBinding viewRoot = ActivityGridViewItemNoteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyGridViewHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGridViewHolder holder, int position) {
        holder.bindData(this.dataSource.get(position), position);
    }

    @Override
    public int getItemCount() {
        return this.dataSource.size();
    }

    protected class MyGridViewHolder extends RecyclerView.ViewHolder {

        private final ActivityGridViewItemNoteItemBinding binding;

        public MyGridViewHolder(@NonNull ActivityGridViewItemNoteItemBinding itemView) {
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

            if (noteItem.getText_content().trim().isEmpty()) {
                this.binding.textContent.setVisibility(View.GONE);
            } else {
                this.binding.textContent.setText(noteItem.getText_content());
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

            if (noteItem.getImagePath() != null) {
                this.binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(noteItem.getImagePath()));
                this.binding.imageNote.setVisibility(View.VISIBLE);
            } else {
                this.binding.imageNote.setVisibility(View.GONE);
            }

            this.binding.label.setText(noteItem.getLabel());
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
    public void filterListInGridView(List<NoteItem> noteItemArrayList) {
        dataSource = noteItemArrayList;
        notifyDataSetChanged();
    }
}

