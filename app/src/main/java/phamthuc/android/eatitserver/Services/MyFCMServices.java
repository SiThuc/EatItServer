package phamthuc.android.eatitserver.Services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import phamthuc.android.eatitserver.Common.Common;

public class MyFCMServices extends FirebaseMessagingService {
    //Copy code from Client app
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Map<String, String> dataRecv = remoteMessage.getData();
        if(dataRecv != null){
            Common.showNotification(this, new Random().nextInt(),
                    dataRecv.get(Common.NOTI_TITLE),
                    dataRecv.get(Common.NOTI_CONTENT),
                    null);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken( s );
        Common.updateToken(this, s);
    }
}