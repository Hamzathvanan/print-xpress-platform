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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.printxpress.app.R;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.ProductCatalog;
import com.printxpress.app.data.model.ProductCategory;
import com.printxpress.app.data.model.ProductEntity;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentHomeBinding;
import com.printxpress.app.databinding.ItemHomeHeaderBinding;
import com.printxpress.app.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeAdapter adapter;
    private UserEntity user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PrintXpressRepository repo = UiHelpers.repo(this);
        adapter = new HomeAdapter(repo, item -> {
            Bundle args = new Bundle();
            args.putString("productId", item.id);
            Navigation.findNavController(view).navigate(R.id.action_home_to_product, args);
        }, category -> {
            Bundle args = new Bundle();
            args.putString("category", category.name());
            Navigation.findNavController(view).navigate(R.id.action_home_to_category, args);
        }, () -> Navigation.findNavController(view).navigate(R.id.action_home_to_guidelines),
                () -> Navigation.findNavController(view).navigate(R.id.action_home_to_faq),
                () -> Navigation.findNavController(view).navigate(R.id.action_home_to_notifications));

        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);

        binding.toolbar.inflateMenu(R.menu.menu_home);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_notifications) {
                Navigation.findNavController(view).navigate(R.id.action_home_to_notifications);
                return true;
            }
            if (id == R.id.action_orders) {
                Navigation.findNavController(view).navigate(R.id.action_home_to_orders);
                return true;
            }
            if (id == R.id.action_profile) {
                Navigation.findNavController(view).navigate(R.id.action_home_to_profile);
                return true;
            }
            return false;
        });

        AppExecutors.get().diskIo().execute(() -> {
            UserEntity current = repo.currentUser();
            AppExecutors.get().mainThread().execute(() -> {
                user = current;
                if (user != null) {
                    String first = user.fullName.split(" ")[0];
                    binding.toolbar.setSubtitle("Hi, " + first);
                    repo.observeUnreadCount(user.id).observe(getViewLifecycleOwner(), count ->
                            adapter.setUnread(count == null ? 0 : count));
                }
            });
        });

        repo.observeProducts().observe(getViewLifecycleOwner(), products ->
                adapter.setProducts(products == null ? new ArrayList<>() : products));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_PRODUCT = 1;

        interface ProductClick {
            void onClick(ProductEntity product);
        }

        interface CategoryClick {
            void onClick(ProductCategory category);
        }

        private final PrintXpressRepository repo;
        private final ProductClick productClick;
        private final CategoryClick categoryClick;
        private final Runnable onGuidelines;
        private final Runnable onFaq;
        private final Runnable onNotifications;
        private final List<ProductEntity> products = new ArrayList<>();
        private int unread;

        HomeAdapter(PrintXpressRepository repo, ProductClick productClick,
                    CategoryClick categoryClick, Runnable onGuidelines, Runnable onFaq,
                    Runnable onNotifications) {
            this.repo = repo;
            this.productClick = productClick;
            this.categoryClick = categoryClick;
            this.onGuidelines = onGuidelines;
            this.onFaq = onFaq;
            this.onNotifications = onNotifications;
        }

        void setProducts(List<ProductEntity> next) {
            products.clear();
            products.addAll(next);
            notifyDataSetChanged();
        }

        void setUnread(int count) {
            unread = count;
            notifyItemChanged(0);
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_HEADER : TYPE_PRODUCT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                return new HeaderHolder(ItemHomeHeaderBinding.inflate(inflater, parent, false));
            }
            return new ProductHolder(com.printxpress.app.databinding.ItemInfoCardBinding.inflate(
                    inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderHolder) {
                HeaderHolder h = (HeaderHolder) holder;
                h.binding.categoryChips.removeAllViews();
                for (ProductCategory category : ProductCategory.values()) {
                    Chip chip = new Chip(h.itemView.getContext());
                    chip.setText(category.getLabel());
                    chip.setOnClickListener(v -> categoryClick.onClick(category));
                    h.binding.categoryChips.addView(chip);
                }
                h.binding.offersContainer.removeAllViews();
                for (String offer : ProductCatalog.SEASONAL_OFFERS) {
                    UiHelpers.addInfoCard(h.binding.offersContainer, "Offer", offer);
                }
                h.binding.guidelinesChip.setOnClickListener(v -> onGuidelines.run());
                h.binding.faqChip.setOnClickListener(v -> onFaq.run());
                if (unread > 0) {
                    h.binding.unreadBanner.setVisibility(View.VISIBLE);
                    h.binding.unreadBanner.setText(
                            "You have " + unread + " unread notification(s).");
                    h.binding.unreadBanner.setOnClickListener(v -> onNotifications.run());
                } else {
                    h.binding.unreadBanner.setVisibility(View.GONE);
                }
                return;
            }
            ProductEntity product = products.get(position - 1);
            ProductHolder h = (ProductHolder) holder;
            h.binding.title.setText(product.name);
            String categoryLabel = ProductCategory.valueOf(product.category).getLabel();
            h.binding.body.setText(categoryLabel + " · " + product.samplePreview);
            h.binding.trailing.setVisibility(View.VISIBLE);
            h.binding.trailing.setText(repo.priceLabel(product.basePriceLkr));
            h.binding.card.setOnClickListener(v -> productClick.onClick(product));
        }

        @Override
        public int getItemCount() {
            return 1 + products.size();
        }

        static class HeaderHolder extends RecyclerView.ViewHolder {
            final ItemHomeHeaderBinding binding;

            HeaderHolder(ItemHomeHeaderBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        static class ProductHolder extends RecyclerView.ViewHolder {
            final com.printxpress.app.databinding.ItemInfoCardBinding binding;

            ProductHolder(com.printxpress.app.databinding.ItemInfoCardBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
