package com.printxpress.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.printxpress.app.R;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentProfileBinding;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private UserEntity user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        AppExecutors.get().diskIo().execute(() -> {
            UserEntity current = repo.currentUser();
            AppExecutors.get().mainThread().execute(() -> {
                if (binding == null || current == null) {
                    return;
                }
                user = current;
                binding.fullName.setText(user.fullName);
                binding.email.setText(user.email == null ? "" : user.email);
                binding.phone.setText(user.phone == null ? "" : user.phone);

                binding.links.removeAllViews();
                UiHelpers.addInfoCard(binding.links, "Delivery addresses",
                        "Manage pickup-alternative home addresses", null,
                        v -> Navigation.findNavController(view)
                                .navigate(R.id.action_profile_to_addresses));
                UiHelpers.addInfoCard(binding.links, "Saved designs",
                        "Keep artwork references for reorders", null,
                        v -> Navigation.findNavController(view)
                                .navigate(R.id.action_profile_to_designs));
                UiHelpers.addInfoCard(binding.links, "Order history",
                        "View and track every PrintXpress job", null,
                        v -> Navigation.findNavController(view)
                                .navigate(R.id.action_profile_to_orders));
            });
        });

        binding.saveProfile.setOnClickListener(v -> {
            if (user == null) {
                return;
            }
            UiHelpers.showError(binding.error, null);
            binding.saved.setVisibility(View.GONE);
            user.fullName = text(binding.fullName);
            String email = text(binding.email);
            String phone = text(binding.phone);
            user.email = email.isEmpty() ? null : email;
            user.phone = phone.isEmpty() ? null : phone;
            AppExecutors.get().diskIo().execute(() -> {
                Result<Void> result = repo.updateProfile(user);
                AppExecutors.get().mainThread().execute(() -> {
                    if (result.isSuccess()) {
                        binding.saved.setVisibility(View.VISIBLE);
                        repo.refreshSessionUser();
                    } else {
                        UiHelpers.showError(binding.error, result.getErrorMessage());
                    }
                });
            });
        });

        binding.signOut.setOnClickListener(v -> AppExecutors.get().diskIo().execute(() -> {
            repo.logout();
            AppExecutors.get().mainThread().execute(() ->
                    Navigation.findNavController(view).navigate(R.id.action_profile_to_login));
        }));
    }

    private static String text(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
