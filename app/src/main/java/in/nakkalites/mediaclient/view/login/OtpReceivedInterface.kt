package `in`.nakkalites.mediaclient.view.login

interface OtpReceivedInterface {
    fun onOtpReceived(otp: String?)
    fun onOtpTimeout()
}
