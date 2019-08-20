#!/usr/bin/env bash

echo -e "\033[0;32m ./gradlew :securestorage2:clean \033[0m"
./gradlew :securestorage2:clean

echo -e "\033[0;32m ./gradlew :securestorage2:install \033[0m"
./gradlew :securestorage2:install

if [ "$CI" == true ] && [ "$TRAVIS_PULL_REQUEST" == false ] && [ "$TRAVIS_BRANCH" == "master" ]; then

    echo -e "\033[0;32m ./gradlew :securestorage2:bintrayUpload \033[0m"
    ./gradlew :securestorage2:bintrayUpload

else
   echo -e "\033[0;32m Current branch is not master, will not upload to bintray. \033[0m"
fi