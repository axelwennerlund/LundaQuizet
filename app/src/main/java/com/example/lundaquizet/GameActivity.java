package com.example.lundaquizet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    private TextView questionText;
    private Button option1, option2, option3, option4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        questionText = findViewById(R.id.questionText);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        questionText.setText("");

        option1.setText("Alternativ 1");
        option2.setText("Alternativ 2");
        option3.setText("Alternativ 3");
        option4.setText("Alternativ 4");
    }
}
