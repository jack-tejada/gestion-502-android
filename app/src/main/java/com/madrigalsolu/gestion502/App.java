package com.madrigalsolu.gestion502;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * El diseño de referencia de la app (listas, formularios, dialogs)
 * es en modo claro, por eso se fuerza el tema claro en toda la app
 * para que los colores del sistema de diseño no cambien con el modo
 * oscuro del teléfono.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
