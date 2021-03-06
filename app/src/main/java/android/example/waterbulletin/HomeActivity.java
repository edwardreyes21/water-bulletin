package android.example.waterbulletin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    public double current_intake = 0; // How much water (in milliliters) the user has drank today
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);

        // Every time the activity is created, update the current intake so that it shows
        // the proper value
        current_intake = sharedPreferences.getInt(
                "" + R.string.saved_current_intake, 0);

        updateCurrentIntake();
        updateCurrentImage();
        updateMinIntake();
        updateAlarm();

        // Catches an intent specifically designed for resetting the intake
        Intent intent = getIntent();
        int data = intent.getIntExtra("resetIntake", 0);
        if (data == 1) {
            resetIntake();
        }

        // If user taps on 'Alerts', send them to 'Alerts' page
        TextView alerts = findViewById(R.id.alerts);
        alerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AlertsActivity.class);
                startActivity(intent);
            }
        });

        // If user taps on 'Settings', send them to 'Settings' page
        TextView settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Handles the user pressing the 'Increase' button
        Button increase_intake = (Button) findViewById(R.id.increase_intake);
        increase_intake.setOnClickListener(new View.OnClickListener() {
            @Override
            // The cup of water represents the minimum intake per day
            // Assume that for now, the minimum intake is 2000 ml
            // We'll change the minimum later, as well as add an algorithm to calculate it
            public void onClick(View v) {
                // Gets the max intake using Shared Preferences
                SharedPreferences max_intake_preference = getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);

                // Reads the max intake key using Shared Preferences
                int default_max_intake = getResources().getInteger(
                        R.integer.settings_max_intake_default);
                int max_intake = max_intake_preference.getInt(getString(
                        R.string.settings_max_intake_key), default_max_intake);

                // Updates the current intake when the 'increase' button is clicked
                // Also updates the progress text on the top of the screen
                current_intake = sharedPreferences.getInt(
                        "" + R.string.saved_current_intake, 0);
                current_intake += 500;

                // Writes the newly updated current intake to saved current intake key
                SharedPreferences.Editor current_intake_editor = sharedPreferences.edit();
                current_intake_editor.putInt("" + R.string.saved_current_intake, (int) current_intake);
                current_intake_editor.apply();

                // Shows a warning when the user has drank more than their maximum intake
                // The toast is temporary, we'll show a more threatening alert later
                if (current_intake >= max_intake) {
                    Toast.makeText(HomeActivity.this,
                            "WARNING: You have consumed more than your daily maximum intake!",
                            Toast.LENGTH_LONG).show();
                }

                // Updates progress text and image
                updateCurrentIntake();
                updateCurrentImage();
            }
        });

    }

    public void updateCurrentIntake() {
        // Accesses the SharedPreferences key of current intake and resets it to 0
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);

        // Resets the progress text to 0ml
        TextView current = findViewById(R.id.current_intake);
        current.setText("" + sharedPreferences.getInt("" + R.string.saved_current_intake, 0));
    }

    public void updateCurrentImage() {
        // Accesses the SharedPreferences key of current intake and resets it to 0
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);

        // Gets the minimum intake set by user (or if not set, the default)
        int default_min_intake = getResources().getInteger(
                R.integer.settings_min_intake_default);
        double min_intake = sharedPreferences.getInt("" + R.string.settings_min_intake_key, default_min_intake);

        ImageView cup_of_water = findViewById(R.id.bottle_fullness);

        Log.v("updateCurrentImage", "" + current_intake + "/" + min_intake + " = " + current_intake / min_intake);
        // Based on how much the user has drank compared to their minimum intake,
        // updates the cup image
        if (current_intake / min_intake <= 0.25) {
            cup_of_water.setImageResource(R.drawable.cupofwater_100percent);
        }
        else if (current_intake / min_intake <= 0.50) {
            cup_of_water.setImageResource(R.drawable.cupofwater_66percent);
        }
        else if (current_intake / min_intake <= 0.75) {
            cup_of_water.setImageResource(R.drawable.cupofwater_33percent);
        } else if (current_intake / min_intake <= 1) {
            cup_of_water.setImageResource(R.drawable.cupofwater_0percent);
        }
    }

    public void updateMinIntake() {
        // Accesses the SharedPreferences key of current intake and resets it to 0
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);

        // Resets the progress text to 0ml
        TextView current = findViewById(R.id.goal_intake);
        int new_min_intake = sharedPreferences.getInt("" + R.string.settings_min_intake_key,
                2000);
        Log.v("updateMinIntake", "Min intake: " + new_min_intake);
        current.setText("" + sharedPreferences.getInt("" + R.string.settings_min_intake_key,
                2000));
        Log.v("updateMinIntake", "Min intake updated");
    }

    public void resetIntake() {
        // Accesses the SharedPreferences key of current intake and resets it to 0
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        SharedPreferences.Editor saved_current_intake_editor = sharedPreferences.edit();
        saved_current_intake_editor.putInt("" + R.string.saved_current_intake, 0);
        saved_current_intake_editor.apply();

        SharedPreferences.Editor image_progress_editor = sharedPreferences.edit();
        image_progress_editor.putInt("" + R.string.saved_image_progress, 0);
        image_progress_editor.apply();

        // Resets the progress text and image to 0ml
        TextView current = findViewById(R.id.current_intake);
        current.setText("" + sharedPreferences.getInt("" + R.string.saved_current_intake,
                0));

        ImageView cup_of_water = findViewById(R.id.bottle_fullness);
        cup_of_water.setImageResource(R.drawable.cupofwater_100percent);

        Log.v("Reset intake", "Current intake has been reset");
    }

    public void updateAlarm() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);

        // Uses Calendar API to reset intake every 24 hrs
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);

        // Uses AlarmManager to send out a push notification every X hour(s)
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent a_intent = new Intent(this, ResetReceiver.class);
        a_intent.putExtra("resetIntake", 1);
        alarmIntent = PendingIntent.getBroadcast(this, 0, a_intent, 0);

        // Milliseconds * seconds * minutes * hours
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        Log.v("Reset intake", "Interval in millis: " + 1000 * 30);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}