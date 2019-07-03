package de.adorsys.android.securestorage2.internal

internal class SecureStorageException(detailMessage: String, cause: Throwable?, val type: ExceptionType) :
    Exception(detailMessage, cause) {

    enum class ExceptionType {
        /**
         * If this exception type is defined you cannot use the keystore / this library on the current device.
         * This is fatal and most likely due to native key store issues.
         */
        KEYSTORE_EXCEPTION,
        /**
         * If this exception type is defined a problem during encryption has occurred.
         * Most likely this is due to using an invalid key for encryption or decryption.
         */
        CRYPTO_EXCEPTION,
        /**
         * If this exception type is set it means simply that the keystore cannot be used on the current device as it is not supported by this library.
         */
        KEYSTORE_NOT_SUPPORTED_EXCEPTION,
        /**
         * If this exception type is set it means that something with this library is wrong.
         */
        INTERNAL_LIBRARY_EXCEPTION
    }
}