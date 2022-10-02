package com.uestechnology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class BeeLister {

    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";
    private final Set<String> dictionaryWordsShort;
    private final Set<String> dictionaryWordsLong;
    private final Set<String> dictionaryWordsExtended;
    private final Set<String> dictionaryWordsMassive;
    private final List<Character> vowels = new ArrayList<>(Arrays.asList('a', 'e', 'i', 'o', 'u', 'y'));
    private boolean debug = false;
    private String letters;


    private BeeLister(String letters) throws IOException {
        this();
        this.letters = letters;
    }

    public BeeLister() {
        letters = null;

        dictionaryWordsShort = new TreeSet<>();
        dictionaryWordsLong = new TreeSet<>();
        dictionaryWordsExtended = new TreeSet<>();
        dictionaryWordsMassive = new TreeSet<>();

        try {
            loadWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }


    private void loadWords() throws IOException {
        InputStream is = getClass().getResourceAsStream("wordlist.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            dictionaryWordsShort.add(line.toLowerCase().trim());
        }
        br.close();

        is = getClass().getResourceAsStream("wiki-100k.txt");

        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            dictionaryWordsLong.add(line.toLowerCase().trim());
        }
        br.close();

        is = getClass().getResourceAsStream("words.txt");
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            dictionaryWordsExtended.add(line.toLowerCase().trim());
        }
        br.close();

        // Add second big list of words to the same large dictionary
        is = getClass().getResourceAsStream("words400k.txt");
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            dictionaryWordsExtended.add(line.toLowerCase().trim());
        }
        br.close();

        // Add third big list of words to the massive large dictionary
        is = getClass().getResourceAsStream("wlist_match1.txt");
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            dictionaryWordsMassive.add(line.toLowerCase().trim());
        }
        br.close();


    }

    public static void main(String[] arg) {
        Scanner scanner = new Scanner(System.in);
        BeeLister beeLister;

        beeLister = new BeeLister();

        while (true) {
            System.out.print(ANSI_RESET + "\n\nEnter letters ('q' to quit): ");

            // get their input as a String
            String letters = scanner.next();
            if (letters.equalsIgnoreCase("q")) {
                break;
            }

            char command = 's';

            if (letters.length() > 2) {
                beeLister.setLetters(letters.trim().toLowerCase());
            } else if (letters.length() == 1) {
                command = letters.charAt(0);
            }

            if (arg.length > 0) {
                beeLister.setDebug();
            }

            final Set<String> commonCombos = beeLister.findCombos(SetType.SHORT);
            final Set<String> moreCombos = beeLister.findCombos(SetType.MEDIUM);
            final Set<String> allCombos = beeLister.findCombos(SetType.LONG);
            final Set<String> massiveCombos = beeLister.findCombos(SetType.MASSIVE);


            if (command == 's') {
                System.out.println(ANSI_RESET + "\nCommon list:\n");
                BeeLister.displayShortWords(commonCombos);
            }

            if (command == 'm') {
                System.out.println(ANSI_RESET + "\n\nMedium list:");
                BeeLister.displayMediumWords(commonCombos, moreCombos);
            }

            if (command == 'e') {
                System.out.println(ANSI_RESET + "\n\nExtended list:");
                BeeLister.displayLongWords(commonCombos, moreCombos, allCombos);
            }

            if (command == 'w') {
                System.out.println(ANSI_RESET + "\n\nMassive list:");
                BeeLister.displayMassiveWords(commonCombos, moreCombos, allCombos, massiveCombos);
            }
        }
    }

    private Set<String> findCombos(SetType type) {

        // Ignore type for now (default to short [list])

        Set<String> foundWordSet = new TreeSet<>();

        // last letter entered is the "must have" letter
        char mustBePresent = letters.charAt(letters.length() - 1);

        Set<String> currentWordSet = null;
        switch (type) {
            case SHORT:
                currentWordSet = dictionaryWordsShort;
                break;
            case MEDIUM:
                currentWordSet = dictionaryWordsLong;
                break;
            case LONG:
                currentWordSet = dictionaryWordsExtended;
                break;
            case MASSIVE:
                currentWordSet = dictionaryWordsMassive;
        }

        // Word elimination logic
        for (String word : currentWordSet) {
            boolean badword = false;

            // Fewer than four letters? out.
            if (word.length() < 4)
                continue;

            // Current letter not in the accepted list? out.
            for (int i = 0; i < word.length(); i++) {
                if (letters.indexOf(word.charAt(i)) == -1) {
                    badword = true;
                    break;
                }
            }
            if (badword)
                continue;

            // All letters valid but the required letter is missing? out.
            if (word.indexOf(mustBePresent) == -1)
                continue;

            // Word doesn't contain at least one vowel? out. (sanity check)
            boolean foundVowel = false;
            for (int i = 0; i < word.length(); i++){
                char letter = word.charAt(i);
                if (vowels.contains(letter)) {
                    foundVowel = true;
                    break;
                }
            }
            if (!foundVowel)
                continue;

            // If we've gotten to here then the word is >= 4 chars, doesn't contain unpermitted letters, and has the required letter
            foundWordSet.add(word);
        }

        return foundWordSet;
    }


    // Display words from set one with set two words colorized
    // REFACTOR THIS CRAP to take two lists, pass in the color of one in the other. Such. Crap.
    private static void displayWords(Set<String> shortWords, Set<String> medWords, Set<String> longWords, Set<String> massiveWords, SetType type) {
        int columnCount = 0;
        Set<String> curList;
        switch (type) {
            case SHORT:
                curList = shortWords;
                break;
            case MEDIUM:
                curList = medWords;
                break;
            case LONG:
                curList = longWords;
                break;
            default:
                curList = massiveWords;
        }

        for (String word : curList) {
            if (shortWords.contains(word) && type == SetType.MEDIUM) {
                System.out.print(ANSI_RESET + ANSI_GREEN);
            } else if (shortWords.contains(word) && type == SetType.LONG) {
                System.out.print(ANSI_RESET + ANSI_RED);
            } else if (longWords.contains(word) && type == SetType.MASSIVE) {
                System.out.print(ANSI_RESET + ANSI_BLUE);
            } else {
                System.out.print(ANSI_RESET);
            }
            System.out.print(word + "\t");
            if (++columnCount == 4) {
                System.out.println();
                columnCount = 0;
            }
        }
    }


    private static void displayShortWords(Set<String> words) {
        displayWords(words, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), SetType.SHORT);
    }

    private static void displayMediumWords(Set<String> shortWords, Set<String> medWords) {
        displayWords(shortWords, medWords, Collections.emptySet(), Collections.emptySet(), SetType.MEDIUM);
    }

    private static void displayLongWords
            (Set<String> shortWords, Set<String> medWords, Set<String> longWords) {
        displayWords(shortWords, medWords, longWords, Collections.emptySet(), SetType.LONG);
    }

    private static void displayMassiveWords
            (Set<String> shortWords, Set<String> medWords, Set<String> longWords, Set<String> massiveWords) {
        displayWords(shortWords, medWords, longWords, massiveWords, SetType.MASSIVE);
    }


    private void setDebug() {
        this.debug = true;
    }
}
