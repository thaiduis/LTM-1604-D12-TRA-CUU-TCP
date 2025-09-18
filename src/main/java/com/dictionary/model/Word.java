package com.dictionary.model;

/**
 * Model class đại diện cho một từ trong từ điển với thông tin chi tiết
 */
public class Word {
    private String englishWord;
    private String partOfSpeech;    // Từ loại (noun, verb, etc.)
    private String phoneticSpelling; // Phiên âm
    private String vietnameseMeaning;
    private String detailedDefinition; // Giải thích chi tiết
    private String exampleSentence;    // Câu ví dụ

    public Word() {
    }

    public Word(String englishWord, String partOfSpeech, String phoneticSpelling,
                String vietnameseMeaning, String detailedDefinition, String exampleSentence) {
        this.englishWord = englishWord;
        this.partOfSpeech = partOfSpeech;
        this.phoneticSpelling = phoneticSpelling;
        this.vietnameseMeaning = vietnameseMeaning;
        this.detailedDefinition = detailedDefinition;
        this.exampleSentence = exampleSentence;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public void setEnglishWord(String englishWord) {
        this.englishWord = englishWord;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getPhoneticSpelling() {
        return phoneticSpelling;
    }

    public void setPhoneticSpelling(String phoneticSpelling) {
        this.phoneticSpelling = phoneticSpelling;
    }

    public String getVietnameseMeaning() {
        return vietnameseMeaning;
    }

    public void setVietnameseMeaning(String vietnameseMeaning) {
        this.vietnameseMeaning = vietnameseMeaning;
    }

    public String getDetailedDefinition() {
        return detailedDefinition;
    }

    public void setDetailedDefinition(String detailedDefinition) {
        this.detailedDefinition = detailedDefinition;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(englishWord).append(" ").append(phoneticSpelling).append(" (").append(partOfSpeech).append(")\n");
        sb.append("Nghĩa: ").append(vietnameseMeaning).append("\n");
        sb.append("Định nghĩa: ").append(detailedDefinition).append("\n");
        if (exampleSentence != null && !exampleSentence.isEmpty()) {
            sb.append("Ví dụ: ").append(exampleSentence);
        }
        return sb.toString();
    }
}
