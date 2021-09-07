package `in`.nakkalites.mediaclient.app.services

import androidx.annotation.NonNull
import com.freshchat.consumer.sdk.Freshchat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(fcmToken: String) {
        Freshchat.getInstance(this).setPushRegistrationToken(fcmToken)
    }

    override fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        if (Freshchat.isFreshchatNotification(remoteMessage)) {
            Freshchat.handleFcmMessage(this, remoteMessage)
        } else {
            //Handle notifications for app
        }
    }
}
