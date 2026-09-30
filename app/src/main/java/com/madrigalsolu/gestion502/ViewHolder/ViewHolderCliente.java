package com.madrigalsolu.gestion502.ViewHolder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madrigalsolu.gestion502.R;

public class ViewHolderCliente extends RecyclerView.ViewHolder {
    View mview;
    private ViewHolderCliente.clicklistener mclicklistener;
    public interface clicklistener{
        void onItemClick(View view, int position);
        void onItemLonClick(View view, int position);
    }
    public void setMclicklistener(ViewHolderCliente.clicklistener clicklistener){
        mclicklistener=clicklistener;

    };
    public ViewHolderCliente(@NonNull View itemView) {
        super(itemView);
        mview=itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mclicklistener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    mclicklistener.onItemClick(view, getAdapterPosition());
                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public  boolean onLongClick(View view){
                if (mclicklistener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    mclicklistener.onItemLonClick(view, getAdapterPosition());
                    return true;
                }
                return false;
            }
        });

    }
    public void setearDatosCliente(Context context, String id_cliente, String uid_cliente, String nombres,
                                   String apellidos, String correo, String telefono, String dni, String direccion){
        TextView tvnombreI, tvapellidosI, tvtelefonoI;

        tvnombreI=mview.findViewById(R.id.tvnombresI);
        tvapellidosI=mview.findViewById(R.id.tvapellidosI);
        tvtelefonoI=mview.findViewById(R.id.tvtelefonoI);

        // En la lista SOLO se muestran: nombre, apellido y celular
        if (tvnombreI != null) tvnombreI.setText(nombres != null ? nombres : "");
        if (tvapellidosI != null) tvapellidosI.setText(apellidos != null ? apellidos : "");
        if (tvtelefonoI != null) tvtelefonoI.setText(telefono != null ? telefono : "");
    }

    public void setearDatosResumidos(String nombres, String apellidos, String telefono){
        TextView tvnombreI = mview.findViewById(R.id.tvnombresI);
        TextView tvapellidosI = mview.findViewById(R.id.tvapellidosI);
        TextView tvtelefonoI = mview.findViewById(R.id.tvtelefonoI);
        if (tvnombreI != null) tvnombreI.setText(nombres != null ? nombres : "");
        if (tvapellidosI != null) tvapellidosI.setText(apellidos != null ? apellidos : "");
        if (tvtelefonoI != null) tvtelefonoI.setText(telefono != null ? telefono : "");
    }

}
