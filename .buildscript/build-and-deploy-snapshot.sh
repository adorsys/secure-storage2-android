#!/usr/bin/env bash

echo -e "\033[0;32m Start clean \033[0m"
./gradlew :securestorage2:clean

echo -e "\033[0;32m Start install \033[0m"
./gradlew :securestorage2:install

if [ "$TRAVIS_BRANCH" == "master" ]; then

    echo -e "\033[0;32m Start bintrayUpload \033[0m"
    ./gradlew :securestorage2:bintrayUpload

else
   echo -e "\033[0;32m Current branch is not master, will not upload to bintray. \033[0m"
fi