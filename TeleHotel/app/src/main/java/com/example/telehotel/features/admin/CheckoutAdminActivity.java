package com.example.telehotel.features.admin;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.telehotel.R;
import com.example.telehotel.features.admin.fragments.CheckoutAdminFragment;

public class CheckoutAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        // Cargar el CheckoutAdminFragment
        if (savedInstanceState == null) {
            CheckoutAdminFragment fragment = new CheckoutAdminFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }
}