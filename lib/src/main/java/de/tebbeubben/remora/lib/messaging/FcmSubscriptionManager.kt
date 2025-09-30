package de.tebbeubben.remora.lib.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import de.tebbeubben.remora.lib.FirebaseAppProvider
import de.tebbeubben.remora.lib.lifecycle.LibraryLifecycleCallback
import de.tebbeubben.remora.lib.persistence.repositories.PeerDeviceRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FcmSubscriptionManager @Inject constructor(
    private val peerDeviceRepository: PeerDeviceRepository,
    private val firebaseAppProvider: FirebaseAppProvider
) : LibraryLifecycleCallback {

    private val fcm get() = firebaseAppProvider.firebaseApp?.get(FirebaseMessaging::class.java)!!

    suspend fun subscribeToPairingTopic(topic: String) {
        fcm.subscribeToTopic(PAIRING_TOPIC_PREFIX + topic).await()
    }

    suspend fun subscribeToPeerTopic(topic: String) {
        fcm.subscribeToTopic(PEER_TOPIC_PREFIX + topic).await()
    }

    fun unsubscribeFromPairingTopic(topic: String) {
        fcm.unsubscribeFromTopic(PAIRING_TOPIC_PREFIX + topic)
    }

    fun unsubscribeFromPeerTopic(topic: String) {
        fcm.unsubscribeFromTopic(PEER_TOPIC_PREFIX + topic)
    }

    override suspend fun onInitFirebase() {
        // We don't want peer devices to be able to trigger malicious push notifications.
        // To ensure this behavior, we first need to disable notification delegation,
        // so that GMS doesn't show push notifications on behalf of our app.
        // We then override FirebaseMessagingService::handleIntent and disarm any display notification payload.
        fcm.isNotificationDelegationEnabled = false
        peerDeviceRepository.getAllSubscribeTopics().forEach {
            fcm.subscribeToTopic(it)
        }
    }

    companion object {

        const val PAIRING_TOPIC_PREFIX = "pairing_"
        const val PEER_TOPIC_PREFIX = "peer_"
    }
}