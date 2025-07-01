package party.dargy.gtbsolver.hud;

import cc.polyfrost.oneconfig.hud.TextHud;
import cc.polyfrost.oneconfig.libs.universal.UChat;
import party.dargy.gtbsolver.config.GTBConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BuildBattleHud extends TextHud {
    
    private boolean isWaiting = false;
    private List<String> wordList = new ArrayList<>();
    private String currentClue = "";
    private List<String> possibleWords = new ArrayList<>();
    private Timer autoSendTimer;
    private Random random = new Random();
    
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
            isWaiting = true;
            currentClue = "";
            possibleWords.clear();
            if (autoSendTimer != null) {
                autoSendTimer.cancel();
                autoSendTimer = null;
            }
        }
        
        if (message.contains("Want to play again")) {
            isWaiting = false;
            currentClue = "";
            possibleWords.clear();
            if (autoSendTimer != null) {
                autoSendTimer.cancel();
                autoSendTimer = null;
            }
        }
    }
    
    public void onActionBarMessage(String actionBarText) {
        if (!GTBConfig.buildBattleHelper || !isWaiting) {
            return;
        }
        
        String cleanText = actionBarText.replaceAll("§[0-9a-fk-or]", "");
        
        if (cleanText.contains("theme is")) {
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
        
        if (GTBConfig.debugMode) UChat.chat("§7[Debug] Looking for pattern: " + clue);
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
        
        if (GTBConfig.debugMode) UChat.chat("§7[Debug] Found " + possibleWords.size() + " matches");
        if (!possibleWords.isEmpty()) {
            UChat.chat("§7All possible words: §f" + String.join(", ", possibleWords));
        }
        
        // Auto-send if only one word remains
        if (GTBConfig.autoSendBestGuess && possibleWords.size() == 1) {
            scheduleAutoSend(possibleWords.get(0));
        } else if (autoSendTimer != null) {
            autoSendTimer.cancel();
            autoSendTimer = null;
        }
    }
    
    private void scheduleAutoSend(String word) {
        // Cancel any existing timer
        if (autoSendTimer != null) {
            autoSendTimer.cancel();
        }
        
        // Calculate delay - prefer the lower number if min > max
        float minDelay = GTBConfig.minimumAutoSenDelay;
        float maxDelay = GTBConfig.maximumAutoSenDelay;
        
        float actualMin = Math.min(minDelay, maxDelay);
        float actualMax = Math.max(minDelay, maxDelay);
        
        // Generate random delay between actualMin and actualMax
        long delay = (long) (actualMin + (random.nextFloat() * (actualMax - actualMin)));
        
        if (GTBConfig.debugMode) {
            UChat.chat("§7[Debug] Auto-sending '" + word + "' in " + delay + "ms");
        }
        
        autoSendTimer = new Timer();
        autoSendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                UChat.say(word);
                if (GTBConfig.debugMode) {
                    UChat.chat("§7[Debug] Auto-sent: " + word);
                }
            }
        }, delay);
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
        
        if (autoSendTimer != null) {
            autoSendTimer.cancel();
            autoSendTimer = null;
        }
    }
}
