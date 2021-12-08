package `in`.nakkalites.mediaclient.view.login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class SmsBroadcastReceiver : BroadcastReceiver() {
    var otpReceiveInterface: OtpReceivedInterface? = null

    fun setOnOtpListeners(otpReceiveInterface: OtpReceivedInterface?) {
        this.otpReceiveInterface = otpReceiveInterface
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val mStatus: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?
            when (mStatus?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                    if (otpReceiveInterface != null && message != null) {
                        val pattern = Pattern.compile("\\d{6}")
                        val matcher = pattern.matcher(message)
                        if (matcher.find()) {
                            otpReceiveInterface?.onOtpReceived(matcher.group(0))
                        }
                    } else {
                        otpReceiveInterface?.onOtpTimeout()
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Waiting for SMS timed out (5 minutes)
                    otpReceiveInterface?.onOtpTimeout()
                }
            }
        }
    }

    companion object {
        private const val TAG = "SmsBroadcastReceiver"
    }
}


