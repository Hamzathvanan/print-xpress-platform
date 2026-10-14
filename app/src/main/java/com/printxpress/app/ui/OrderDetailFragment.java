package com.printxpress.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.DeliveryMode;
import com.printxpress.app.data.model.OrderEntity;
import com.printxpress.app.data.model.OrderStatus;
import com.printxpress.app.databinding.FragmentOrderDetailBinding;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;

import java.util.Calendar;
import java.util.Locale;

public class OrderDetailFragment extends Fragment {
    private FragmentOrderDetailBinding binding;
    private OrderEntity current;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long orderId = requireArguments().getLong("orderId");
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.toolbar.setTitle("Order #" + orderId);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        repo.observeOrder(orderId).observe(getViewLifecycleOwner(), order -> {
            if (order == null || binding == null) {
                return;
            }
            current = order;
            render(repo, order);
        });
    }

    private void render(PrintXpressRepository repo, OrderEntity order) {
        OrderStatus status = OrderStatus.valueOf(order.status);
        boolean canEdit = status == OrderStatus.RECEIVED || status == OrderStatus.PROCESSING;
        boolean finished = status == OrderStatus.CANCELLED || status == OrderStatus.COMPLETED;

        binding.cards.removeAllViews();
        UiHelpers.addInfoCard(binding.cards, "Status", status.getLabel());
        UiHelpers.addInfoCard(binding.cards, "Specs",
                "Qty " + order.quantity + " · " + order.size + " · " + order.paperType);
        UiHelpers.addInfoCard(binding.cards,
                DeliveryMode.valueOf(order.deliveryMode).getLabel(),
                "Scheduled: " + order.scheduledDate);
        if (order.customText != null && !order.customText.trim().isEmpty()) {
            UiHelpers.addInfoCard(binding.cards, "Instructions", order.customText);
        }
        if (order.artworkUri != null && !order.artworkUri.trim().isEmpty()) {
            UiHelpers.addInfoCard(binding.cards, "Artwork", order.artworkUri);
        }
        UiHelpers.addInfoCard(binding.cards, "Total", repo.priceLabel(order.totalPriceLkr));

        binding.reschedule.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        binding.cancelOrder.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        binding.lockedNote.setVisibility(!canEdit && !finished ? View.VISIBLE : View.GONE);
        binding.demoAdvance.setVisibility(finished ? View.GONE : View.VISIBLE);
        binding.demoNote.setVisibility(finished ? View.GONE : View.VISIBLE);

        binding.reschedule.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, y, m, d) -> {
                String date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                AppExecutors.get().diskIo().execute(() -> {
                    Result<Void> result = repo.rescheduleOrder(order, date);
                    AppExecutors.get().mainThread().execute(() -> {
                        if (result.isSuccess()) {
                            showMessage("Rescheduled to " + date);
                            UiHelpers.showError(binding.error, null);
                        } else {
                            UiHelpers.showError(binding.error, result.getErrorMessage());
                        }
                    });
                });
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        binding.cancelOrder.setOnClickListener(v -> AppExecutors.get().diskIo().execute(() -> {
            Result<Void> result = repo.cancelOrder(order);
            AppExecutors.get().mainThread().execute(() -> {
                if (result.isSuccess()) {
                    showMessage("Order cancelled");
                    UiHelpers.showError(binding.error, null);
                } else {
                    UiHelpers.showError(binding.error, result.getErrorMessage());
                }
            });
        }));

        binding.demoAdvance.setOnClickListener(v -> AppExecutors.get().diskIo().execute(() -> {
            Result<Void> result = repo.advanceOrderDemo(order);
            AppExecutors.get().mainThread().execute(() -> {
                if (result.isSuccess()) {
                    showMessage("Status advanced");
                    UiHelpers.showError(binding.error, null);
                } else {
                    UiHelpers.showError(binding.error, result.getErrorMessage());
                }
            });
        }));
    }

    private void showMessage(String text) {
        if (binding == null) {
            return;
        }
        binding.message.setVisibility(View.VISIBLE);
        binding.message.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
