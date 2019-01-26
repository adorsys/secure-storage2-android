package de.adorsys.android.securestorage2library.internal;

import androidx.annotation.NonNull;

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

        int minLowerCaseCount = 10;
        int minUpperCaseCount = 10;
        int minNumberCount = 10;
        int minSpecialCount = 20;

        int minLength = 64;
        int maxLength = 128;

        Map<String, Integer> charGroupsUsed = new HashMap<>();
        charGroupsUsed.put(lowerCaseGroup, minLowerCaseCount);
        charGroupsUsed.put(upperCaseGroup, minUpperCaseCount);
        charGroupsUsed.put(numericGroup, minNumberCount);
        charGroupsUsed.put(specialGroup, minSpecialCount);

        SecureRandom random = new SecureRandom();

        // Allocate appropriate memory for the password.
        int randomIndex = random.nextInt((maxLength - minLength) + 1) + minLength;
        randomString = new char[randomIndex];

        int requiredCharactersLeft = minLowerCaseCount + minUpperCaseCount + minNumberCount + minSpecialCount;

        // Build the password.
        for (int i = 0; i < randomString.length; i++) {
            String selectableChars = "";

            // if we still have plenty of characters left to achieve our minimum requirements.
            if (requiredCharactersLeft < randomString.length - i) {
                // choose from any group at random
                selectableChars = lowerCaseChars + upperCaseChars + numericChard + specialChars;
            } else {
                // we are out of wiggle room, choose from a random group that still needs to have a minimum required.
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
            randomIndex = random.nextInt(selectableChars.length() - 1);
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