package com.kevinclaros.claroscse248parkinggarage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.SplittableRandom;

import static java.time.temporal.ChronoUnit.SECONDS;

public class InvoiceActivity extends AppCompatActivity {
    private final double TAX_RATE = 8.875;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_view);

        LocalTime departureTime = LocalTime.now();

        TextView ticketNumText = (TextView) findViewById(R.id.ticketField);
        TextView licenseText = (TextView) findViewById(R.id.plateField);
        TextView spotText = (TextView) findViewById(R.id.spotField);
        TextView arrivalText = (TextView) findViewById(R.id.arrivalField);
        TextView departureTxt = (TextView) findViewById(R.id.departureField);
        TextView subText = (TextView) findViewById(R.id.subtotalField);

        Intent intent = getIntent();
        String ticketNumber = intent.getExtras().getString("Ticket Number");
        String license = intent.getExtras().getString("License Plate");
        String spot = intent.getExtras().getString("Parking Spot");
        String arrival = intent.getExtras().getString("Arrival Time");
        String amountDue = intent.getExtras().getString("Amount Due");

        ticketNumText.setText(ticketNumber);
        licenseText.setText(license);
        spotText.setText(spot);
        arrivalText.setText(arrival);


        departureTxt.setText(String.valueOf(departureTime));
        subText.setText(String.valueOf(amountDue));


    }

}
