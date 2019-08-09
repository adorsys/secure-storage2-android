#!/bin/bash

set +e

./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.adorsys.android.securestorage2sampleapp.SecureStorage2LogicTest --stacktrace