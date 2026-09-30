package com.madrigalsolu.gestion502.Clientes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madrigalsolu.gestion502.Clases.Cliente;
import com.madrigalsolu.gestion502.R;
import com.madrigalsolu.gestion502.ViewHolder.ViewHolderCliente;

import java.util.List;

public class ClienteAdapter extends RecyclerView.Adapter<ViewHolderCliente> {

    public interface OnClienteListener {
        void onItemClick(Cliente cliente);
        void onItemLongClick(Cliente cliente);
    }

    private final List<Cliente> lista;
    private final OnClienteListener listener;

    public ClienteAdapter(List<Cliente> lista, OnClienteListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolderCliente onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ViewHolderCliente(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderCliente holder, int position) {
        Cliente cliente = lista.get(position);
        holder.setearDatosResumidos(cliente.getNombres(), cliente.getApellidos(), cliente.getTelefono());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(cliente);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(cliente);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
