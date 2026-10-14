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
import com.printxpress.app.data.model.DeliveryMode;
import com.printxpress.app.data.model.OrderEntity;
import com.printxpress.app.data.model.OrderStatus;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentListBinding;
import com.printxpress.app.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {
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
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.toolbar.setTitle("My orders");
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        InfoCardAdapter adapter = new InfoCardAdapter(item -> {
            Bundle args = new Bundle();
            args.putLong("orderId", (Long) item.payload);
            Navigation.findNavController(view).navigate(R.id.action_orders_to_detail, args);
        });
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(adapter);

        AppExecutors.get().diskIo().execute(() -> {
            UserEntity user = repo.currentUser();
            AppExecutors.get().mainThread().execute(() -> {
                if (user == null || binding == null) {
                    return;
                }
                repo.observeOrders(user.id).observe(getViewLifecycleOwner(), orders -> {
                    List<InfoCardAdapter.Item> items = new ArrayList<>();
                    if (orders != null) {
                        for (OrderEntity order : orders) {
                            OrderStatus status = OrderStatus.valueOf(order.status);
                            DeliveryMode mode = DeliveryMode.valueOf(order.deliveryMode);
                            items.add(new InfoCardAdapter.Item(
                                    "Order #" + order.id,
                                    status.getLabel() + " · " + order.scheduledDate
                                            + " · " + mode.getLabel(),
                                    repo.priceLabel(order.totalPriceLkr),
                                    order.id
                            ));
                        }
                    }
                    adapter.submit(items);
                    binding.empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.empty.setText(
                            "No orders yet. Browse products to place your first print job.");
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
