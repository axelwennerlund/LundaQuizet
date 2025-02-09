package com.example.lundaquizet;

public class Question {
    private String category;
    private String question;
    private String[] options;
    private String answer;

    public Question(String category, String question, String[] options, String answer) {
        this.category = category;
        this.question = question;
        this.options = options;
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public String getAnswer() {
        return answer;
    }
}