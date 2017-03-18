package ufc.pet.bustracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import ufc.pet.bustracker.ufc.pet.bustracker.types.NotificationObject;

/**
 * Created by Isaben on 06/09/2016.
 * Classe para serviço de mensagens online do Firebase. É um serviço que fica
 * "escutando" sempre e age ao receber algum pacote.
 */
public class FirebaseService extends FirebaseMessagingService {

    /**
     * Método que é chamado sempre que uma mensagem é recebida.
     * @param remoteMessage objeto da mensagem recebida
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e("Recebeu algo!", "feijoada");
        if (remoteMessage.getNotification() != null) {
            Log.d("Mermão", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        Map<String, String> mensagem = remoteMessage.getData();
        listar(mensagem);
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        if(pref.getBoolean(getString(R.string.notifications), true))
            notificar(mensagem.get("message"), mensagem.get("title"));
    }

    /**
     * Chamada sempre que o serviço receber uma mensagem, invoca uma notificação para
     * o usuário.
     * @param messageBody conteúdo da mensagem
     * @param title título da notificação
     */
    private void notificar(String messageBody, String title) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    /**
     * Chamada sempre que uma mensagem chegar, salva a mesma na SharedPreferences que
     * guarda todas as notificações recebidas, na intenção de serem mostradas na
     * NotificationListActivity
     * @param notification o Map da notificação em si
     */
    private void listar(Map<String, String> notification){
        SharedPreferences pref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        NotificationObject nova = new NotificationObject(notification.get("title"), notification.get("message"), notification.get("date"));
        String shared_retorno = pref.getString(getString(R.string.notification_data), "null");
        SharedPreferences.Editor editor = pref.edit();
        ArrayList<NotificationObject> dados = new ArrayList<>();

        if(!shared_retorno.equals("null")) {
            Type tipo = new TypeToken<ArrayList<NotificationObject>>() { }.getType();
            dados = new Gson().fromJson(shared_retorno, tipo);
        }

        dados.add(nova);
        String dados_novos = new Gson().toJson(dados);
        editor.putString(getString(R.string.notification_data), dados_novos);
        editor.apply();

    }
}
