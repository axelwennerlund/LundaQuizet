package com.example.lundaquizet;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionLoader {
    private static List<Question> questionList = new ArrayList<>();

    public static void loadQuestions(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("questions.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            questionList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String category = obj.getString("category");
                String question = obj.getString("question");
                JSONArray optionsArray = obj.getJSONArray("options");
                String[] options = new String[optionsArray.length()];
                for (int j = 0; j < optionsArray.length(); j++) {
                    options[j] = optionsArray.getString(j);
                }
                String answer = obj.getString("answer");

                questionList.add(new Question(category, question, options, answer));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Question getQuestionByCategory(String category) {
        List<Question> filteredQuestions = new ArrayList<>();
        for (Question q : questionList) {
            if (q.getCategory().equalsIgnoreCase(category)) {
                filteredQuestions.add(q);
            }
        }
        if (!filteredQuestions.isEmpty()) {
            Random random = new Random();
            return filteredQuestions.get(random.nextInt(filteredQuestions.size()));
        }
        return null;
    }
}