[securestorage2](../../../index.md) / [de.adorsys.android.securestorage2](../../index.md) / [SecureStorageException](../index.md) / [ExceptionType](./index.md)

# ExceptionType

`enum class ExceptionType`

### Enum Values

| Name | Summary |
|---|---|
| [KEYSTORE_EXCEPTION](-k-e-y-s-t-o-r-e_-e-x-c-e-p-t-i-o-n.md) | If this exception type is defined you cannot use the keystore / this library on the current device. This is fatal and most likely due to native key store issues. |
| [CRYPTO_EXCEPTION](-c-r-y-p-t-o_-e-x-c-e-p-t-i-o-n.md) | If this exception type is defined a problem during encryption has occurred. Most likely this is due to using an invalid key for encryption or decryption. |
| [KEYSTORE_NOT_SUPPORTED_EXCEPTION](-k-e-y-s-t-o-r-e_-n-o-t_-s-u-p-p-o-r-t-e-d_-e-x-c-e-p-t-i-o-n.md) | If this exception type is set it means simply that the keystore cannot be used on the current device as it is not supported by this library. |
| [INTERNAL_LIBRARY_EXCEPTION](-i-n-t-e-r-n-a-l_-l-i-b-r-a-r-y_-e-x-c-e-p-t-i-o-n.md) | If this exception type is set it means that something with this library is wrong. |
