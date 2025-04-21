package com.example.telehotel.features.taxista;
import com.example.telehotel.R;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class TaxistaNavigationHelper {
    public static void navigate(Context context, int itemId) {
        Intent intent = null;

        if (itemId == R.id.nav_home) {
            intent = new Intent(context, TaxistaActivity.class);
        } else if (itemId == R.id.nav_hotels) {
            intent = new Intent(context, TaxistaHoteles.class);
        } else if (itemId == R.id.nav_stats) {
            intent = new Intent(context, TaxistaStatsActivity.class);
        } else if (itemId == R.id.nav_profile) {
            intent = new Intent(context, TaxistaPerfil.class);
        }

        if (intent != null && !context.getClass().getName().equals(intent.getComponent().getClassName())) {
            context.startActivity(intent);
        }
    }
}
