#!/usr/bin/env bash

echo -e "\033[0;32mStart normal check \033[0m"
./gradlew check
echo -e "\033[0;32m Finished normal check \033[0m"

echo -e "\033[0;32m Start checkstyle check \033[0m"
./gradlew checkstyle
echo -e "\033[0;32m Finished checkstyle check \033[0m"

echo -e "\033[0;32m Start ktlint check \033[0m"
./gradlew ktlint
echo -e "\033[0;32m Finished ktlint check \033[0m"