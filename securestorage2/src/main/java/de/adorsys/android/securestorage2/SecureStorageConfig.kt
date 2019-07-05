/*
 * Copyright (C) 2019 adorsys GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.android.securestorage2

import android.content.Context
import de.adorsys.android.securestorage2.internal.KeyStoreTool

@Suppress("PropertyName")
enum class SecureStorageConfig {
    INSTANCE;

    internal lateinit var SHARED_PREFERENCES_NAME: String
    internal lateinit var ENCRYPTION_KEY_ALIAS: String
    internal lateinit var X500PRINCIPAL: String
    internal var EXPLICITLY_USE_SECURE_HARDWARE = false
    internal var CAN_USE_LIBRARY = true
    internal var ASYNC_OPERATION = true

    fun initializeSecureStorageConfig(
        context: Context,
        encryptionKeyAlias: String? = null,
        x500Principal: String? = null,
        useOnlyWithHardwareSupport: Boolean = false,
        workWithDataAsynchronously: Boolean = true
    ) {
        SHARED_PREFERENCES_NAME = context.packageName + ".SecureStorage2"
        ENCRYPTION_KEY_ALIAS = encryptionKeyAlias ?: "SecureStorage2Key"
        X500PRINCIPAL = x500Principal ?: "CN=App Name, O=Organization Name, C=Country"
        ASYNC_OPERATION = workWithDataAsynchronously
        EXPLICITLY_USE_SECURE_HARDWARE = useOnlyWithHardwareSupport

        CAN_USE_LIBRARY = if (useOnlyWithHardwareSupport) {
            KeyStoreTool.deviceHasSecureHardwareSupport(context)
        } else {
            true
        }
    }
}