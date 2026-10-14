package com.printxpress.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.printxpress.app.R;
import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.AddressEntity;
import com.printxpress.app.data.model.DeliveryMode;
import com.printxpress.app.data.model.ProductEntity;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.databinding.FragmentPlaceOrderBinding;
import com.printxpress.app.util.AppExecutors;
import com.printxpress.app.util.Result;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlaceOrderFragment extends Fragment {
    private FragmentPlaceOrderBinding binding;
    private ProductEntity product;
    private UserEntity user;
    private String artworkUri;
    private String scheduledDate = "";
    private Long selectedAddressId;
    private List<AddressEntity> addresses = new ArrayList<>();
    private ActivityResultLauncher<String> picker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && binding != null) {
                artworkUri = uri.toString();
                binding.artworkUri.setVisibility(View.VISIBLE);
                binding.artworkUri.setText("Selected: " + artworkUri);
                binding.uploadArtwork.setText(R.string.change_artwork);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaceOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String productId = requireArguments().getString("productId");
        PrintXpressRepository repo = UiHelpers.repo(this);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.uploadArtwork.setOnClickListener(v -> picker.launch("image/*"));
        binding.chooseDate.setOnClickListener(v -> showDatePicker());
        binding.pickupChip.setOnCheckedChangeListener((button, checked) -> updateDeliveryUi());
        binding.deliveryChip.setOnCheckedChangeListener((button, checked) -> updateDeliveryUi());
        binding.quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotal(repo);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AppExecutors.get().diskIo().execute(() -> {
            UserEntity current = repo.currentUser();
            ProductEntity item = repo.getProduct(productId);
            AppExecutors.get().mainThread().execute(() -> {
                if (binding == null || current == null || item == null) {
                    return;
                }
                user = current;
                product = item;
                binding.productName.setText(item.name);
                String[] materials = item.materials.split(",");
                String[] sizes = item.sizes.split(",");
                ArrayAdapter<String> materialAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_list_item_1, materials);
                ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_list_item_1, sizes);
                binding.material.setAdapter(materialAdapter);
                binding.size.setAdapter(sizeAdapter);
                binding.material.setText(materials[0], false);
                binding.size.setText(sizes[0], false);
                updateTotal(repo);

                repo.observeAddresses(user.id).observe(getViewLifecycleOwner(), list -> {
                    addresses = list == null ? new ArrayList<>() : list;
                    List<String> labels = new ArrayList<>();
                    for (AddressEntity address : addresses) {
                        labels.add(address.id + "|" + address.label + " — "
                                + address.line1 + ", " + address.city);
                    }
                    ArrayAdapter<String> addressAdapter = new ArrayAdapter<>(
                            requireContext(), android.R.layout.simple_list_item_1, labels);
                    binding.address.setAdapter(addressAdapter);
                    if (selectedAddressId == null && !addresses.isEmpty()) {
                        AddressEntity preferred = null;
                        for (AddressEntity address : addresses) {
                            if (address.isDefault) {
                                preferred = address;
                                break;
                            }
                        }
                        if (preferred == null) {
                            preferred = addresses.get(0);
                        }
                        selectedAddressId = preferred.id;
                        binding.address.setText(
                                preferred.label + " — " + preferred.line1 + ", " + preferred.city,
                                false);
                    }
                    binding.address.setOnItemClickListener((parent, v, position, id) -> {
                        String chosen = labels.get(position);
                        selectedAddressId = Long.parseLong(chosen.substring(0, chosen.indexOf('|')));
                        binding.address.setText(chosen.substring(chosen.indexOf('|') + 1), false);
                    });
                    updateDeliveryUi();
                });
            });
        });

        binding.confirm.setOnClickListener(v -> confirmOrder(repo, view));
    }

    private void confirmOrder(PrintXpressRepository repo, View view) {
        if (product == null || user == null) {
            return;
        }
        binding.confirm.setEnabled(false);
        UiHelpers.showError(binding.error, null);
        int qty;
        try {
            qty = Integer.parseInt(text(binding.quantity));
        } catch (NumberFormatException e) {
            qty = 0;
        }
        DeliveryMode mode = binding.deliveryChip.isChecked()
                ? DeliveryMode.DELIVERY
                : DeliveryMode.PICKUP;
        int finalQty = qty;
        AppExecutors.get().diskIo().execute(() -> {
            Result<Long> result = repo.placeOrder(
                    user.id,
                    product,
                    finalQty,
                    text(binding.material),
                    text(binding.size),
                    text(binding.customText),
                    artworkUri,
                    mode,
                    mode == DeliveryMode.DELIVERY ? selectedAddressId : null,
                    scheduledDate,
                    text(binding.notes)
            );
            AppExecutors.get().mainThread().execute(() -> {
                if (binding == null) {
                    return;
                }
                binding.confirm.setEnabled(true);
                if (result.isSuccess()) {
                    Bundle args = new Bundle();
                    args.putLong("orderId", result.getValue());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_place_order_to_detail, args);
                } else {
                    UiHelpers.showError(binding.error, result.getErrorMessage());
                }
            });
        });
    }

    private void updateDeliveryUi() {
        if (binding == null) {
            return;
        }
        boolean delivery = binding.deliveryChip.isChecked();
        if (delivery) {
            if (addresses.isEmpty()) {
                binding.addressHint.setVisibility(View.VISIBLE);
                binding.addressHint.setText(
                        "Add a delivery address in Profile before ordering for delivery.");
                binding.addressLayout.setVisibility(View.GONE);
            } else {
                binding.addressHint.setVisibility(View.GONE);
                binding.addressLayout.setVisibility(View.VISIBLE);
            }
        } else {
            binding.addressHint.setVisibility(View.GONE);
            binding.addressLayout.setVisibility(View.GONE);
        }
    }

    private void updateTotal(PrintXpressRepository repo) {
        if (binding == null || product == null) {
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(text(binding.quantity));
        } catch (NumberFormatException e) {
            qty = 0;
        }
        double total = Math.max(0, product.basePriceLkr * qty);
        binding.total.setText("Estimated total\n" + repo.priceLabel(total));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, y, m, d) -> {
            scheduledDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            binding.chooseDate.setText("Date: " + scheduledDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static String text(android.widget.TextView view) {
        CharSequence value = view.getText();
        return value == null ? "" : value.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
