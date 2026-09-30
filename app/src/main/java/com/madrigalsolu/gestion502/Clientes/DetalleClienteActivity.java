package com.madrigalsolu.gestion502.Clientes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.madrigalsolu.gestion502.R;

public class DetalleClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_cliente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvNombres = findViewById(R.id.tvDetNombres);
        TextView tvApellidos = findViewById(R.id.tvDetApellidos);
        TextView tvCorreo = findViewById(R.id.tvDetCorreo);
        TextView tvTelefono = findViewById(R.id.tvDetTelefono);
        TextView tvDni = findViewById(R.id.tvDetDni);
        TextView tvDireccion = findViewById(R.id.tvDetDireccion);

        if (getIntent() != null) {
            tvNombres.setText(orDash(getIntent().getStringExtra("nombres")));
            tvApellidos.setText(orDash(getIntent().getStringExtra("apellidos")));
            tvCorreo.setText(orDash(getIntent().getStringExtra("correo")));
            tvTelefono.setText(orDash(getIntent().getStringExtra("telefono")));
            tvDni.setText(orDash(getIntent().getStringExtra("dni")));
            tvDireccion.setText(orDash(getIntent().getStringExtra("direccion")));
        }

        findViewById(R.id.btnVolverDetalle).setOnClickListener(v -> finish());
    }

    private String orDash(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }
}
