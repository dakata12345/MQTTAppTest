package com.example.valen.mqttapptest;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    static String MQTTHOST = "tcp://172.20.10.9:1883";
   // static String USERNAME = "pi";
    //static String PASSWORD = "raspberry";
    String topicStr = "gateway/b827ebffff119b2b/rx";
    String topicDecoded = "decoded_message/";
    MqttAndroidClient client;
    TextView subText;
    TextView decodedMessage;
    TextView plainText;

    Vibrator vibrator;
    Ringtone ringtone;
    JSONObject jObj ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(getParent(),uri);
        subText = (TextView) findViewById(R.id.subText);
        decodedMessage = (TextView) findViewById(R.id.decodedMessage);
        plainText = (TextView) findViewById(R.id.PlainText);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        String clientId = MqttClient.generateClientId();
        client =  new MqttAndroidClient(this.getApplicationContext(),MQTTHOST,clientId);

        //MqttConnectOptions options = new MqttConnectOptions();
        //options.setUserName(USERNAME);
        //options.setPassword(PASSWORD.toCharArray());


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals(topicStr)) {
                    String payload = new String(message.getPayload());
                    jObj = new JSONObject(payload);
                    //subText.setText(payload);
                    JSONObject subObj = jObj.getJSONObject("rxInfo");
                    String str = subObj.getString("rssi");
                    subText.setText(str);

                } else if (topic.equals(topicDecoded)){
                    String payload = new String(message.getPayload());
                    String [] res = payload.split(",");
                    String devAdd = res[0];
                    String rssi = res[1];
                    decodedMessage.setText("0x" + devAdd);
                    plainText.setText(rssi);
            }
                //JSONObject subObj = jObj.getJSONObject("stat");


                vibrator.vibrate(500);
                ringtone.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }
    public void pub(View v){
        String topic = topicDecoded;
        String message = "Hello World!";
        byte[] encodedPayload = new byte[0];
        try {
            client.publish(topic, message.getBytes(),1,false);
        } catch ( MqttException e) {
            e.printStackTrace();
        }
    }
    private  void setSubscription(){
        try{
            client.subscribe(topicStr,0);
            client.subscribe(topicDecoded,0);
        } catch(MqttException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    public void connect (View v){
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    //Log.d(TAG, "onSuccess");
                    Toast.makeText(MainActivity.this,"Conexiune reusita",Toast.LENGTH_LONG).show();
                    setSubscription();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this,"Eroare la conexiune",Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void disconnect(View v){
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    //Log.d(TAG, "onSuccess");
                    Toast.makeText(MainActivity.this, "Conexiune intrerupta", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this,"Eroare la intrerupere conexiune",Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
