package de.adorsys.android.securestorage2.internal

internal object KeyStoreTool {

    internal fun keyExists(): Boolean {
        //TODO Check if key exists in KeyStore
        return true
    }

    internal fun generateKey() {
        //TODO Generate key in KeyStore
    }

    internal fun encryptValue(value: String): String {
        //TODO Encrypt and return encrypted value
        return "PLACEHOLDER"
    }

    internal fun decryptValue(value: String?): String? {
        //TODO Decrypt and return decrypted value
        return "PLACEHOLDER"
    }

    internal fun deleteKey() {
        //TODO Delete key
    }

}