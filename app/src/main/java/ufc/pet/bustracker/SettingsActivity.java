package ufc.pet.bustracker;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    Spinner intervalos;
    Toolbar mToolbar;
    Switch notifica;
    int update_time;
    boolean notification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(mToolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch(NullPointerException exception){
            exibir_alerta(R.string.erro_botao_home_title, R.string.erro_botao_home_msg);
        }

        notifica = (Switch) findViewById(R.id.notificacoes_switch);

        //Spinner com os valores possíveis de intervalos
        intervalos = (Spinner) findViewById(R.id.intervalos);

        // Configuração da Spinner
        List<String> lista_intervalos = new ArrayList<String>();
        lista_intervalos.add("3");
        lista_intervalos.add("5");
        lista_intervalos.add("10");
        lista_intervalos.add("15");
        lista_intervalos.add("20");
        lista_intervalos.add("30");

        ArrayAdapter<String> intervalos_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lista_intervalos);
        intervalos_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        intervalos.setAdapter(intervalos_adapter);

        // Pega as preferências salvas
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        update_time = pref.getInt(getString(R.string.update_time), 3);
        notification = pref.getBoolean(getString(R.string.notifications), true);
        // Interpreta o valor salvo (3-30), e interpreta pra colocar na GUI
        Hashtable<Integer, Integer> tempo_real = new Hashtable<>();
        tempo_real.put(3, 0);
        tempo_real.put(5, 1);
        tempo_real.put(10, 2);
        tempo_real.put(15, 3);
        tempo_real.put(20, 4);
        tempo_real.put(30, 5);
        intervalos.setSelection(tempo_real.get(update_time));

        notifica.setChecked(notification);
    }

    /**
     * Salva as preferências do usuário ao sair da activity de configurações.
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        salvar_preferences();
    }

    @Override
    public void onPause(){
        super.onPause();
        salvar_preferences();
    }

    public void salvar_preferences(){
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE).edit();
        editor.putInt(getString(R.string.update_time), Integer.valueOf(intervalos.getSelectedItem().toString()));
        editor.putBoolean(getString(R.string.notifications), notifica.isChecked());
        editor.apply();
    }

    public void onClickSaveSettings(MenuItem item){
        salvar_preferences();
        Toast.makeText(this, "Configurações Salvas!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Cria o botão de deletar as notificações
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    public void exibir_alerta(int titleID, int msgID){
        AlertDialog.Builder alertD = new AlertDialog.Builder(this);

        alertD.setMessage(msgID);
        alertD.setTitle(titleID);
        alertD.setNeutralButton(R.string.botao_ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                finish();
            }
        });
        alertD.show();
    }

}
