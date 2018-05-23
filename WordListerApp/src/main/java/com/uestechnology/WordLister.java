package com.uestechnology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WordLister {

    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";
    private Set<String> validWordsShort;
    private Set<String> validWordsMedium;
    private Set<String> validWordsLong;
    private boolean debug = false;
    private String letters;
    private Map<Integer, Set<Integer>> cachedCombos = new ConcurrentHashMap<>();
    private final ExecutorService prePopulateExecutorService;

    private WordLister(String letters) throws IOException {
        this();
        this.letters = letters;
    }

    public WordLister() {
        letters = null;
        prePopulateExecutorService = Executors.newFixedThreadPool(8);
        populateCache();
        validWordsShort = new TreeSet<>();
        validWordsMedium = new TreeSet<>();
        validWordsLong = new TreeSet<>();
        try {
            loadWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }

    public static Set<String> getWords(String letters, SetType setType) throws IOException, InterruptedException {
        WordLister wl = new WordLister(letters.toUpperCase());
        return wl.findCombos(setType);
    }

    private void loadWords() throws IOException {
        InputStream is = getClass().getResourceAsStream("wordlist.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            validWordsShort.add(line);
        }
        br.close();

        is = getClass().getResourceAsStream("wiki-100k.txt");
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            validWordsMedium.add(line);
        }
        br.close();

        is = getClass().getResourceAsStream("words.txt");
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            validWordsLong.add(line);
        }
        br.close();
    }

    private void populateCache() {

        prePopulateExecutorService.execute(() -> {
            allCombos(6);
            allCombos(7);
            allCombos(8);
        });

        prePopulateExecutorService.shutdown();
    }

    public static void main(String[] arg) {
        Scanner scanner = new Scanner(System.in);
        WordLister wordLister;

        try {
            wordLister = new WordLister();

            while (true) {
                System.out.print(ANSI_RESET + "\n\nEnter scrambled letters ('q' to quit): ");

                // get their input as a String
                String letters = scanner.next();
                if (letters.equalsIgnoreCase("q")) {
                    wordLister.shutdown();
                    break;
                }

                Character command = 's';

                if (letters.length() > 2) {
                    wordLister.setLetters(letters);
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        if (!prePopulateExecutorService.isShutdown())
            prePopulateExecutorService.shutdownNow();
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

        for (String word : curList) {
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

    public Set<String> findCombos(SetType setType) throws InterruptedException {
        Set<String> allCombos = new LinkedHashSet<>();
        for (int i = 3; i <= letters.length(); i++) {
            allCombos.addAll(findAllCombos(i, setType));
        }
        return allCombos;
    }


    private Set<String> findAllCombos(int size, SetType setType) throws InterruptedException {
        Set<String> words = new ConcurrentSkipListSet<>();
        Set<Integer> allNumericCombos = allCombos();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        for (Integer source : allNumericCombos) {

            executorService.execute(() -> {
                String str = source.toString();
                char[] word = new char[str.length()];
                for (int i = 0; i < str.length(); i++) {
                    word[i] = letters.charAt((str.charAt(i) - 49));
                }
                String addWord = String.valueOf(word).substring(letters.length() - size, letters.length());
                if (debug) System.out.println("Looking up: " + addWord);
                if (setType == SetType.SHORT && validWordsShort.contains(addWord.toLowerCase())) {
                    words.add(addWord);
                }
                if (setType == SetType.MEDIUM && validWordsMedium.contains(addWord.toLowerCase())) {
                    words.add(addWord);
                }
                if (setType == SetType.LONG && validWordsLong.contains(addWord.toLowerCase())) {
                    words.add(addWord);
                }
            });

        }
        executorService.shutdown();
        executorService.awaitTermination(10000, TimeUnit.SECONDS);

        return words;
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


    private static int getMax(int length) {
        int max = 0;

        while (length-- > 0) {
            int multiplier = 1;
            for (int j = 0; j < length; j++) {
                multiplier *= 10;
            }
            max += (multiplier * (length + 1));
        }
        return max;
    }

    static int getMax(String letters) {
        if (letters == null) {
            throw new IllegalArgumentException("Letters must not be null");
        }
        return getMax(letters.length());
    }

    static int getMin(String letters) {
        if (letters == null) {
            throw new IllegalArgumentException("Letters must not be null");
        }

        return getMin(letters.length());
    }

    private static int getMin(int length) {
        int min = 0;
        for (int i = 1; i <= length; i++) {
            min += Math.pow(10, length - i) * i;
        }
        return min;
    }


    private Set<Integer> allCombos(int len) {
        Set<Integer> combosSet = cachedCombos.get(len);
        if (combosSet != null) {
            return combosSet;
        }

        int min = getMin(len);
        int max = getMax(len);
        combosSet = new ConcurrentSkipListSet<>();

        for (int i = min; i <= max; i++) {
            if (checkValid(i, max)) {
                combosSet.add(i);
            }
        }

        cachedCombos.put(len, combosSet);

        return combosSet;
    }

    private Set<Integer> allCombos() {
        Set<Integer> combosSet = cachedCombos.get(letters.length());
        if (combosSet != null) {
            return combosSet;
        }

        return allCombos(letters.length());
    }

    /**
     * Check that this is a valid number within the set of numbers
     * <p/>
     * i.e. 1234 == valid
     * 1224 == invalid
     * 3421 == valid
     * 1235 == invalid
     */
    static boolean checkValid(Integer number, Integer reference) {
        char[] individualLetters = number.toString().toCharArray();
        char[] referenceIndividualLetters = reference.toString().toCharArray();

        // Prevent duplicate numbers (e.g. 1223)
        for (int i = 0; i < individualLetters.length; i++) {
            char iter = individualLetters[i];
            for (int j = 0; j < individualLetters.length; j++) {
                if (j != i && individualLetters[j] == iter) {
                    return false;
                }
            }
        }

        // Prevent out of range numbers (e.g. base is 1234 and parameter is 1235)
        boolean valid = true;
        for (char iter : individualLetters) {
            for (int j = 0; j < referenceIndividualLetters.length; j++) {
                if (referenceIndividualLetters[j] == iter) {
                    break;
                }
                if (j == referenceIndividualLetters.length - 1) {
                    valid = false;
                }
            }

        }
        return valid;
    }

    private void setDebug() {
        this.debug = true;
    }
}
