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

import com.printxpress.app.databinding.FragmentStaticListBinding;

import java.util.Arrays;
import java.util.List;

public class GuidelinesFragment extends Fragment {
    private FragmentStaticListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStaticListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.toolbar.setTitle("Print guidelines");
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        List<InfoCardAdapter.Item> items = Arrays.asList(
                new InfoCardAdapter.Item("File requirements",
                        "Upload PDF, PNG, or JPG. Prefer PDF for multi-page jobs. Keep fonts outlined."),
                new InfoCardAdapter.Item("Colour formats",
                        "Use CMYK for professional print accuracy. RGB designs may shift when converted."),
                new InfoCardAdapter.Item("Bleed margins",
                        "Include 3mm bleed on all sides and keep critical text 5mm inside the trim."),
                new InfoCardAdapter.Item("Resolution",
                        "Raster artwork should be 300 DPI at final print size."),
                new InfoCardAdapter.Item("Safe zone",
                        "Avoid placing logos or phone numbers near edges or fold lines.")
        );
        InfoCardAdapter adapter = new InfoCardAdapter(null);
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);
        adapter.submit(items);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
