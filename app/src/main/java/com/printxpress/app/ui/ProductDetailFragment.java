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
import com.printxpress.app.data.model.ProductCategory;
import com.printxpress.app.data.model.ProductEntity;
import com.printxpress.app.databinding.FragmentProductDetailBinding;
import com.printxpress.app.util.AppExecutors;

public class ProductDetailFragment extends Fragment {
    private FragmentProductDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String productId = requireArguments().getString("productId");
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        AppExecutors.get().diskIo().execute(() -> {
            ProductEntity product = repo.getProduct(productId);
            AppExecutors.get().mainThread().execute(() -> {
                if (product == null || binding == null) {
                    return;
                }
                binding.toolbar.setTitle(product.name);
                binding.category.setText(ProductCategory.valueOf(product.category).getLabel());
                binding.description.setText(product.description);
                binding.cards.removeAllViews();
                UiHelpers.addInfoCard(binding.cards,
                        "From " + repo.priceLabel(product.basePriceLkr),
                        "Base unit price before quantity. Final total is calculated at checkout.");
                UiHelpers.addInfoCard(binding.cards, "Materials",
                        product.materials.replace(",", " · "));
                UiHelpers.addInfoCard(binding.cards, "Sizes",
                        product.sizes.replace(",", " · "));
                UiHelpers.addInfoCard(binding.cards, "Sample preview", product.samplePreview);
                binding.order.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("productId", product.id);
                    Navigation.findNavController(v).navigate(R.id.action_product_to_place_order, args);
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
