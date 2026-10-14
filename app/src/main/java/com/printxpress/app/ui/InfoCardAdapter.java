package com.printxpress.app.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.printxpress.app.databinding.ItemInfoCardBinding;

import java.util.ArrayList;
import java.util.List;

public class InfoCardAdapter extends RecyclerView.Adapter<InfoCardAdapter.Holder> {
    public static class Item {
        public final String title;
        public final String body;
        @Nullable
        public final String trailing;
        @Nullable
        public final Object payload;

        public Item(String title, String body, @Nullable String trailing, @Nullable Object payload) {
            this.title = title;
            this.body = body;
            this.trailing = trailing;
            this.payload = payload;
        }

        public Item(String title, String body) {
            this(title, body, null, null);
        }
    }

    public interface Listener {
        void onClick(Item item);
    }

    private final List<Item> items = new ArrayList<>();
    @Nullable
    private final Listener listener;

    public InfoCardAdapter(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Item> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemInfoCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Item item = items.get(position);
        holder.binding.title.setText(item.title);
        holder.binding.body.setText(item.body);
        if (item.trailing == null || item.trailing.isEmpty()) {
            holder.binding.trailing.setVisibility(android.view.View.GONE);
        } else {
            holder.binding.trailing.setVisibility(android.view.View.VISIBLE);
            holder.binding.trailing.setText(item.trailing);
        }
        if (listener != null) {
            holder.binding.card.setOnClickListener(v -> listener.onClick(item));
            holder.binding.card.setClickable(true);
        } else {
            holder.binding.card.setOnClickListener(null);
            holder.binding.card.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemInfoCardBinding binding;

        Holder(ItemInfoCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
