package com.uestechnology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class ConnectionLister {


    private static final String ANSI_RESET = "\u001B[0m";
    private final Set<String> dictionaryWordsShort;
    private final Set<String> dictionaryWordsLong;
    private final Set<String> dictionaryWordsExtended;
    private final Set<String> dictionaryWordsMassive;
    private final List<Character> vowels = new ArrayList<>(Arrays.asList('a', 'e', 'i', 'o', 'u', 'y'));
    private boolean debug = false;
    private String letters;
    private List<Character> groupOne;
    private List<Character> groupTwo;
    private List<Character> groupThree;
    private List<Character> groupFour;


    private ConnectionLister(String letters) throws IOException {
        this();
        this.letters = letters;
    }

    public ConnectionLister() {
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
        char[] array = letters.toCharArray();
        this.groupOne = new ArrayList<>(Arrays.asList(array[0], array[1], array[2]));
        this.groupTwo = new ArrayList<>(Arrays.asList(array[3], array[4], array[5]));
        this.groupThree = new ArrayList<>(Arrays.asList(array[6], array[7], array[8]));
        this.groupFour = new ArrayList<>(Arrays.asList(array[9], array[10], array[11]));
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
        ConnectionLister connectionLister;

        connectionLister = new ConnectionLister();

        while (true) {
            System.out.print(ANSI_RESET + "\n\nEnter letters ('q' to quit): ");

            // get their input as a String
            String letters = scanner.next();
            if (letters.equalsIgnoreCase("q")) {
                break;
            }

            char command = 's';

            if (letters.length() == 1) {
                command = letters.charAt(0);
            } else if (letters.length() != 12) {
                System.out.println("There must be twelve letters input");
                System.exit(1);
            } else {
                connectionLister.setLetters(letters.trim().toLowerCase());
            }

            if (arg.length > 0) {
                connectionLister.setDebug();
            }


            if (command == 's') {
                System.out.println(ANSI_RESET + "\nCommon list:\n");
                Map<Integer, Set<String>> countMap = connectionLister.findCombos(SetType.SHORT);
                ConnectionLister.displayWords(countMap);
            }

            if (command == 'm') {
                System.out.println(ANSI_RESET + "\n\nMedium list:");
                Map<Integer, Set<String>> countMap = connectionLister.findCombos(SetType.MEDIUM);
                ConnectionLister.displayWords(countMap);
            }

            if (command == 'e') {
                System.out.println(ANSI_RESET + "\n\nExtended list:");
                Map<Integer, Set<String>> countMap = connectionLister.findCombos(SetType.LONG);
                ConnectionLister.displayWords(countMap);
            }

            if (command == 'w') {
                System.out.println(ANSI_RESET + "\n\nMassive list:");
                Map<Integer, Set<String>> countMap = connectionLister.findCombos(SetType.MASSIVE);
                ConnectionLister.displayWords(countMap);
            }

        }
    }


    private Map<Integer, Set<String>> findCombos(SetType type) {

        // Ignore type for now (default to short [list])

        Set<String> foundWordSet = new TreeSet<>();
        Map<Integer, Set<String>> countMap = new HashMap<>(); // word sets by number of unique found letters

        // last letter entered is the "must have" letter
//        char mustBePresent = letters.charAt(letters.length() - 1);

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

            // Fewer than three letters? out.
            if (word.length() < 3)
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


            // Word doesn't contain at least one vowel? out. (sanity check)
            boolean foundVowel = false;
            for (int i = 0; i < word.length(); i++) {
                char letter = word.charAt(i);
                if (vowels.contains(letter)) {
                    foundVowel = true;
                    break;
                }
            }
            if (!foundVowel)
                continue;

            // If we've gotten to here then the word is >= 3 chars and doesn't contain unpermitted letters
            int curGrp = 0;
            Map<Character, Integer> uniqueCount = new HashMap<>(); // We don't need an actual count of each letter, just that it exists, but whatever
            for (int i = 0; i < word.length(); i++) {
                char letter = word.charAt(i);
                if (groupOne.contains(letter) && curGrp != 1) {
                    curGrp = 1;
                    uniqueCount.merge(letter, 1, Integer::sum);
                    continue;
                }
                if (groupTwo.contains(letter) && curGrp != 2) {
                    curGrp = 2;
                    uniqueCount.merge(letter, 1, Integer::sum);
                    continue;
                }
                if (groupThree.contains(letter) && curGrp != 3) {
                    curGrp = 3;
                    uniqueCount.merge(letter, 1, Integer::sum);
                    continue;
                }
                if (groupFour.contains(letter) && curGrp != 4) {
                    curGrp = 4;
                    uniqueCount.merge(letter, 1, Integer::sum);
                    continue;
                }

                // If we've made it here then the letter either isn't in a group or is in the same group as the last letter. Disqualify
                curGrp = 5;
                break;
            }

            if (curGrp != 5) {
                int numUniqueLetters = uniqueCount.keySet().size();
                Set<String> wordSetIndexed = countMap.computeIfAbsent(numUniqueLetters, k -> new TreeSet<>());
                wordSetIndexed.add(word);
            }
        }
        return countMap;
    }


    // Display words from set one with set two words colorized
    // REFACTOR THIS CRAP to take two lists, pass in the color of one in the other. Such. Crap.
    private static void displayWords(Map<Integer, Set<String>> countMap) {
        int columnCount = 0;
        Set<String> wordSet = new LinkedHashSet<>();

        for (int count: countMap.keySet()){
            wordSet.addAll(countMap.get(count));
        }
        for (String word : wordSet) {

            System.out.print(word + "\t");
            if (++columnCount == 4) {
                System.out.println();
                columnCount = 0;
            }
        }
    }



    private void setDebug() {
        this.debug = true;
    }
}
