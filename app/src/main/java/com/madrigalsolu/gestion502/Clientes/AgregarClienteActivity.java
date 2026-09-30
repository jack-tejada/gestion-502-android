package com.madrigalsolu.gestion502.Clientes;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.madrigalsolu.gestion502.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.madrigalsolu.gestion502.Clases.Cliente;

public class AgregarClienteActivity extends AppCompatActivity {
    TextView uidusuario_l;
    EditText nombrescli, apellidoscli, correocli, dnicli, telefonocli;
    AutoCompleteTextView departamentoCli;
    TextInputLayout tilNombres, tilApellidos, tilCorreo, tilDni, tilTelefono, tilDepartamento;
    Button btnguardarcliente;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference BD_Usuario;

    boolean modoEdicion = false;
    String idClienteEditar = null;
    boolean guardando = false; // evita doble tap en Guardar (doble dialog / doble registro)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_cliente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        inicializarVariable();

        // Desplegable de departamentos del Perú
        ArrayAdapter<String> adapterDepas = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.departamentos_peru));
        departamentoCli.setAdapter(adapterDepas);

        // Si viene de Editar (long-click -> Editar), precargar datos
        if (getIntent() != null && getIntent().getBooleanExtra("modo_edicion", false)) {
            modoEdicion = true;
            idClienteEditar = getIntent().getStringExtra("id_cliente");
            nombrescli.setText(orEmpty(getIntent().getStringExtra("nombres")));
            apellidoscli.setText(orEmpty(getIntent().getStringExtra("apellidos")));
            correocli.setText(orEmpty(getIntent().getStringExtra("correo")));
            dnicli.setText(orEmpty(getIntent().getStringExtra("dni")));
            telefonocli.setText(orEmpty(getIntent().getStringExtra("telefono")));
            departamentoCli.setText(orEmpty(getIntent().getStringExtra("direccion")), false);
            ((TextView) findViewById(R.id.tvTituloForm)).setText("Actualizar Cliente");
            btnguardarcliente.setText("Actualizar");
        }

        btnguardarcliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1) Validación por campo: error inline + dialog azul por cada campo
                if (!validarCampos()) return;
                // 2) Dialog de confirmación antes de guardar
                mostrarDialogConfirmacion();
            }
        });
    }

    private String orEmpty(String s) {
        return s == null ? "" : s;
    }

    private void inicializarVariable() {
        btnguardarcliente = findViewById(R.id.btnGuardar);
        uidusuario_l = findViewById(R.id.tvUidUsuario);
        nombrescli = findViewById(R.id.etNombresCliente);
        apellidoscli = findViewById(R.id.etApellidosCliente);
        correocli = findViewById(R.id.etcorreocli);
        dnicli = findViewById(R.id.etDni);
        telefonocli = findViewById(R.id.etTelefono);
        departamentoCli = findViewById(R.id.actvDepartamento);
        tilNombres = findViewById(R.id.tilNombres);
        tilApellidos = findViewById(R.id.tilApellidos);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilDni = findViewById(R.id.tilDni);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilDepartamento = findViewById(R.id.tilDepartamento);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        BD_Usuario = FirebaseDatabase.getInstance().getReference("Usuarios");
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            uidusuario_l.setText("UID: " + uid);
            BD_Usuario.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nombres = snapshot.child("nombres").getValue(String.class);
                    String apellidos = snapshot.child("apellidos").getValue(String.class);
                    String nombreCompleto = ((nombres == null ? "" : nombres) + " " +
                            (apellidos == null ? "" : apellidos)).trim();
                    TextView tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
                    if (!nombreCompleto.isEmpty()) {
                        tvNombreUsuario.setText("Nombre: " + nombreCompleto);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    // ---------- Dialog de validación estilo "Aviso de Aplicativo" ----------
    private void dialogValidacion(String mensaje) {
        DialogAviso.mostrarInfo(this, mensaje, DialogAviso.Icono.ALERTA_ROSA);
    }

    private void limpiarErrores() {
        tilNombres.setError(null);
        tilApellidos.setError(null);
        tilCorreo.setError(null);
        tilDni.setError(null);
        tilTelefono.setError(null);
        tilDepartamento.setError(null);
    }

    /** Valida campo por campo (como en navegador): marca el campo y avisa con dialog cuál falta. */
    private boolean validarCampos() {
        limpiarErrores();
        String nombres = nombrescli.getText().toString().trim();
        String apellidos = apellidoscli.getText().toString().trim();
        String correo = correocli.getText().toString().trim();
        String dni = dnicli.getText().toString().trim();
        String telefono = telefonocli.getText().toString().trim();
        String departamento = departamentoCli.getText().toString().trim();

        if (nombres.isEmpty()) {
            tilNombres.setError("Falta el nombre");
            dialogValidacion("Debe ingresar como mínimo el nombre del Cliente");
            nombrescli.requestFocus();
            return false;
        }
        if (nombres.length() < 2) {
            tilNombres.setError("Muy corto");
            dialogValidacion("El nombre es muy corto. Debe tener al menos 2 letras.");
            nombrescli.requestFocus();
            return false;
        }
        if (apellidos.isEmpty()) {
            tilApellidos.setError("Faltan los apellidos");
            dialogValidacion("Debe ingresar los apellidos del Cliente");
            apellidoscli.requestFocus();
            return false;
        }
        if (correo.isEmpty()) {
            tilCorreo.setError("Falta el correo");
            dialogValidacion("Debe ingresar el correo del Cliente. Ejemplo: cliente@correo.com");
            correocli.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Correo inválido");
            dialogValidacion("El correo ingresado no es válido. Revise que tenga @ y dominio.");
            correocli.requestFocus();
            return false;
        }
        if (dni.isEmpty()) {
            tilDni.setError("Falta el DNI");
            dialogValidacion("Debe ingresar el DNI del Cliente");
            dnicli.requestFocus();
            return false;
        }
        if (!dni.matches("\\d{8}")) {
            tilDni.setError("Deben ser 8 números");
            dialogValidacion("El DNI debe tener exactamente 8 números. Ejemplo: 12345678");
            dnicli.requestFocus();
            return false;
        }
        if (telefono.isEmpty()) {
            tilTelefono.setError("Falta el celular");
            dialogValidacion("Debe ingresar el celular del Cliente");
            telefonocli.requestFocus();
            return false;
        }
        if (!telefono.matches("\\d{9}")) {
            tilTelefono.setError("Deben ser 9 números");
            dialogValidacion("El celular debe tener exactamente 9 números. Ejemplo: 987654321");
            telefonocli.requestFocus();
            return false;
        }
        if (departamento.isEmpty()) {
            tilDepartamento.setError("Seleccione el departamento");
            dialogValidacion("Debe seleccionar el departamento del Cliente en el desplegable");
            departamentoCli.requestFocus();
            departamentoCli.showDropDown();
            return false;
        }
        return true;
    }

    // ---------- Dialog de confirmación antes de guardar ----------
    private void mostrarDialogConfirmacion() {
        DialogAviso.mostrarConfirmacion(this,
                modoEdicion ? "¿Desea actualizar este Cliente?" : "¿Desea guardar este Cliente?",
                this::guardarCliente);
    }

    private void guardarCliente() {
        if (isFinishing() || isDestroyed()) return;
        if (guardando) return; // ya hay un guardado en curso
        guardando = true;
        btnguardarcliente.setEnabled(false);
        if (firebaseUser == null) {
            guardando = false;
            btnguardarcliente.setEnabled(true);
            Toast.makeText(this, "Inicia sesión para registrar un cliente", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = firebaseUser.getUid();
        String nombres = nombrescli.getText().toString().trim();
        String apellidos = apellidoscli.getText().toString().trim();
        String correo = correocli.getText().toString().trim();
        String dni = dnicli.getText().toString().trim();
        String telefono = telefonocli.getText().toString().trim();
        String direccion = departamentoCli.getText().toString().trim();

        DatabaseReference clientes = BD_Usuario.child(uid).child("clientes");

        if (modoEdicion && idClienteEditar != null) {
            Cliente cliente = new Cliente(idClienteEditar, uid, nombres, apellidos, correo, telefono, dni, direccion);
            clientes.child(idClienteEditar).setValue(cliente)
                    .addOnSuccessListener(unused -> mostrarDialogExitoAuto("Cliente Actualizado Correctamente", true))
                    .addOnFailureListener(error -> {
                        guardando = false;
                        btnguardarcliente.setEnabled(true);
                        dialogValidacion("No se pudo actualizar el cliente. Intente de nuevo.");
                    });
        } else {
            String id_cliente = clientes.push().getKey();
            if (id_cliente == null) {
                guardando = false;
                btnguardarcliente.setEnabled(true);
                dialogValidacion("No se pudo generar el ID del cliente. Intente de nuevo.");
                return;
            }
            Cliente cliente = new Cliente(id_cliente, uid, nombres, apellidos, correo, telefono, dni, direccion);
            clientes.child(id_cliente).setValue(cliente)
                    .addOnSuccessListener(unused -> mostrarDialogExitoAuto("Cliente Agregado Correctamente", false))
                    .addOnFailureListener(error -> {
                        guardando = false;
                        btnguardarcliente.setEnabled(true);
                        dialogValidacion("No se pudo registrar el cliente. Intente de nuevo.");
                    });
        }
    }

    /**
     * Dialog de éxito SIN botón de cierre: se cierra solo a los 3 segundos.
     */
    private void mostrarDialogExitoAuto(String mensaje, boolean esActualizacion) {
        DialogAviso.mostrarExitoAuto(this, mensaje,
                esActualizacion ? DialogAviso.Icono.CHECK_AMARILLO : DialogAviso.Icono.CHECK_BLANCO,
                this::finish);
    }
}
