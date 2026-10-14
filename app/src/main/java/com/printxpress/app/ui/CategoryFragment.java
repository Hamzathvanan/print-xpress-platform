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

import com.printxpress.app.R;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.ProductCategory;
import com.printxpress.app.data.model.ProductEntity;
import com.printxpress.app.databinding.FragmentListBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {
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
        String categoryName = requireArguments().getString("category");
        ProductCategory category = ProductCategory.valueOf(categoryName);
        PrintXpressRepository repo = UiHelpers.repo(this);

        binding.toolbar.setTitle(category.getLabel());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        InfoCardAdapter adapter = new InfoCardAdapter(item -> {
            Bundle args = new Bundle();
            args.putString("productId", (String) item.payload);
            Navigation.findNavController(view).navigate(R.id.action_category_to_product, args);
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);

        repo.observeProductsByCategory(category.name()).observe(getViewLifecycleOwner(), products -> {
            List<InfoCardAdapter.Item> items = new ArrayList<>();
            if (products != null) {
                for (ProductEntity product : products) {
                    items.add(new InfoCardAdapter.Item(
                            product.name,
                            product.description,
                            repo.priceLabel(product.basePriceLkr),
                            product.id
                    ));
                }
            }
            adapter.submit(items);
            binding.empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            binding.empty.setText("No products in this category.");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
