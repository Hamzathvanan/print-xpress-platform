package com.printxpress.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.SavedDesignEntity;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentDesignsBinding;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;

import java.util.List;

public class DesignsFragment extends Fragment {
    private FragmentDesignsBinding binding;
    private UserEntity user;
    private String artworkUri;
    private ActivityResultLauncher<String> picker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && binding != null) {
                artworkUri = uri.toString();
                binding.attach.setText("Change artwork");
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDesignsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.attach.setOnClickListener(v -> picker.launch("image/*"));

        AppExecutors.get().diskIo().execute(() -> {
            UserEntity current = repo.currentUser();
            AppExecutors.get().mainThread().execute(() -> {
                if (binding == null || current == null) {
                    return;
                }
                user = current;
                repo.observeDesigns(user.id).observe(getViewLifecycleOwner(), this::renderList);
            });
        });

        binding.save.setOnClickListener(v -> {
            if (user == null) {
                return;
            }
            UiHelpers.showError(binding.error, null);
            SavedDesignEntity design = new SavedDesignEntity(
                    user.id,
                    text(binding.title),
                    text(binding.notes),
                    artworkUri
            );
            AppExecutors.get().diskIo().execute(() -> {
                Result<Long> result = repo.saveDesign(design);
                AppExecutors.get().mainThread().execute(() -> {
                    if (result.isSuccess()) {
                        binding.title.setText("");
                        binding.notes.setText("");
                        artworkUri = null;
                        binding.attach.setText("Attach artwork");
                    } else {
                        UiHelpers.showError(binding.error, result.getErrorMessage());
                    }
                });
            });
        });
    }

    private void renderList(List<SavedDesignEntity> designs) {
        if (binding == null) {
            return;
        }
        binding.designList.removeAllViews();
        boolean empty = designs == null || designs.isEmpty();
        binding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            return;
        }
        PrintXpressRepository repo = UiHelpers.repo(this);
        for (SavedDesignEntity design : designs) {
            String body = design.notes == null || design.notes.isEmpty()
                    ? (design.artworkUri == null ? "No file attached" : design.artworkUri)
                    : design.notes;
            UiHelpers.addInfoCard(binding.designList, design.title, body, null, v ->
                    AppExecutors.get().diskIo().execute(() -> repo.deleteDesign(design.id)));
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
