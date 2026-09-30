package com.madrigalsolu.gestion502.Clientes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madrigalsolu.gestion502.Clases.Cliente;
import com.madrigalsolu.gestion502.R;

import java.util.ArrayList;
import java.util.List;

public class ListaClienteActivity extends AppCompatActivity {

    RecyclerView recyclerViewClientes;
    EditText etBuscarCliente;
    TextView tvListaVacia;
    FloatingActionButton btnagregarcliente;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference refClientes;

    // Lista completa (Firebase) y lista mostrada (filtrada)
    final List<Cliente> listaTotal = new ArrayList<>();
    final List<Cliente> listaFiltrada = new ArrayList<>();
    ClienteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_cliente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewClientes = findViewById(R.id.recylerviewClientes);
        etBuscarCliente = findViewById(R.id.etBuscarCliente);
        tvListaVacia = findViewById(R.id.tvListaVacia);
        recyclerViewClientes.setHasFixedSize(true);
        recyclerViewClientes.setLayoutManager(new GridLayoutManager(this, 2));

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "Inicia sesión para ver tus clientes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refClientes = firebaseDatabase.getReference("Usuarios")
                .child(firebaseUser.getUid()).child("clientes");

        adapter = new ClienteAdapter(listaFiltrada, new ClienteAdapter.OnClienteListener() {
            @Override
            public void onItemClick(Cliente cliente) {
                // Click corto -> pestaña de detalle con datos completos
                Intent intent = new Intent(ListaClienteActivity.this, DetalleClienteActivity.class);
                intent.putExtra("id_cliente", cliente.getId_cliente());
                intent.putExtra("nombres", cliente.getNombres());
                intent.putExtra("apellidos", cliente.getApellidos());
                intent.putExtra("correo", cliente.getCorreo());
                intent.putExtra("telefono", cliente.getTelefono());
                intent.putExtra("dni", cliente.getDni());
                intent.putExtra("direccion", cliente.getDireccion());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Cliente cliente) {
                // Click prolongado -> dialog con opciones Editar y Borrar
                mostrarDialogOpciones(cliente);
            }
        });
        recyclerViewClientes.setAdapter(adapter);

        // Barra de búsqueda: filtra por nombre, apellido y celular
        etBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarClientes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnagregarcliente = findViewById(R.id.btnagregarcliente);
        btnagregarcliente.setOnClickListener(view ->
                startActivity(new Intent(ListaClienteActivity.this, AgregarClienteActivity.class)));

        escucharClientes();
    }

    private void escucharClientes() {
        refClientes.orderByChild("nombres").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaTotal.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Cliente c = ds.getValue(Cliente.class);
                    if (c != null) listaTotal.add(c);
                }
                // Reaplicar el filtro actual cada vez que cambian los datos
                String texto = etBuscarCliente != null ? etBuscarCliente.getText().toString() : "";
                filtrarClientes(texto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaClienteActivity.this,
                        "Error al cargar clientes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Búsqueda local por nombre, apellido y celular (insensible a mayúsculas). */
    private void filtrarClientes(String texto) {
        String q = texto == null ? "" : texto.trim().toLowerCase();
        listaFiltrada.clear();
        if (q.isEmpty()) {
            listaFiltrada.addAll(listaTotal);
        } else {
            for (Cliente c : listaTotal) {
                String nom = safe(c.getNombres()).toLowerCase();
                String ape = safe(c.getApellidos()).toLowerCase();
                String tel = safe(c.getTelefono()).toLowerCase();
                if (nom.contains(q) || ape.contains(q) || tel.contains(q)) {
                    listaFiltrada.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
        boolean vacia = listaFiltrada.isEmpty();
        tvListaVacia.setVisibility(vacia ? View.VISIBLE : View.GONE);
        tvListaVacia.setText(listaTotal.isEmpty()
                ? "Aún no hay clientes registrados. Presiona + para registrar uno."
                : "No se encontraron clientes.");
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void mostrarDialogOpciones(Cliente cliente) {
        // Click prolongado -> dialog azul "Seleccione una Opción" con Editar y Eliminar
        DialogAviso.mostrarOpciones(this,
                () -> {
                    // Editar -> abre el formulario en modo edición
                    Intent intent = new Intent(ListaClienteActivity.this, AgregarClienteActivity.class);
                    intent.putExtra("modo_edicion", true);
                    intent.putExtra("id_cliente", cliente.getId_cliente());
                    intent.putExtra("nombres", cliente.getNombres());
                    intent.putExtra("apellidos", cliente.getApellidos());
                    intent.putExtra("correo", cliente.getCorreo());
                    intent.putExtra("telefono", cliente.getTelefono());
                    intent.putExtra("dni", cliente.getDni());
                    intent.putExtra("direccion", cliente.getDireccion());
                    startActivity(intent);
                },
                () -> mostrarDialogEliminar(cliente));
    }

    private void mostrarDialogEliminar(Cliente cliente) {
        if (cliente.getId_cliente() == null || cliente.getId_cliente().isEmpty()) {
            Toast.makeText(this, "No se puede eliminar: registro sin ID", Toast.LENGTH_SHORT).show();
            return;
        }
        DialogAviso.mostrarConfirmacion(this,
                "¿Está seguro de Eliminar el Cliente?",
                () -> refClientes.child(cliente.getId_cliente()).removeValue()
                        .addOnSuccessListener(a -> DialogAviso.mostrarExitoAuto(
                                ListaClienteActivity.this,
                                "Cliente Eliminado Correctamente",
                                "deleteanimation.json",
                                DialogAviso.Icono.PAPELERA_NARANJA, null))
                        .addOnFailureListener(e -> Toast.makeText(this,
                                "No se pudo eliminar", Toast.LENGTH_SHORT).show()));
    }
}
