#!/bin/bash

set +e

echo -e "\033[0;32m ./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.adorsys.android.securestorage2sampleapp.SecureStorage2LogicTest --stacktrace \033[0m"
./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.adorsys.android.securestorage2sampleapp.SecureStorage2LogicTest --stacktrace