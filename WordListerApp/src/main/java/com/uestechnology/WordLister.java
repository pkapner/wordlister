package com.uestechnology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class WordLister {

    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";
    private final Set<String> dictionaryWordsShort;
    private final Set<String> dictionaryWordsLong;
    private final Set<String> dictionaryWordsExtended;
    private boolean debug = false;
    private String letters;


    private WordLister(String letters) throws IOException {
        this();
        this.letters = letters;
    }

    public WordLister() {
        letters = null;

        dictionaryWordsShort = new TreeSet<>();
        dictionaryWordsLong = new TreeSet<>();
        dictionaryWordsExtended = new TreeSet<>();
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
    }


    public static void main(String[] arg) {
        Scanner scanner = new Scanner(System.in);
        WordLister wordLister;

        wordLister = new WordLister();

        while (true) {
            System.out.print(ANSI_RESET + "\n\nEnter letters ('q' to quit): ");

            // get their input as a String
            String letters = scanner.next();
            if (letters.equalsIgnoreCase("q")) {
                break;
            }

            char command = 's';

            if (letters.length() > 2) {
                wordLister.setLetters(letters.trim().toLowerCase());
            } else if (letters.length() == 1) {
                command = letters.charAt(0);
            }

            if (arg.length > 0) {
                wordLister.setDebug();
            }

            final Set<String> commonCombos = wordLister.findCombos(SetType.SHORT);
            final Set<String> moreCombos = wordLister.findCombos(SetType.MEDIUM);
            final Set<String> allCombos = wordLister.findCombos(SetType.LONG);


            if (command == 's') {
                System.out.println(ANSI_RESET + "\nCommon list:\n");
                WordLister.displayShortWords(commonCombos);
            }

            if (command == 'm') {
                System.out.println(ANSI_RESET + "\n\nMedium list:");
                WordLister.displayMediumWords(commonCombos, moreCombos);
            }

            if (command == 'e') {
                System.out.println(ANSI_RESET + "\n\nExtended list:");
                WordLister.displayLongWords(commonCombos, moreCombos, allCombos);
            }
        }
    }

    public Set<String> findCombos(SetType type) {

        Set<String> foundWordSet = new TreeSet<>();

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
        }

        for (String word : currentWordSet) {
            boolean badword = false;
            String localLetters = letters;

            if (word.length() < 3)
                continue;
            for (int i = 0; i < word.length(); i++) {
                if (localLetters.indexOf(word.charAt(i)) == -1) {
                    badword = true;
                    break;
                }

                // Remove found letter from our localLetters String
                localLetters = localLetters.replaceFirst(String.valueOf(word.charAt(i)), "");
            }
            if (badword)
                continue;

            // If we've gotten to here then the word is >= 4 chars and doesn't contain unpermitted letters
            foundWordSet.add(word);
        }

        return foundWordSet;
    }


    // Display words from set one with set two words colorized
    // REFACTOR THIS CRAP to take two lists, pass in the color of one in the other. Such. Crap.
    private static void displayWords(Set<String> shortWords, Set<String> medWords, Set<String> longWords, SetType type) {
        int columnCount = 0;
        Set<String> curList;
        switch (type) {
            case SHORT:
                curList = shortWords;
                break;
            case MEDIUM:
                curList = medWords;
                break;
            default:
                curList = longWords;
        }
        List<String> printableList = new ArrayList<>(curList);
        printableList.sort(new LengthFirstComparator());

        for (String word : printableList) {
            if (shortWords.contains(word) && type == SetType.MEDIUM) {
                System.out.print(ANSI_RESET + ANSI_GREEN);
            } else if (shortWords.contains(word) && type == SetType.LONG) {
                System.out.print(ANSI_RESET + ANSI_RED);
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
        displayWords(words, Collections.emptySet(), Collections.emptySet(), SetType.SHORT);
    }

    private static void displayMediumWords(Set<String> shortWords, Set<String> medWords) {
        displayWords(shortWords, medWords, Collections.emptySet(), SetType.MEDIUM);
    }

    private static void displayLongWords
            (Set<String> shortWords, Set<String> medWords, Set<String> longWords) {
        displayWords(shortWords, medWords, longWords, SetType.LONG);
    }

    public static class LengthFirstComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1.length() != o2.length()) {
                return o1.length() - o2.length(); //overflow impossible since lengths are non-negative
            }
            return o1.compareTo(o2);
        }
    }

    private void setDebug() {
        this.debug = true;
    }
}
