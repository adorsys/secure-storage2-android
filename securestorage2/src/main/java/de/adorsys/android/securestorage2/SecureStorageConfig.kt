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

// this is how a x500principal must look:
//    "CN=App Name, O=Company Name, C=Country"

enum class SecureStorageConfig {
    INSTANCE;

    internal lateinit var SHARED_PREFERENCES_NAME: String
    internal lateinit var ENCRYPTION_KEY_ALIAS: String
    internal lateinit var X500PRINCIPAL: String
    internal var CAN_USE_LIBRARY = false
    internal var ASYNC_OPERATION = false

    fun initializeSecureStorageConfig(
        context: Context,
        encryptionKeyAlias: String,
        x500Principal: String,
        useOnlyWithHardwareSupport: Boolean,
        workWithDataAsynchronously: Boolean
    ) {
        SHARED_PREFERENCES_NAME = context.packageName + ".SecureStorage2"
        ENCRYPTION_KEY_ALIAS = encryptionKeyAlias
        X500PRINCIPAL = x500Principal
        ASYNC_OPERATION = workWithDataAsynchronously

        CAN_USE_LIBRARY = if (useOnlyWithHardwareSupport) {
            KeyStoreTool.deviceHasSecureHardwareSupport(context)
        } else {
            true
        }
    }
}