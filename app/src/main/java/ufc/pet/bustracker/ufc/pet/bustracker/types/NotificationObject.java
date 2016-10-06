package ufc.pet.bustracker.ufc.pet.bustracker.types;

/**
 * Created by Isaben on 06/10/2016.
 */
public class NotificationObject {

    private String mensagem;
    private String data;
    private String title;

    public NotificationObject(String title, String mensagem, String data ){
        this.mensagem = mensagem;
        this.data = data;
        this.title = title;
    }

    public void setMensagem(String mensagem){
        this.mensagem = mensagem;
    }
    public void setData(String data){
        this.data = data;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getMensagem(){
        return mensagem;
    }
    public String getData(){
        return data;
    }
    public String getTitle(){
        return title;
    }
}
