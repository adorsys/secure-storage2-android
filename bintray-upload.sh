#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" == "master" ]; then
    echo -e "\033[0;32m Start clean module \033[0m"
    ./gradlew :securestorage2library:clean
    echo -e "Finished clean \033[0m"

    echo -e "\033[0;32m Start install module \033[0m"
    ./gradlew :securestorage2library:install
    echo -e "\033[0;32m Finished install \033[0m"

    echo -e "\033[0;32m Start bintrayUpload \033[0m"
    ./gradlew :securestorage2library:bintrayUpload
    echo -e "\033[0;32m Finished bintrayUpload \033[0m"
else
   echo -e "\033[0;32m Current branch is not master, will not upload to bintray. \033[0m"
fi