package ufc.pet.bustracker.tools;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ufc.pet.bustracker.R;
import ufc.pet.bustracker.ufc.pet.bustracker.types.NotificationObject;

/**
 * Created by Isaben on 06/10/2016.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private ArrayList<NotificationObject> dados;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title;
        public TextView mensagem;
        public TextView data;

        public ViewHolder(View view){
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            mensagem = (TextView) view.findViewById(R.id.mensagem);
            data = (TextView) view.findViewById(R.id.data);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            Log.i("do", "nothing");
        }
    }

    public NotificationsAdapter(ArrayList<NotificationObject> dados){
        this.dados = dados;
    }

    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(ViewGroup pai, int tipoView){
        View v = LayoutInflater.from(pai.getContext()).inflate(R.layout.cardview_notifications, pai, false);

        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        String title = dados.get(position).getTitle();
        String mensagem = dados.get(position).getMensagem();
        String data = dados.get(position).getData();

        holder.title.setText(title);
        holder.mensagem.setText(mensagem);
        holder.data.setText(data);
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public void update_dados(ArrayList<NotificationObject> novos){
        this.dados = novos;
    }

}
