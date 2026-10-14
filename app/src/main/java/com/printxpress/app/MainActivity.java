package com.printxpress.app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.printxpress.app.data.PrintXpressRepository;
import com.printxpress.app.data.model.UserEntity;
import com.printxpress.app.util.AppExecutors;

public class MainActivity extends AppCompatActivity {
    private boolean routed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PrintXpressRepository repository = ((PrintXpressApp) getApplication()).getRepository();
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (host == null) {
            return;
        }
        NavController navController = host.getNavController();

        AppExecutors.get().diskIo().execute(() -> {
            UserEntity user = repository.currentUser();
            runOnUiThread(() -> {
                if (routed || user == null) {
                    return;
                }
                routed = true;
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .build();
                navController.navigate(R.id.homeFragment, null, options);
            });
        });
    }
}
