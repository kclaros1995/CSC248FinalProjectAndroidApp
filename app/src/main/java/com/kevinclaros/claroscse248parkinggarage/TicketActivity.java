package com.kevinclaros.claroscse248parkinggarage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class TicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_view);
        TextView dateTextView = (TextView) findViewById(R.id.dateId);
        TextView timeTextView = (TextView) findViewById(R.id.timeId);
        TextView subText = (TextView) findViewById(R.id.subtotalField);
        String currentDate = DateFormat.getDateInstance().format(new Date());
        String currentTime = DateFormat.getTimeInstance().format(new Date());


        dateTextView.setText(currentDate);
        timeTextView.setText(currentTime);


        Button button = (Button) findViewById(R.id.returnToMain);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
