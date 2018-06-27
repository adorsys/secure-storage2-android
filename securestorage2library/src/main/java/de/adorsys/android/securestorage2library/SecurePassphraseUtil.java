package de.adorsys.android.securestorage2library;

import android.support.annotation.NonNull;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

final class SecurePassphraseUtil {

    private SecurePassphraseUtil() {
        // prevent instantiation
    }

    @NonNull
    public static String generateRandomString() {
        char[] randomString;

        String lowerCaseChars = "abcdefgijkmnopqrstwxyz";
        String upperCaseChars = "ABCDEFGHJKLMNPQRSTWXYZ";
        String numericChard = "23456789";
        String specialChars = "*$-+?_&=!%{}/§±`~";

        String lowerCaseGroup = "lowercase";
        String upperCaseGroup = "uppercase";
        String numericGroup = "numeric";
        String specialGroup = "special";

        int MIN_LOWER_CASE_COUNT = 10;
        int MIN_UPPER_CASE_COUNT = 10;
        int MIN_NUMBER_COUNT = 10;
        int MIN_SPECIAL_COUNT = 20;

        int MIN_LENGTH = 64;
        int MAX_LENGTH = 128;

        Map<String, Integer> charGroupsUsed = new HashMap<>();
        charGroupsUsed.put(lowerCaseGroup, MIN_LOWER_CASE_COUNT);
        charGroupsUsed.put(upperCaseGroup, MIN_UPPER_CASE_COUNT);
        charGroupsUsed.put(numericGroup, MIN_NUMBER_COUNT);
        charGroupsUsed.put(specialGroup, MIN_SPECIAL_COUNT);

        SecureRandom random = new SecureRandom();

        // Allocate appropriate memory for the password.
        int randomIndex = random.nextInt((MAX_LENGTH - MIN_LENGTH) + 1) + MIN_LENGTH;
        randomString = new char[randomIndex];

        int requiredCharactersLeft = MIN_LOWER_CASE_COUNT + MIN_UPPER_CASE_COUNT + MIN_NUMBER_COUNT + MIN_SPECIAL_COUNT;

        // Build the password.
        for (int i = 0; i < randomString.length; i++) {
            String selectableChars = "";

            // if we still have plenty of characters left to achieve our minimum requirements.
            if (requiredCharactersLeft < randomString.length - i) {
                // choose from any group at random
                selectableChars = lowerCaseChars + upperCaseChars + numericChard + specialChars;
            } else // we are out of wiggle room, choose from a random group that still needs to have a minimum required.
            {
                // choose only from a group that we need to satisfy a minimum for.
                for (Map.Entry<String, Integer> charGroup : charGroupsUsed.entrySet()) {
                    if (charGroup.getValue() > 0) {
                        if (lowerCaseGroup.equals(charGroup.getKey())) {
                            selectableChars += lowerCaseChars;
                        } else if (upperCaseGroup.equals(charGroup.getKey())) {
                            selectableChars += upperCaseChars;
                        } else if (numericGroup.equals(charGroup.getKey())) {
                            selectableChars += numericChard;
                        } else if (specialGroup.equals(charGroup.getKey())) {
                            selectableChars += specialChars;
                        }
                    }
                }
            }

            // Now that the string is built, get the next random character.
            randomIndex = random.nextInt((selectableChars.length()) - 1);
            char nextChar = selectableChars.charAt(randomIndex);

            // Tac it onto our password.
            randomString[i] = nextChar;

            // Now figure out where it came from, and decrement the appropriate minimum value.
            if (lowerCaseChars.indexOf(nextChar) > -1) {
                charGroupsUsed.put(lowerCaseGroup, charGroupsUsed.get(lowerCaseGroup) - 1);
                if (charGroupsUsed.get(lowerCaseGroup) >= 0) {
                    requiredCharactersLeft--;
                }
            } else if (upperCaseChars.indexOf(nextChar) > -1) {
                charGroupsUsed.put(upperCaseGroup, charGroupsUsed.get(upperCaseGroup) - 1);
                if (charGroupsUsed.get(upperCaseGroup) >= 0) {
                    requiredCharactersLeft--;
                }
            } else if (numericChard.indexOf(nextChar) > -1) {
                charGroupsUsed.put(numericGroup, charGroupsUsed.get(numericGroup) - 1);
                if (charGroupsUsed.get(numericGroup) >= 0) {
                    requiredCharactersLeft--;
                }
            } else if (specialChars.indexOf(nextChar) > -1) {
                charGroupsUsed.put(specialGroup, charGroupsUsed.get(specialGroup) - 1);
                if (charGroupsUsed.get(specialGroup) >= 0) {
                    requiredCharactersLeft--;
                }
            }
        }
        return new String(randomString);
    }
}