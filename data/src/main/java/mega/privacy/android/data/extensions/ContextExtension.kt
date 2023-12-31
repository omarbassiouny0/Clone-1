package mega.privacy.android.data.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Size
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

/**
 * expose Broadcast Receivers as Flow
 */
internal fun Context.registerReceiverAsFlow(
    flags: Int,
    vararg intentFilterActions: String,
) = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySendBlocking(intent)
        }
    }
    val intentFilter = IntentFilter()
    intentFilterActions.forEach {
        Timber.d("Broadcast Receiver registered for $it")
        intentFilter.addAction(it)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.registerReceiver(
            this@registerReceiverAsFlow,
            receiver,
            intentFilter,
            flags
        )
    } else {
        registerReceiver(receiver, intentFilter)
    }
    awaitClose {
        unregisterReceiver(receiver)
    }
}.buffer(capacity = Channel.UNLIMITED)


/**
 * Get the Screen size
 */
fun Context.getScreenSize() =
    Size(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
