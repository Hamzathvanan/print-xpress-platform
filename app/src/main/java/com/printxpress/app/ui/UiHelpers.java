package com.printxpress.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.printxpress.app.PrintXpressApp;
import com.printxpress.app.R;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.databinding.ItemInfoCardBinding;

public final class UiHelpers {
    private UiHelpers() {
    }

    public static PrintXpressRepository repo(Fragment fragment) {
        return ((PrintXpressApp) fragment.requireActivity().getApplication()).getRepository();
    }

    public static void showError(TextView view, @Nullable String message) {
        if (message == null || message.trim().isEmpty()) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setText(message);
        view.setVisibility(View.VISIBLE);
    }

    public static View inflateInfoCard(Context context, ViewGroup parent,
                                       String title, String body, @Nullable String trailing,
                                       @Nullable View.OnClickListener listener) {
        ItemInfoCardBinding binding = ItemInfoCardBinding.inflate(
                LayoutInflater.from(context), parent, false);
        binding.title.setText(title);
        binding.body.setText(body);
        if (trailing == null || trailing.isEmpty()) {
            binding.trailing.setVisibility(View.GONE);
        } else {
            binding.trailing.setVisibility(View.VISIBLE);
            binding.trailing.setText(trailing);
        }
        if (listener != null) {
            binding.card.setOnClickListener(listener);
            binding.card.setClickable(true);
        } else {
            binding.card.setClickable(false);
        }
        return binding.getRoot();
    }

    public static void addInfoCard(LinearLayout container, String title, String body,
                                   @Nullable String trailing, @Nullable View.OnClickListener listener) {
        container.addView(inflateInfoCard(container.getContext(), container, title, body, trailing, listener));
    }

    public static void addInfoCard(LinearLayout container, String title, String body) {
        addInfoCard(container, title, body, null, null);
    }
}
