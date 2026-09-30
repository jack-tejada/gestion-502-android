package com.madrigalsolu.gestion502.Clientes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;
import com.madrigalsolu.gestion502.R;

/**
 * Dialog estilo "Aviso de Aplicativo" (fondo azul, título blanco,
 * imagen grande) según diseño de las fotos de referencia.
 */
public class DialogAviso {

    public enum Icono {
        ALERTA_ROSA,      // sirena alarm.png (validaciones)
        CHECK_BLANCO,     // check verde checked.png (guardado OK)
        CHECK_VERDE,      // check verde checked.png (opciones)
        CHECK_AMARILLO,   // check verde checked.png (actualizado OK)
        PAPELERA_NARANJA  // tacho rojo trash.png (eliminado OK)
    }

    public interface OnOpcion {
        void ejecutar();
    }

    /** Muestra la animación Lottie si hay asset; si no, la imagen del icono indicado. */
    private static void mostrarIconoOAnimacion(View layout, String lottieAsset, Icono iconoFallback) {
        ImageView iv = layout.findViewById(R.id.ivDialogIcono);
        LottieAnimationView anim = layout.findViewById(R.id.lottieDialogIcono);
        if (lottieAsset != null && !lottieAsset.isEmpty()) {
            iv.setVisibility(View.GONE);
            anim.setVisibility(View.VISIBLE);
            anim.setAnimation(lottieAsset);
            anim.playAnimation();
        } else {
            anim.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
            aplicarIcono(layout, iconoFallback);
        }
    }

    /** Solo imagen (validaciones, opciones): oculta la animación. */
    private static void mostrarSoloImagen(View layout, Icono icono) {
        layout.findViewById(R.id.lottieDialogIcono).setVisibility(View.GONE);
        layout.findViewById(R.id.ivDialogIcono).setVisibility(View.VISIBLE);
        aplicarIcono(layout, icono);
    }
    private static void aplicarIcono(View layout, Icono icono) {
        ImageView ivIcono = layout.findViewById(R.id.ivDialogIcono);
        switch (icono) {
            case ALERTA_ROSA:
                ivIcono.setImageResource(R.drawable.alarm);
                break;
            case CHECK_BLANCO:
            case CHECK_VERDE:
            case CHECK_AMARILLO:
                ivIcono.setImageResource(R.drawable.checked);
                break;
            case PAPELERA_NARANJA:
                ivIcono.setImageResource(R.drawable.trash);
                break;
        }
    }

    private static Dialog crearBase(Context context, View layout) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // El dialog por defecto es angosto y corta el texto ("Aviso de",
            // "Seleccione una", "Edita/Elimi"): forzar ancho casi completo.
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    /** Dialog informativo con botón "Entiendo" (validaciones por campo). */
    public static void mostrarInfo(Context context, String mensaje, Icono icono) {
        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_aviso, null);
        ((TextView) layout.findViewById(R.id.tvDialogTitulo)).setText("Aviso de Aplicativo");
        ((TextView) layout.findViewById(R.id.tvDialogMensaje)).setText(mensaje);
        mostrarSoloImagen(layout, icono);
        layout.findViewById(R.id.layoutOpciones).setVisibility(View.GONE);
        Button btn = layout.findViewById(R.id.btnDialogEntendido);
        btn.setVisibility(View.VISIBLE);
        Dialog dialog = crearBase(context, layout);
        dialog.setCancelable(true);
        btn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Dialog de éxito SIN botón: se cierra solo a los 3 segundos (máx. 5s).
     * Si se indica un asset Lottie (ej. "success.json") se muestra la animación;
     * si no, la imagen del icono indicado.
     */
    public static void mostrarExitoAuto(Context context, String mensaje,
                                        String lottieAsset, Icono iconoFallback, Runnable alCerrar) {
        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_aviso, null);
        ((TextView) layout.findViewById(R.id.tvDialogTitulo)).setText("Aviso de Aplicativo");
        ((TextView) layout.findViewById(R.id.tvDialogMensaje)).setText(mensaje);
        mostrarIconoOAnimacion(layout, lottieAsset, iconoFallback);
        layout.findViewById(R.id.layoutOpciones).setVisibility(View.GONE);
        layout.findViewById(R.id.btnDialogEntendido).setVisibility(View.GONE);
        Dialog dialog = crearBase(context, layout);
        dialog.setCancelable(false); // sin botón ni toque fuera: solo se cierra con el tiempo
        dialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (dialog.isShowing()) dialog.dismiss();
            } catch (Exception ignored) {
            }
            if (alCerrar != null) alCerrar.run();
        }, 3000); // 3 segundos (máximo permitido: 5)
    }

    /** Dialog de click prolongado: "Seleccione una Opción" con Editar y Eliminar. */
    public static void mostrarOpciones(Context context, OnOpcion onEditar, OnOpcion onEliminar) {
        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_aviso, null);
        ((TextView) layout.findViewById(R.id.tvDialogTitulo)).setText("Aviso de Aplicativo");
        ((TextView) layout.findViewById(R.id.tvDialogMensaje)).setText("Seleccione una Opción");
        mostrarSoloImagen(layout, Icono.CHECK_VERDE);
        layout.findViewById(R.id.btnDialogEntendido).setVisibility(View.GONE);
        layout.findViewById(R.id.layoutOpciones).setVisibility(View.VISIBLE);
        Dialog dialog = crearBase(context, layout);
        dialog.setCancelable(true);
        layout.findViewById(R.id.btnOpcionEditar).setOnClickListener(v -> {
            dialog.dismiss();
            if (onEditar != null) onEditar.ejecutar();
        });
        layout.findViewById(R.id.btnOpcionEliminar).setOnClickListener(v -> {
            dialog.dismiss();
            if (onEliminar != null) onEliminar.ejecutar();
        });
        dialog.show();
    }

    /** Confirmación blanca pequeña estilo "¿Está seguro...? CANCELAR / CONFIRMAR". */
    public static void mostrarConfirmacion(Context context, String mensaje, OnOpcion onConfirmar) {
        new AlertDialog.Builder(context)
                .setTitle("Aviso de Aplicativo")
                .setMessage(mensaje)
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("CONFIRMAR", (d, w) -> {
                    if (onConfirmar != null) onConfirmar.ejecutar();
                })
                .show();
    }
}
