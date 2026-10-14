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

public class FaqFragment extends Fragment {
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
        binding.toolbar.setTitle("FAQ & support");
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        List<InfoCardAdapter.Item> items = Arrays.asList(
                new InfoCardAdapter.Item("How long does printing take?",
                        "Most standard jobs are ready within 24–48 hours after artwork approval."),
                new InfoCardAdapter.Item("Can I cancel an order?",
                        "Yes — cancel or reschedule while status is Received or Processing, before printing begins."),
                new InfoCardAdapter.Item("Do you deliver island-wide?",
                        "Pickup is available at partner centres; home delivery covers major Sri Lankan cities."),
                new InfoCardAdapter.Item("What if my file is wrong?",
                        "Contact support from this screen’s guidance or note corrections in order instructions before printing starts."),
                new InfoCardAdapter.Item("Who do I contact?",
                        "Email support@printxpress.lk or call +94 11 200 3000 (coursework demo contacts).")
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
