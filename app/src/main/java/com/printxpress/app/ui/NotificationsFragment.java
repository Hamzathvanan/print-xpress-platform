package com.printxpress.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.NotificationEntity;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentListBinding;
import com.printxpress.app.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {
    private FragmentListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.toolbar.setTitle("Notifications & offers");
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        InfoCardAdapter adapter = new InfoCardAdapter(null);
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);

        AppExecutors.get().diskIo().execute(() -> {
            UserEntity user = repo.currentUser();
            if (user != null) {
                repo.markNotificationsRead(user.id);
            }
            AppExecutors.get().mainThread().execute(() -> {
                if (user == null || binding == null) {
                    return;
                }
                repo.observeNotifications(user.id).observe(getViewLifecycleOwner(), notifications -> {
                    List<InfoCardAdapter.Item> items = new ArrayList<>();
                    if (notifications != null) {
                        for (NotificationEntity item : notifications) {
                            items.add(new InfoCardAdapter.Item(
                                    item.title,
                                    item.body,
                                    item.type.toUpperCase(Locale.US),
                                    null
                            ));
                        }
                    }
                    adapter.submit(items);
                    binding.empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.empty.setText("No notifications yet.");
                });
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
