package com.madrigalsolu.gestion502.Clientes;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.madrigalsolu.gestion502.R;

/**
 * Dialog estilo "Aviso de Aplicativo" (fondo azul, título blanco,
 * animación/imagen grande) según diseño de las fotos de referencia.
 *
 * Blindado: cualquier fallo al mostrar un dialog cae a un Toast y
 * nunca rompe la app.
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

    private static boolean contextoValido(Context context) {
        if (context == null) return false;
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            if (a.isFinishing() || a.isDestroyed()) return false;
        }
        return true;
    }

    private static void toastSeguro(Context context, String mensaje) {
        try {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
        } catch (Exception ignored) {
        }
    }

    private static void cerrarSeguro(Dialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    private static void aplicarIcono(View layout, Icono icono) {
        ImageView ivIcono = layout.findViewById(R.id.ivDialogIcono);
        if (ivIcono == null) return;
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

    /** Solo imagen (validaciones, opciones, éxitos): sin Lottie en dialogs. */
    private static void mostrarSoloImagen(View layout, Icono icono) {
        try {
            View iv = layout.findViewById(R.id.ivDialogIcono);
            if (iv != null) iv.setVisibility(View.VISIBLE);
            aplicarIcono(layout, icono);
        } catch (Exception ignored) {
        }
    }

    private static Dialog crearBase(Context context, View layout) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Ventana a ancho completo y la tarjeta azul lleva sus propios
            // márgenes laterales: así no choca con los bordes de la pantalla.
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    /** Dialog informativo con botón "Entiendo" (validaciones por campo, imagen de alerta). */
    public static void mostrarInfo(Context context, String mensaje, Icono icono) {
        if (!contextoValido(context)) return;
        try {
            View layout = LayoutInflater.from(context).inflate(R.layout.dialog_aviso, null);
            ((TextView) layout.findViewById(R.id.tvDialogTitulo)).setText("Aviso de Aplicativo");
            ((TextView) layout.findViewById(R.id.tvDialogMensaje)).setText(mensaje);
            mostrarSoloImagen(layout, icono);
            layout.findViewById(R.id.layoutOpciones).setVisibility(View.GONE);
            Button btn = layout.findViewById(R.id.btnDialogEntendido);
            btn.setVisibility(View.VISIBLE);
            Dialog dialog = crearBase(context, layout);
            dialog.setCancelable(true);
            btn.setOnClickListener(v -> cerrarSeguro(dialog));
            dialog.show();
        } catch (Exception e) {
            toastSeguro(context, mensaje);
        }
    }

    /**
     * Dialog de éxito SIN botón: se cierra solo a los 3 segundos (máx. 5s).
     * Usa imagen estática (sin Lottie en dialogs para máxima estabilidad).
     */
    public static void mostrarExitoAuto(Context context, String mensaje,
                                        Icono icono, Runnable alCerrar) {
        if (!contextoValido(context)) {
            if (alCerrar != null) {
                try {
                    alCerrar.run();
                } catch (Exception ignored) {
                }
            }
            return;
        }
        try {
            View layout = LayoutInflater.from(context).inflate(R.layout.dialog_aviso, null);
            ((TextView) layout.findViewById(R.id.tvDialogTitulo)).setText("Aviso de Aplicativo");
            ((TextView) layout.findViewById(R.id.tvDialogMensaje)).setText(mensaje);
            mostrarSoloImagen(layout, icono);
            layout.findViewById(R.id.layoutOpciones).setVisibility(View.GONE);
            layout.findViewById(R.id.btnDialogEntendido).setVisibility(View.GONE);
            Dialog dialog = crearBase(context, layout);
            dialog.setCancelable(false); // sin botón ni toque fuera: solo se cierra con el tiempo
            dialog.show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                cerrarSeguro(dialog);
                if (alCerrar != null) {
                    try {
                        alCerrar.run();
                    } catch (Exception ignored) {
                    }
                }
            }, 3000); // 3 segundos (máximo permitido: 5)
        } catch (Exception e) {
            toastSeguro(context, mensaje);
            if (alCerrar != null) {
                try {
                    alCerrar.run();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Dialog de click prolongado: "Seleccione una Opción" con Editar y Eliminar. */
    public static void mostrarOpciones(Context context, OnOpcion onEditar, OnOpcion onEliminar) {
        if (!contextoValido(context)) return;
        try {
            View layout = LayoutInflater.from(context).inflate(R.layout.dialog_aviso, null);
            ((TextView) layout.findViewById(R.id.tvDialogTitulo)).setText("Aviso de Aplicativo");
            ((TextView) layout.findViewById(R.id.tvDialogMensaje)).setText("Seleccione una Opción");
            mostrarSoloImagen(layout, Icono.CHECK_VERDE);
            layout.findViewById(R.id.btnDialogEntendido).setVisibility(View.GONE);
            layout.findViewById(R.id.layoutOpciones).setVisibility(View.VISIBLE);
            Dialog dialog = crearBase(context, layout);
            dialog.setCancelable(true);
            layout.findViewById(R.id.btnOpcionEditar).setOnClickListener(v -> {
                cerrarSeguro(dialog);
                if (onEditar != null) {
                    try {
                        onEditar.ejecutar();
                    } catch (Exception ignored) {
                    }
                }
            });
            layout.findViewById(R.id.btnOpcionEliminar).setOnClickListener(v -> {
                cerrarSeguro(dialog);
                if (onEliminar != null) {
                    try {
                        onEliminar.ejecutar();
                    } catch (Exception ignored) {
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
            toastSeguro(context, "Seleccione una Opción");
        }
    }

    /** Confirmación blanca pequeña estilo "¿Está seguro...? CANCELAR / CONFIRMAR". */
    public static void mostrarConfirmacion(Context context, String mensaje, OnOpcion onConfirmar) {
        if (!contextoValido(context)) return;
        try {
            new AlertDialog.Builder(context)
                    .setTitle("Aviso de Aplicativo")
                    .setMessage(mensaje)
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton("CONFIRMAR", (d, w) -> {
                        if (onConfirmar != null) {
                            try {
                                onConfirmar.ejecutar();
                            } catch (Exception ignored) {
                            }
                        }
                    })
                    .show();
        } catch (Exception e) {
            // Si el dialog no se puede mostrar, se ejecuta igual la acción
            // para no dejar al usuario bloqueado... no: mejor avisar.
            toastSeguro(context, mensaje);
        }
    }
}
