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
import com.printxpress.app.databinding.FragmentRegisterBinding;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.goLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
        binding.createAccount.setOnClickListener(v -> {
            binding.createAccount.setEnabled(false);
            UiHelpers.showError(binding.error, null);
            String fullName = text(binding.fullName);
            String email = text(binding.email);
            String phone = text(binding.phone);
            String password = text(binding.password);
            AppExecutors.get().diskIo().execute(() -> {
                Result<UserEntity> result = repo.register(
                        fullName,
                        email.isEmpty() ? null : email,
                        phone.isEmpty() ? null : phone,
                        password
                );
                AppExecutors.get().mainThread().execute(() -> {
                    binding.createAccount.setEnabled(true);
                    if (result.isSuccess()) {
                        Navigation.findNavController(view).navigate(R.id.action_register_to_home);
                    } else {
                        UiHelpers.showError(binding.error, result.getErrorMessage());
                    }
                });
            });
        });
    }

    private static String text(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
