package `in`.nakkalites.mediaclient.viewmodel.utils

import `in`.nakkalites.mediaclient.R
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException


sealed class PhoneAuthException(val resId: Int, errorMessage: String?, val errorBody: String?) :
    RuntimeException(errorMessage) {
    class RequestLimitExceededException(errorMessage: String?, errorBody: String? = null) :
        PhoneAuthException(R.string.phone_error_quota_exceeded, errorMessage, errorBody)
    class ResendLimitExceededException(errorMessage: String?, errorBody: String? = null) :
        PhoneAuthException(R.string.phone_too_many_attempts, errorMessage, errorBody)
    class InvalidPhoneNumberException(errorMessage: String?, errorBody: String? = null) :
        PhoneAuthException(R.string.enter_valid_mobile_number, errorMessage, errorBody)
    class OtpExpiredException(errorMessage: String?, errorBody: String? = null) :
        PhoneAuthException(R.string.otp_error_code_expired, errorMessage, errorBody)
    class InvalidOtpException(errorMessage: String?, errorBody: String? = null) :
        PhoneAuthException(R.string.enter_the_correct_otp, errorMessage, errorBody)
    class UnknownException(errorMessage: String?, errorBody: String? = null) :
        PhoneAuthException(R.string.generic_error_message, errorMessage, errorBody)

    companion object {

        fun mapFirebaseException(e: Throwable?): PhoneAuthException = when(e) {
            is FirebaseAuthException -> when (e.errorCode) {
                FirebaseAuthError.ERROR_TOO_MANY_REQUESTS.name ->
                    ResendLimitExceededException(e.errorCode)
                FirebaseAuthError.ERROR_INVALID_PHONE_NUMBER.name ->
                    InvalidPhoneNumberException(e.errorCode)
                FirebaseAuthError.ERROR_INVALID_VERIFICATION_CODE.name ->
                    InvalidOtpException(e.errorCode)
                FirebaseAuthError.ERROR_SESSION_EXPIRED.name ->
                    OtpExpiredException(e.errorCode)
                FirebaseAuthError.ERROR_QUOTA_EXCEEDED.name ->
                    RequestLimitExceededException(e.message)
                else -> UnknownException(e.errorCode)
            }
            is FirebaseTooManyRequestsException -> ResendLimitExceededException(e.message)
            else -> UnknownException(null)
        }
    }
}
