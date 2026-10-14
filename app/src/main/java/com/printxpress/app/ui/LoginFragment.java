package com.printxpress.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.printxpress.app.R;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentLoginBinding;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.goRegister.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_register));
        binding.signIn.setOnClickListener(v -> {
            binding.signIn.setEnabled(false);
            UiHelpers.showError(binding.error, null);
            String identifier = text(binding.identifier);
            String password = text(binding.password);
            AppExecutors.get().diskIo().execute(() -> {
                Result<UserEntity> result = repo.login(identifier, password);
                AppExecutors.get().mainThread().execute(() -> {
                    binding.signIn.setEnabled(true);
                    if (result.isSuccess()) {
                        Navigation.findNavController(view).navigate(R.id.action_login_to_home);
                    } else {
                        UiHelpers.showError(binding.error, result.getErrorMessage());
                    }
                });
            });
        });
    }

    private static String text(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
