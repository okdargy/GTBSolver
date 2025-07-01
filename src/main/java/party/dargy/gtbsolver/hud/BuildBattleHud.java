package party.dargy.gtbsolver.hud;

import cc.polyfrost.oneconfig.hud.TextHud;
import cc.polyfrost.oneconfig.libs.universal.UChat;
import party.dargy.gtbsolver.config.GTBConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BuildBattleHud extends TextHud {
    
    private boolean isWaiting = false;
    private List<String> wordList = new ArrayList<>();
    private String currentClue = "";
    private List<String> possibleWords = new ArrayList<>();
    
    public BuildBattleHud() {
        super(false);
        loadWordList();
    }
    
    private void loadWordList() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("wordlist.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        wordList.add(line.toLowerCase());
                        String noSpaces = line.replaceAll("\\s+", "").toLowerCase();
                        if (!noSpaces.equals(line.toLowerCase())) {
                            wordList.add(noSpaces);
                        }
                    }
                }
                reader.close();
                System.out.println("Loaded " + wordList.size() + " words from wordlist.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (example) {
            lines.add("§8__§ab§8_§ae§8_");
            lines.add("§7Possible words:");
            lines.add("§fCobweb");
            lines.add("§fGoblet");
            lines.add("§fRubber");
            lines.add("§fTablet");
            return;
        }
        
        if (isWaiting) {
            if (possibleWords.isEmpty()) {
                lines.add("§eWaiting...");
            } else if (!currentClue.contains("_")) {
                lines.add("§aCompleted: §7" + currentClue);
            } else {
                lines.add(formatClue(currentClue));
                lines.add("§7Possible words:");
                for (int i = 0; i < Math.min(possibleWords.size(), 10); i++) {
                    lines.add("§f" + possibleWords.get(i));
                }
                if (possibleWords.size() > 10) {
                    lines.add("§7...and " + (possibleWords.size() - 10) + " more");
                }
            }
        } else {
            lines.add("§7Idle");
        }
    }

    private static String formatClue(String clue) {
        StringBuilder formatted = new StringBuilder();
        boolean inLetters = false;
        boolean inUnderscores = false;
        
        for (char c : clue.toCharArray()) {
            if (Character.isLetter(c)) {
                if (!inLetters) {
                    formatted.append("§a");
                    inLetters = true;
                    inUnderscores = false;
                }
                formatted.append(c);
            } else {
                if (!inUnderscores) {
                    formatted.append("§8");
                    inUnderscores = true;
                    inLetters = false;
                }
                formatted.append(c);
            }
        }
        
        return formatted.toString();
    }
    
    public void onChatMessage(String message) {
        if (!GTBConfig.buildBattleHelper) {
            return;
        }
        
        String cleanMessage = message.replaceAll("§[0-9a-fk-or]", "");

        if (cleanMessage.startsWith("Builder: ") || cleanMessage.startsWith("Welcome back!")) {
            System.out.println("PASSED 1: " + cleanMessage);
            isWaiting = true;
            currentClue = "";
            possibleWords.clear();
        }
        
        if (message.contains("Want to play again")) {
            System.out.println("PASSED 2: " + cleanMessage);
            isWaiting = false;
            currentClue = "";
            possibleWords.clear();
        }
    }
    
    public void onActionBarMessage(String actionBarText) {
        if (!GTBConfig.buildBattleHelper || !isWaiting) {
            return;
        }
        
        String cleanText = actionBarText.replaceAll("§[0-9a-fk-or]", "");
        
        if (cleanText.contains("theme is")) {
            System.out.println("PASSED 3: " + cleanText);
            int startPos = cleanText.indexOf("theme is") + "theme is".length();
            String clue = cleanText.substring(startPos).trim();
                
            if (!clue.isEmpty() && !clue.equals(currentClue)) {
                currentClue = clue;
                updatePossibleWords(clue);
            }
        }
    }

    public void onBlockPlaced(String blockName) {
        if (!GTBConfig.buildBattleHelper || !isWaiting) {
            return;
        }

        // TODO: Logic for block placement
    }
    
    private void updatePossibleWords(String clue) {
        if (clue.isEmpty() || !clue.contains("_")) {
            return;
        }
        
        UChat.chat("§7[Debug] Looking for pattern: " + clue);
        possibleWords.clear();
        
        for (String word : wordList) {
            if (matchesPattern(word, clue)) {
                possibleWords.add(word);
            }
        }
        
        possibleWords.sort((a, b) -> {
            int lengthCompare = Integer.compare(a.length(), b.length());
            if (lengthCompare != 0) return lengthCompare;
            return a.compareToIgnoreCase(b);
        });
        
        UChat.chat("§7[Debug] Found " + possibleWords.size() + " matches");
        if (!possibleWords.isEmpty()) {
            UChat.chat("§7[Debug] First few: " + 
                String.join(", ", possibleWords.subList(0, Math.min(3, possibleWords.size()))));
        }
    }
    
    private boolean matchesPattern(String word, String pattern) {
        String normalizedWord = word.toLowerCase();
        String normalizedPattern = pattern.toLowerCase();
        
        if (normalizedWord.length() != normalizedPattern.length()) {
            return false;
        }
        
        for (int i = 0; i < normalizedWord.length(); i++) {
            char wordChar = normalizedWord.charAt(i);
            char patternChar = normalizedPattern.charAt(i);
            
            if (Character.isLetter(patternChar)) {
                if (wordChar != patternChar) {
                    return false;
                }
            }
            
            else if (patternChar == '_') {
                if (!Character.isLetter(wordChar)) {
                    return false;
                }
            }

            else {
                if (wordChar != patternChar) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void reset() {
        isWaiting = false;
        currentClue = "";
        possibleWords.clear();
    }
}
