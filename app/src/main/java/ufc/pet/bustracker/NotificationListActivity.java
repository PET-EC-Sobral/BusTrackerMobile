package ufc.pet.bustracker;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ufc.pet.bustracker.tools.NotificationsAdapter;
import ufc.pet.bustracker.ufc.pet.bustracker.types.NotificationObject;

public class NotificationListActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView lista_notifications;
    private NotificationsAdapter adapter;
    TextView notificationLabel;
    private RecyclerView.LayoutManager layout_manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_notifications);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lista_notifications = (RecyclerView) findViewById(R.id.recycler_view_notifications);

        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        String shared_retorno = pref.getString(getString(R.string.notification_data), "null");

        notificationLabel = (TextView) findViewById(R.id.no_notifications_label);

        if(!shared_retorno.equals("null")) {
            Type tipo = new TypeToken<ArrayList<NotificationObject>>() { }.getType();
            ArrayList<NotificationObject> dados = new Gson().fromJson(shared_retorno, tipo);
            adapter = new NotificationsAdapter(dados);
        }
        else{
            adapter = new NotificationsAdapter(new ArrayList<NotificationObject>());
        }

        if(adapter.getItemCount() == 0){
            notificationLabel.setVisibility(View.VISIBLE);
        } else{
            notificationLabel.setVisibility(View.GONE);
        }
        layout_manager = new LinearLayoutManager(this);

        lista_notifications.setLayoutManager(layout_manager);
        lista_notifications.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    public void onClickDeleteNotifications(MenuItem item){
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(getString(R.string.notification_data), "null");
        edit.apply();
        adapter.update_dados(new ArrayList<NotificationObject>());
        adapter.notifyDataSetChanged();
        notificationLabel.setVisibility(View.VISIBLE);
    }
}
