package com.printxpress.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.printxpress.app.R;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.AddressEntity;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentAddressesBinding;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;

import java.util.List;

public class AddressesFragment extends Fragment {
    private FragmentAddressesBinding binding;
    private UserEntity user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressesBinding.inflate(inflater, container, false);
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
                repo.observeAddresses(user.id).observe(getViewLifecycleOwner(), this::renderList);
            });
        });

        binding.save.setOnClickListener(v -> {
            if (user == null) {
                return;
            }
            UiHelpers.showError(binding.error, null);
            AddressEntity address = new AddressEntity(
                    user.id,
                    text(binding.label),
                    text(binding.line1),
                    text(binding.city),
                    text(binding.district),
                    text(binding.postalCode),
                    binding.defaultChip.isChecked()
            );
            AppExecutors.get().diskIo().execute(() -> {
                Result<Long> result = repo.saveAddress(address);
                AppExecutors.get().mainThread().execute(() -> {
                    if (result.isSuccess()) {
                        binding.line1.setText("");
                        binding.city.setText("");
                        binding.district.setText("");
                        binding.postalCode.setText("");
                    } else {
                        UiHelpers.showError(binding.error, result.getErrorMessage());
                    }
                });
            });
        });
    }

    private void renderList(List<AddressEntity> addresses) {
        if (binding == null) {
            return;
        }
        binding.addressList.removeAllViews();
        boolean empty = addresses == null || addresses.isEmpty();
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            return;
        }
        PrintXpressRepository repo = UiHelpers.repo(this);
        for (AddressEntity address : addresses) {
            String title = address.label + (address.isDefault ? " (default)" : "");
            String body = address.line1 + ", " + address.city + ", "
                    + address.district + " " + address.postalCode;
            UiHelpers.addInfoCard(binding.addressList, title, body, null, v ->
                    AppExecutors.get().diskIo().execute(() -> repo.deleteAddress(address.id)));
            TextView tip = new TextView(requireContext());
            tip.setText(R.string.tap_to_delete);
            tip.setTextColor(requireContext().getColor(R.color.px_muted));
            tip.setTextSize(12f);
            tip.setPadding(0, 0, 0, 16);
            binding.addressList.addView(tip);
        }
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
