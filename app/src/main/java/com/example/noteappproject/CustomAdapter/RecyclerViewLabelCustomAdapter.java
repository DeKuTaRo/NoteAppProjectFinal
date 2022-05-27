package com.example.noteappproject.CustomAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noteappproject.Models.NoteLabel;
import com.example.noteappproject.databinding.RecyclerViewLabelItemBinding;

import java.util.List;

public class RecyclerViewLabelCustomAdapter extends RecyclerView.Adapter<RecyclerViewLabelCustomAdapter.ViewHolder> {
    private final Context context;
    private final List<NoteLabel> dataSource;


    public RecyclerViewLabelCustomAdapter(Context context, List<NoteLabel> dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @NonNull
    @Override
    public RecyclerViewLabelCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerViewLabelItemBinding viewRoot = RecyclerViewLabelItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecyclerViewLabelCustomAdapter.ViewHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewLabelCustomAdapter.ViewHolder holder, int position) {
        holder.bindData(this.dataSource.get(position), position);
    }

    @Override
    public int getItemCount() {
        return this.dataSource.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final RecyclerViewLabelItemBinding binding;

        public ViewHolder(@NonNull RecyclerViewLabelItemBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;
        }

        private void bindData(NoteLabel noteLabel, int position) {
            this.binding.textViewLabel.setText(noteLabel.getLabelName());
            this.binding.checkBoxLabel.setChecked(noteLabel.isCheck());

            this.binding.checkBoxLabel.setOnClickListener(v -> dataSource.get(position).setCheck(!noteLabel.isCheck()));
        }
    }
}
