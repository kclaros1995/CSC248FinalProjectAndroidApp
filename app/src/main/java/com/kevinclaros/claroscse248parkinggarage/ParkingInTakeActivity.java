package com.kevinclaros.claroscse248parkinggarage;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;

public class ParkingInTakeActivity extends AppCompatActivity {
    DatabaseHelper myDb;
    private final double TAX_RATE = 8.875;
    long date = System.currentTimeMillis();
    SimpleDateFormat sdf = new SimpleDateFormat("MMM MM dd, yyyy h:mm a");
    static ArrayList<String> list = new ArrayList<>();
    Button parkButton;
    EditText firstNameField;
    EditText lastNameField;
    EditText licensePlateField;
    Button viewBookedSpots;
    Spinner vehicleSpinner;
    Spinner paymentPlanSpinner;
    Button unparkButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myDb = new DatabaseHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parking_in_take_view);
        viewBookedSpots = (Button) findViewById(R.id.viewBookedSpotsButton);
        vehicleSpinner = (Spinner) findViewById(R.id.vehicleSpinner);
        paymentPlanSpinner = (Spinner) findViewById(R.id.paymentPlanSpinner);
        firstNameField = (EditText) findViewById(R.id.firstNameField);
        lastNameField = (EditText) findViewById(R.id.lastNameField);
        licensePlateField = (EditText) findViewById(R.id.licensePlateField);
        parkButton = (Button) findViewById(R.id.parkButton);
        unparkButton = (Button) findViewById(R.id.unparkButton);

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<String>(ParkingInTakeActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Vehicles));
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleSpinner.setAdapter(vehicleAdapter);

        ArrayAdapter<String> paymentPlanAdapter = new ArrayAdapter<>(ParkingInTakeActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.PaymentPlans));
        paymentPlanSpinner.setAdapter(paymentPlanAdapter);

        parkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInserted = myDb.insertData(vehicleSpinner.getSelectedItem().toString(), licensePlateField.getText().toString(),
                        firstNameField.getText().toString(), lastNameField.getText().toString(), paymentPlanSpinner.getSelectedItem().toString());
                if (isInserted == true)
                    Toast.makeText(ParkingInTakeActivity.this, "vehicle inserted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ParkingInTakeActivity.this, "vehicle not parked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ParkingInTakeActivity.this, TicketActivity.class);
                startActivity(intent);
                firstNameField.getText().clear();
                lastNameField.getText().clear();
                licensePlateField.getText().clear();
                vehicleSpinner.setSelection(0);
                paymentPlanSpinner.setSelection(0);
            }
        });




        viewBookedSpots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = myDb.getAllVehicles();
                Cursor res2 = myDb.getParkingSpot2();
                if (res.getCount() == 0) {
                    showMessage("Sorry", "Vehicle Lot is Empty");
                    return;
                }
                if (res2.getCount() == 0) {
                    showMessage("Sorry", "Vehicle Lot is Empty");
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                    buffer.append("Ticket Number :" + res.getString(0) + "\n");
                    buffer.append("vehicle type :" + res.getString(1) + "\n");
                    buffer.append("license plate :" + res.getString(2) + "\n");
                    buffer.append("first name :" + res.getString(3) + "\n");
                    buffer.append("last name :" + res.getString(4) + "\n");
                    buffer.append("Date: " + res.getString(5) + "\n");
                    buffer.append("Time: " + res.getString(6) + "\n");
                    buffer.append("Parking Spot: " + res.getInt(7) + "\n");
                    buffer.append("Payment Plan: " + res.getString(8) + "\n\n");
                }
                showMessage("Data", buffer.toString());
            }
        });
        unparkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParkingInTakeActivity.this, InvoiceActivity.class);
                Cursor res = myDb.getCurrentVehicle(licensePlateField.getText().toString());

                String arrival = res.getString(6);
                LocalTime departureTime = LocalTime.now();
                LocalTime arrivalTime = LocalTime.parse(arrival);
                double weeklyPaymentPrice = .98;
                double monthlyPrice = 0.95;

                if (res.getString(8).equals("DAILY") || res.getString(8).equals("Select Payment Plan")) {
                    if (LocalTime.now().isAfter(LocalTime.NOON) && LocalTime.now().isBefore(LocalTime.MAX)) {
                        if (res.getString(1).equals("Motorcycle")) {
                            double amountDue = calculateMotorcycleRegularRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Compact Car")) {
                            double amountDue = calculateCompactCarRegularRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Mid Size Car")) {
                            double amountDue = calculateMidSizeCarRegularRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Truck/Van/SUV")) {
                            double amountDue = calculateTruckRegularRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        }
                    } else if (LocalTime.now().isAfter(LocalTime.MIDNIGHT) && LocalTime.now().isBefore(LocalTime.NOON)) {
                        if (res.getString(1).equals("Motorcycle")) {
                            double amountDue = calculateMotorcycleEarlyBirdRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Compact Car")) {
                            double amountDue = calculateCompactVehicleEarlyBirdRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Mid Size Car")) {
                            double amountDue = calculateMidSizeVehicleEarlyBirdRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Truck/Van/SUV")) {
                            double amountDue = calculateTruckEarlyBirdRate(arrivalTime, departureTime);
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        }
                    }
                } else if (res.getString(8).equals("WEEKLY")) {
                    if (LocalTime.now().isAfter(LocalTime.NOON) && LocalTime.now().isBefore(LocalTime.MAX)) {
                        if (res.getString(1).equals("Motorcycle")) {
                            double amountDue = calculateMotorcycleRegularRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Compact Car")) {
                            double amountDue = calculateCompactCarRegularRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Mid Size Car")) {
                            double amountDue = calculateMidSizeCarRegularRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Truck/Van/SUV")) {
                            double amountDue = calculateTruckRegularRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        }
                    } else if (LocalTime.now().isAfter(LocalTime.MIDNIGHT) && LocalTime.now().isBefore(LocalTime.NOON)) {
                        if (res.getString(1).equals("Motorcycle")) {
                            double amountDue = calculateMotorcycleEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Compact Car")) {
                            double amountDue = calculateCompactVehicleEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Mid Size Car")) {
                            double amountDue = calculateMidSizeVehicleEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Truck/Van/SUV")) {
                            double amountDue = calculateTruckEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= weeklyPaymentPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        }
                    }
                } else if (res.getString(8).equals("MONTHLY")) {
                    if (LocalTime.now().isAfter(LocalTime.NOON) && LocalTime.now().isBefore(LocalTime.MAX)) {
                        if (res.getString(1).equals("Motorcycle")) {
                            double amountDue = calculateMotorcycleRegularRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Compact Car")) {
                            double amountDue = calculateCompactCarRegularRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Mid Size Car")) {
                            double amountDue = calculateMidSizeCarRegularRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        } else if (res.getString(1).equals("Truck/Van/SUV")) {
                            double amountDue = calculateTruckRegularRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        }
                    } else if (LocalTime.now().isAfter(LocalTime.MIDNIGHT) && LocalTime.now().isBefore(LocalTime.NOON)) {
                        if (res.getString(1).equals("Motorcycle")) {
                            double amountDue = calculateMotorcycleEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Compact Car")) {
                            double amountDue = calculateCompactVehicleEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Mid Size Car")) {
                            double amountDue = calculateMidSizeVehicleEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));
                        } else if (res.getString(1).equals("Truck/Van/SUV")) {
                            double amountDue = calculateTruckEarlyBirdRate(arrivalTime, departureTime);
                            amountDue *= monthlyPrice;
                            intent.putExtra("Amount Due", String.valueOf(amountDue));

                        }
                    }
                }
                intent.putExtra("Ticket Number", res.getString(0));
                intent.putExtra("License Plate", res.getString(2));
                intent.putExtra("Date", res.getString(5));
                intent.putExtra("Arrival Time", res.getString(6));
                intent.putExtra("Parking Spot", res.getString(7));
                startActivity(intent);
                Integer deletedRows = myDb.removeVehicles(licensePlateField.getText().toString().trim());
                if (deletedRows > 0)
                    Toast.makeText(ParkingInTakeActivity.this, "Vehicle unparked", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ParkingInTakeActivity.this, "Vehicle not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public double calculateMotorcycleRegularRate(LocalTime startTime, LocalTime endTime) {
        startTime.until(endTime, SECONDS);
        long seconds = SECONDS.between(startTime, endTime);
        double MOTORCYCLE_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            MOTORCYCLE_REGULAR_RATE = 30;
            tax = (30 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            MOTORCYCLE_REGULAR_RATE = 35;
            tax = (35 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            MOTORCYCLE_REGULAR_RATE = 40;
            tax = (40 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            MOTORCYCLE_REGULAR_RATE = 45;
            tax = (45 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            MOTORCYCLE_REGULAR_RATE = 50;
            tax = (50 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            MOTORCYCLE_REGULAR_RATE = 55;
            tax = (55 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            MOTORCYCLE_REGULAR_RATE = 60;
            tax = (60 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            MOTORCYCLE_REGULAR_RATE = 65;
            tax = (65 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            MOTORCYCLE_REGULAR_RATE = 70;
            tax = (70 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            MOTORCYCLE_REGULAR_RATE = 75;
            tax = (75 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            MOTORCYCLE_REGULAR_RATE = 80;
            tax = (80 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            MOTORCYCLE_REGULAR_RATE = 85;
            tax = (85 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            MOTORCYCLE_REGULAR_RATE = 90;
            tax = (90 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            MOTORCYCLE_REGULAR_RATE = 95;
            tax = (95 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            MOTORCYCLE_REGULAR_RATE = 100;
            tax = (100 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            MOTORCYCLE_REGULAR_RATE = 105;
            tax = (105 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            MOTORCYCLE_REGULAR_RATE = 110;
            tax = (110 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            MOTORCYCLE_REGULAR_RATE = 115;
            tax = (115 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            MOTORCYCLE_REGULAR_RATE = 120;
            tax = (120 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            MOTORCYCLE_REGULAR_RATE = 125;
            tax = (125 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            MOTORCYCLE_REGULAR_RATE = 130;
            tax = (130 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            MOTORCYCLE_REGULAR_RATE = 135;
            tax = (135 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            MOTORCYCLE_REGULAR_RATE = 140;
            tax = (140 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            MOTORCYCLE_REGULAR_RATE = 145;
            tax = (145 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            MOTORCYCLE_REGULAR_RATE = 150;
            tax = (150 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            MOTORCYCLE_REGULAR_RATE = 155;
            tax = (155 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            MOTORCYCLE_REGULAR_RATE = 160;
            tax = (160 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            MOTORCYCLE_REGULAR_RATE = 165;
            tax = (165 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            MOTORCYCLE_REGULAR_RATE = 170;
            tax = (170 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }

    public double calculateCompactCarRegularRate(LocalTime startTime, LocalTime endTime) {
        startTime.until(endTime, SECONDS);
        long seconds = SECONDS.between(startTime, endTime);

        double COMPACT_CAR_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            COMPACT_CAR_REGULAR_RATE = 35;
            tax = (30 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            COMPACT_CAR_REGULAR_RATE = 35;
            tax = (35 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            COMPACT_CAR_REGULAR_RATE = 40;
            tax = (40 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            COMPACT_CAR_REGULAR_RATE = 45;
            tax = (45 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            COMPACT_CAR_REGULAR_RATE = 50;
            tax = (50 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            COMPACT_CAR_REGULAR_RATE = 55;
            tax = (55 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            COMPACT_CAR_REGULAR_RATE = 60;
            tax = (60 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            COMPACT_CAR_REGULAR_RATE = 65;
            tax = (65 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            COMPACT_CAR_REGULAR_RATE = 70;
            tax = (70 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            COMPACT_CAR_REGULAR_RATE = 75;
            tax = (75 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            COMPACT_CAR_REGULAR_RATE = 80;
            tax = (80 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            COMPACT_CAR_REGULAR_RATE = 85;
            tax = (85 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            COMPACT_CAR_REGULAR_RATE = 90;
            tax = (90 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            COMPACT_CAR_REGULAR_RATE = 95;
            tax = (95 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            COMPACT_CAR_REGULAR_RATE = 100;
            tax = (100 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            COMPACT_CAR_REGULAR_RATE = 105;
            tax = (105 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            COMPACT_CAR_REGULAR_RATE = 110;
            tax = (110 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            COMPACT_CAR_REGULAR_RATE = 115;
            tax = (115 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            COMPACT_CAR_REGULAR_RATE = 120;
            tax = (120 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            COMPACT_CAR_REGULAR_RATE = 125;
            tax = (125 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            COMPACT_CAR_REGULAR_RATE = 130;
            tax = (130 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            COMPACT_CAR_REGULAR_RATE = 135;
            tax = (135 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            COMPACT_CAR_REGULAR_RATE = 140;
            tax = (140 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            COMPACT_CAR_REGULAR_RATE = 145;
            tax = (145 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            COMPACT_CAR_REGULAR_RATE = 150;
            tax = (150 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            COMPACT_CAR_REGULAR_RATE = 155;
            tax = (155 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            COMPACT_CAR_REGULAR_RATE = 160;
            tax = (160 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            COMPACT_CAR_REGULAR_RATE = 165;
            tax = (165 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            COMPACT_CAR_REGULAR_RATE = 170;
            tax = (170 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }

    public double calculateMidSizeCarRegularRate(LocalTime startTime, LocalTime endTime) {
        startTime.until(endTime, SECONDS);
        long seconds = SECONDS.between(startTime, endTime);

        double MID_SIZE_CAR_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            MID_SIZE_CAR_REGULAR_RATE = 40;
            tax = (30 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            MID_SIZE_CAR_REGULAR_RATE = 45;
            tax = (35 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            MID_SIZE_CAR_REGULAR_RATE = 50;
            tax = (40 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            MID_SIZE_CAR_REGULAR_RATE = 55;
            tax = (45 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            MID_SIZE_CAR_REGULAR_RATE = 60;
            tax = (50 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            MID_SIZE_CAR_REGULAR_RATE = 65;
            tax = (55 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            MID_SIZE_CAR_REGULAR_RATE = 70;
            tax = (60 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            MID_SIZE_CAR_REGULAR_RATE = 75;
            tax = (65 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            MID_SIZE_CAR_REGULAR_RATE = 80;
            tax = (70 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            MID_SIZE_CAR_REGULAR_RATE = 85;
            tax = (75 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            MID_SIZE_CAR_REGULAR_RATE = 90;
            tax = (80 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            MID_SIZE_CAR_REGULAR_RATE = 95;
            tax = (85 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            MID_SIZE_CAR_REGULAR_RATE = 100;
            tax = (90 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            MID_SIZE_CAR_REGULAR_RATE = 95;
            tax = (95 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            MID_SIZE_CAR_REGULAR_RATE = 100;
            tax = (100 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            MID_SIZE_CAR_REGULAR_RATE = 105;
            tax = (105 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            MID_SIZE_CAR_REGULAR_RATE = 110;
            tax = (110 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            MID_SIZE_CAR_REGULAR_RATE = 115;
            tax = (115 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            MID_SIZE_CAR_REGULAR_RATE = 120;
            tax = (120 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            MID_SIZE_CAR_REGULAR_RATE = 125;
            tax = (125 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            MID_SIZE_CAR_REGULAR_RATE = 130;
            tax = (130 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            MID_SIZE_CAR_REGULAR_RATE = 135;
            tax = (135 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            MID_SIZE_CAR_REGULAR_RATE = 140;
            tax = (140 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            MID_SIZE_CAR_REGULAR_RATE = 145;
            tax = (145 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            MID_SIZE_CAR_REGULAR_RATE = 150;
            tax = (150 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            MID_SIZE_CAR_REGULAR_RATE = 155;
            tax = (155 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            MID_SIZE_CAR_REGULAR_RATE = 160;
            tax = (160 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            MID_SIZE_CAR_REGULAR_RATE = 165;
            tax = (165 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            MID_SIZE_CAR_REGULAR_RATE = 170;
            tax = (170 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }

    public double calculateTruckRegularRate(LocalTime startTime, LocalTime endTime) {
        long seconds = SECONDS.between(startTime, endTime);
        double TRUCK_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            TRUCK_REGULAR_RATE = 45;
            tax = (30 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            TRUCK_REGULAR_RATE = 45;
            tax = (35 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            TRUCK_REGULAR_RATE = 50;
            tax = (40 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            TRUCK_REGULAR_RATE = 55;
            tax = (45 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            TRUCK_REGULAR_RATE = 60;
            tax = (50 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            TRUCK_REGULAR_RATE = 65;
            tax = (55 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            TRUCK_REGULAR_RATE = 65;
            tax = (60 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            TRUCK_REGULAR_RATE = 70;
            tax = (65 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            TRUCK_REGULAR_RATE = 75;
            tax = (70 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            TRUCK_REGULAR_RATE = 80;
            tax = (75 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            TRUCK_REGULAR_RATE = 85;
            tax = (80 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            TRUCK_REGULAR_RATE = 90;
            tax = (85 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            TRUCK_REGULAR_RATE = 95;
            tax = (90 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            TRUCK_REGULAR_RATE = 100;
            tax = (95 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            TRUCK_REGULAR_RATE = 105;
            tax = (100 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            TRUCK_REGULAR_RATE = 110;
            tax = (105 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            TRUCK_REGULAR_RATE = 115;
            tax = (110 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            TRUCK_REGULAR_RATE = 115;
            tax = (115 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            TRUCK_REGULAR_RATE = 120;
            tax = (120 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            TRUCK_REGULAR_RATE = 125;
            tax = (125 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            TRUCK_REGULAR_RATE = 130;
            tax = (130 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            TRUCK_REGULAR_RATE = 135;
            tax = (135 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            TRUCK_REGULAR_RATE = 140;
            tax = (140 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            TRUCK_REGULAR_RATE = 145;
            tax = (145 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            TRUCK_REGULAR_RATE = 150;
            tax = (150 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            TRUCK_REGULAR_RATE = 155;
            tax = (155 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            TRUCK_REGULAR_RATE = 160;
            tax = (160 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            TRUCK_REGULAR_RATE = 165;
            tax = (165 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            TRUCK_REGULAR_RATE = 170;
            tax = (170 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }

    public double calculateMotorcycleEarlyBirdRate(LocalTime startTime, LocalTime endTime) {
        startTime.until(endTime, SECONDS);
        long seconds = SECONDS.between(startTime, endTime);
        double MOTORCYLCE_EARLY_BIRD_RATE = 0.90;
        double MOTORCYCLE_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            MOTORCYCLE_REGULAR_RATE = 30 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (30 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            MOTORCYCLE_REGULAR_RATE = 35 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (35 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            MOTORCYCLE_REGULAR_RATE = 40 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (40 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            MOTORCYCLE_REGULAR_RATE = 45 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (45 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            MOTORCYCLE_REGULAR_RATE = 50 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (50 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            MOTORCYCLE_REGULAR_RATE = 55 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (55 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            MOTORCYCLE_REGULAR_RATE = 60 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (60 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            MOTORCYCLE_REGULAR_RATE = 65 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (65 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            MOTORCYCLE_REGULAR_RATE = 70 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (70 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            MOTORCYCLE_REGULAR_RATE = 75 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (75 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            MOTORCYCLE_REGULAR_RATE = 80 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (80 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            MOTORCYCLE_REGULAR_RATE = 85 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (85 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            MOTORCYCLE_REGULAR_RATE = 90 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (90 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            MOTORCYCLE_REGULAR_RATE = 95 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (95 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            MOTORCYCLE_REGULAR_RATE = 100 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (100 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            MOTORCYCLE_REGULAR_RATE = 105 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (105 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            MOTORCYCLE_REGULAR_RATE = 110 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (110 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            MOTORCYCLE_REGULAR_RATE = 115 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (115 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            MOTORCYCLE_REGULAR_RATE = 120 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (120 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            MOTORCYCLE_REGULAR_RATE = 125 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (125 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            MOTORCYCLE_REGULAR_RATE = 130 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (130 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            MOTORCYCLE_REGULAR_RATE = 135 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (135 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            MOTORCYCLE_REGULAR_RATE = 140 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (140 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            MOTORCYCLE_REGULAR_RATE = 145 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (145 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            MOTORCYCLE_REGULAR_RATE = 150 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (150 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            MOTORCYCLE_REGULAR_RATE = 155 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (155 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            MOTORCYCLE_REGULAR_RATE = 160 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (160 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            MOTORCYCLE_REGULAR_RATE = 165 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (165 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            MOTORCYCLE_REGULAR_RATE = 170 * MOTORCYLCE_EARLY_BIRD_RATE;
            tax = (170 * TAX_RATE) / 100;
            total = MOTORCYCLE_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }

    public double calculateCompactVehicleEarlyBirdRate(LocalTime startTime, LocalTime endTime) {
        startTime.until(endTime, SECONDS);
        long seconds = SECONDS.between(startTime, endTime);
        double COMPACT_CAR_EARLY_BIRD_RATE = 0.90;
        double COMPACT_CAR_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            COMPACT_CAR_REGULAR_RATE = 35 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (30 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            COMPACT_CAR_REGULAR_RATE = 35 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (35 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            COMPACT_CAR_REGULAR_RATE = 40 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (40 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            COMPACT_CAR_REGULAR_RATE = 45 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (45 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            COMPACT_CAR_REGULAR_RATE = 50 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (50 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            COMPACT_CAR_REGULAR_RATE = 55 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (55 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            COMPACT_CAR_REGULAR_RATE = 60 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (60 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            COMPACT_CAR_REGULAR_RATE = 65 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (65 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            COMPACT_CAR_REGULAR_RATE = 70 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (70 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            COMPACT_CAR_REGULAR_RATE = 75 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (75 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            COMPACT_CAR_REGULAR_RATE = 80 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (80 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            COMPACT_CAR_REGULAR_RATE = 85 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (85 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            COMPACT_CAR_REGULAR_RATE = 90 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (90 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            COMPACT_CAR_REGULAR_RATE = 95 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (95 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            COMPACT_CAR_REGULAR_RATE = 100 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (100 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            COMPACT_CAR_REGULAR_RATE = 105 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (105 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            COMPACT_CAR_REGULAR_RATE = 110 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (110 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            COMPACT_CAR_REGULAR_RATE = 115 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (115 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            COMPACT_CAR_REGULAR_RATE = 120 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (120 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            COMPACT_CAR_REGULAR_RATE = 125 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (125 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            COMPACT_CAR_REGULAR_RATE = 130 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (130 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            COMPACT_CAR_REGULAR_RATE = 135 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (135 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            COMPACT_CAR_REGULAR_RATE = 140 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (140 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            COMPACT_CAR_REGULAR_RATE = 145 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (145 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            COMPACT_CAR_REGULAR_RATE = 150 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (150 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            COMPACT_CAR_REGULAR_RATE = 155 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (155 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            COMPACT_CAR_REGULAR_RATE = 160 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (160 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            COMPACT_CAR_REGULAR_RATE = 165 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (165 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            COMPACT_CAR_REGULAR_RATE = 170 * COMPACT_CAR_EARLY_BIRD_RATE;
            tax = (170 * TAX_RATE) / 100;
            total = COMPACT_CAR_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }

    public double calculateMidSizeVehicleEarlyBirdRate(LocalTime startTime, LocalTime endTime) {
        startTime.until(endTime, SECONDS);
        long seconds = SECONDS.between(startTime, endTime);
        double MID_SIZE_CAR_EALY_BIRD_RATE = 0.90;
        double MID_SIZE_CAR_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            MID_SIZE_CAR_REGULAR_RATE = 40 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (30 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            MID_SIZE_CAR_REGULAR_RATE = 45 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (35 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            MID_SIZE_CAR_REGULAR_RATE = 50 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (40 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            MID_SIZE_CAR_REGULAR_RATE = 55 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (45 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            MID_SIZE_CAR_REGULAR_RATE = 60 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (50 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            MID_SIZE_CAR_REGULAR_RATE = 65 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (55 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            MID_SIZE_CAR_REGULAR_RATE = 70 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (60 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            MID_SIZE_CAR_REGULAR_RATE = 75 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (65 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            MID_SIZE_CAR_REGULAR_RATE = 80 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (70 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            MID_SIZE_CAR_REGULAR_RATE = 85 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (75 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            MID_SIZE_CAR_REGULAR_RATE = 90 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (80 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            MID_SIZE_CAR_REGULAR_RATE = 95 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (85 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            MID_SIZE_CAR_REGULAR_RATE = 100 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (90 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            MID_SIZE_CAR_REGULAR_RATE = 105 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (95 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            MID_SIZE_CAR_REGULAR_RATE = 110 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (100 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            MID_SIZE_CAR_REGULAR_RATE = 115 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (105 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            MID_SIZE_CAR_REGULAR_RATE = 120 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (110 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            MID_SIZE_CAR_REGULAR_RATE = 125 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (115 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            MID_SIZE_CAR_REGULAR_RATE = 130 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (120 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            MID_SIZE_CAR_REGULAR_RATE = 135 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (125 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            MID_SIZE_CAR_REGULAR_RATE = 140 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (130 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            MID_SIZE_CAR_REGULAR_RATE = 145 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (135 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            MID_SIZE_CAR_REGULAR_RATE = 150 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (140 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            MID_SIZE_CAR_REGULAR_RATE = 155 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (145 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            MID_SIZE_CAR_REGULAR_RATE = 160 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (150 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            MID_SIZE_CAR_REGULAR_RATE = 175 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (155 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            MID_SIZE_CAR_REGULAR_RATE = 180 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (160 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            MID_SIZE_CAR_REGULAR_RATE = 185 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (165 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            MID_SIZE_CAR_REGULAR_RATE = 190 * MID_SIZE_CAR_EALY_BIRD_RATE;
            tax = (170 * TAX_RATE) / 100;
            total = MID_SIZE_CAR_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }

    public double calculateTruckEarlyBirdRate(LocalTime startTime, LocalTime endTime) {
        long seconds = SECONDS.between(startTime, endTime);
        double TRUCK_EARLY_BIRD_RATE = 0.90;
        double TRUCK_REGULAR_RATE;
        double tax;
        double total = 0;

        if (seconds <= 60) {
            TRUCK_REGULAR_RATE = 45 * TRUCK_EARLY_BIRD_RATE;
            tax = (30 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 60 && seconds <= 70) {
            TRUCK_REGULAR_RATE = 45 * TRUCK_EARLY_BIRD_RATE;
            tax = (35 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 70 && seconds <= 80) {
            TRUCK_REGULAR_RATE = 50 * TRUCK_EARLY_BIRD_RATE;
            tax = (40 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 80 && seconds <= 90) {
            TRUCK_REGULAR_RATE = 55 * TRUCK_EARLY_BIRD_RATE;
            tax = (45 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 90 && seconds <= 120) {
            TRUCK_REGULAR_RATE = 60 * TRUCK_EARLY_BIRD_RATE;
            tax = (50 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        ///// after two hours

        if (seconds > 120 && seconds <= 130) {
            TRUCK_REGULAR_RATE = 65 * TRUCK_EARLY_BIRD_RATE;
            tax = (55 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 130 && seconds <= 140) {
            TRUCK_REGULAR_RATE = 70 * TRUCK_EARLY_BIRD_RATE;
            tax = (60 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 140 && seconds <= 150) {
            TRUCK_REGULAR_RATE = 75 * TRUCK_EARLY_BIRD_RATE;
            tax = (65 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 150 && seconds <= 180) {
            TRUCK_REGULAR_RATE = 80 * TRUCK_EARLY_BIRD_RATE;
            tax = (70 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        }

        // after three hours

        if (seconds > 180 && seconds <= 190) {
            TRUCK_REGULAR_RATE = 85 * TRUCK_EARLY_BIRD_RATE;
            tax = (75 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 190 && seconds <= 200) {
            TRUCK_REGULAR_RATE = 89 * TRUCK_EARLY_BIRD_RATE;
            tax = (80 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 200 && seconds <= 210) {
            TRUCK_REGULAR_RATE = 95 * TRUCK_EARLY_BIRD_RATE;
            tax = (85 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 220 && seconds <= 240) {
            TRUCK_REGULAR_RATE = 100 * TRUCK_EARLY_BIRD_RATE;
            tax = (90 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        // after four hour

        if (seconds > 240 && seconds <= 250) {
            TRUCK_REGULAR_RATE = 105 * TRUCK_EARLY_BIRD_RATE;
            tax = (95 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 250 && seconds <= 260) {
            TRUCK_REGULAR_RATE = 110 * TRUCK_EARLY_BIRD_RATE;
            tax = (100 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 260 && seconds <= 270) {
            TRUCK_REGULAR_RATE = 115 * TRUCK_EARLY_BIRD_RATE;
            tax = (105 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 270 && seconds <= 300) {
            TRUCK_REGULAR_RATE = 120 * TRUCK_EARLY_BIRD_RATE;
            tax = (110 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        // after five

        if (seconds > 300 && seconds <= 310) {
            TRUCK_REGULAR_RATE = 135 * TRUCK_EARLY_BIRD_RATE;
            tax = (115 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 310 && seconds <= 320) {
            TRUCK_REGULAR_RATE = 140 * TRUCK_EARLY_BIRD_RATE;
            tax = (120 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 320 && seconds <= 330) {
            TRUCK_REGULAR_RATE = 145 * TRUCK_EARLY_BIRD_RATE;
            tax = (125 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 330 && seconds <= 360) {
            TRUCK_REGULAR_RATE = 150 * TRUCK_EARLY_BIRD_RATE;
            tax = (130 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        //after six

        if (seconds > 360 && seconds <= 370) {
            TRUCK_REGULAR_RATE = 155 * TRUCK_EARLY_BIRD_RATE;
            tax = (135 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 370 && seconds <= 380) {
            TRUCK_REGULAR_RATE = 160 * TRUCK_EARLY_BIRD_RATE;
            tax = (140 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 380 && seconds <= 390) {
            TRUCK_REGULAR_RATE = 165 * TRUCK_EARLY_BIRD_RATE;
            tax = (145 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 390 && seconds <= 420) {
            TRUCK_REGULAR_RATE = 170 * TRUCK_EARLY_BIRD_RATE;
            tax = (150 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        // after seven

        if (seconds > 420 && seconds <= 430) {
            TRUCK_REGULAR_RATE = 175 * TRUCK_EARLY_BIRD_RATE;
            tax = (155 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 430 && seconds <= 440) {
            TRUCK_REGULAR_RATE = 180 * TRUCK_EARLY_BIRD_RATE;
            tax = (160 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;

        } else if (seconds > 440 && seconds <= 450) {
            TRUCK_REGULAR_RATE = 185 * TRUCK_EARLY_BIRD_RATE;
            tax = (165 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        } else if (seconds > 450 && seconds <= 480) {
            TRUCK_REGULAR_RATE = 190 * TRUCK_EARLY_BIRD_RATE;
            tax = (170 * TAX_RATE) / 100;
            total = TRUCK_REGULAR_RATE + tax;
            return total;
        }

        return total;
    }


    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public EditText getLicensePlateField() {
        return licensePlateField;
    }

    public static ArrayList<String> getList() {
        return list;
    }
}


