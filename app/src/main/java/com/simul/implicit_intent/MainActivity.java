package com.simul.implicit_intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final int PHOTO_REQUEST = 300;
    //camera (photo)
    private ActivityResultLauncher<Intent> cameraLauncher;
    EditText phoneNumberField;
    private Button startDateBtn;
    private Button endDateBtn;
    private Button startTimeBtn;
    private Button endTimeBtn;
    //    toggle
    private SwitchMaterial switchMaterial;
    boolean fullDayEventFlag = false;
    //    time toggle color
    private ColorStateList trackTintList;
    private EditText eventTitleInput;
    private EditText eventDescInput;
    private EditText eventEmailsInput;
    //    access type flag
    String accessType = "public";
    //event type
    RadioGroup eventAccessTypeGroup;
    //calendar variables that stores the input calendar values for both start/end date/time
    //is required when entered these values in the calendar
    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();


    //setting the current date to defaultCurrentDate`
    Date currentDate = new Date();
    SimpleDateFormat dateFormatDate = new SimpleDateFormat("MMM dd, yyyy");
    String defaultCurrentDate = dateFormatDate.format(currentDate);

    //setting the current time to defaultCurrentTime
    Date currentTime = new Date();
    SimpleDateFormat dateFormatTime = new SimpleDateFormat("h:mm a");
    String defaultCurrentTime = dateFormatTime.format(currentTime);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //calling setDefaultDateTime method - start/end date/time when the app opens
        //so that it can display the current date/time by default
        startDateBtn = findViewById(R.id.eventStartDateBtn);
        endDateBtn = findViewById(R.id.eventEndDateBtn);
        startTimeBtn = findViewById(R.id.eventStartTimeBtn);
        endTimeBtn = findViewById(R.id.eventEndTimeBtn);
        setDefaultDateTime(startDateBtn, endDateBtn, startTimeBtn, endTimeBtn);


        //fetches the photo taken by the camera through the intent and displays it in activity_main
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                //Get the photo into a Bitmap object and display it in the imageView
                Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                ImageView imageview = (ImageView) findViewById(R.id.imageView);
                imageview.setImageBitmap(image);
            }
        });

        //toggle switch
        switchMaterial = findViewById(R.id.eventAllDayInput);
        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //full day event

                    //disable the time buttons
                    startTimeBtn.setEnabled(false);
                    endTimeBtn.setEnabled(false);

                    trackTintList = ColorStateList.valueOf(getResources().getColor(R.color.switch_track_color_on));
                    switchMaterial.setTrackTintList(trackTintList);

                    fullDayEventFlag = true;
                } else {
                    //not full day event

                    //enable the time buttons
                    startTimeBtn.setEnabled(true);
                    endTimeBtn.setEnabled(true);

                    //change the tint color back to enabled color
                    trackTintList = ColorStateList.valueOf(getResources().getColor(R.color.switch_track_color_off));
                    switchMaterial.setTrackTintList(trackTintList);
                }
            }
        });


    }

    //method to get intent from the photo app and pass it to cameraLauncher to display it back in activity_main
    public void openCameraApp(View view) {
        //create an intent for camera
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (photoIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(photoIntent);
        }
    }

    //method to make a call using the phone no. entered by the user
    public void makeCall(View view) {
        //get number from the input field
        phoneNumberField = findViewById(R.id.phoneNumberField);
        String phoneNumber = phoneNumberField.getText().toString();

        //contact number validation
        if (validatePhoneNumber(phoneNumber)) {
            //create an intent for call
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show();
        }
    }

    //method that validates the input and then adds the event to the calendar
    public void addEvent(View view) {
        //event title
        eventTitleInput = findViewById(R.id.eventTitleInput);
        String eTitle = eventTitleInput.getText().toString();
        //event startDate
        String eStartDate = startDateBtn.getText().toString();
        //event endDate
        String eEndDate = endDateBtn.getText().toString();
        //event description
        eventDescInput = findViewById(R.id.eventDescInput);
        String eDesc = eventDescInput.getText().toString();
        //invitees emails
        eventEmailsInput = findViewById(R.id.eventEmailsInput);
        String eInviteesEmails = eventEmailsInput.getText().toString();
        //access type(public/private)
        eventAccessTypeGroup = findViewById(R.id.eventAccessTypeGroup);
        int selectedRadioButton = eventAccessTypeGroup.getCheckedRadioButtonId();
        if (selectedRadioButton == R.id.radioButtonPublic) accessType = "public";
        else accessType = "private";

        //validation
        if (!eTitle.isEmpty() && !eStartDate.isEmpty() && !eEndDate.isEmpty() && !eInviteesEmails.isEmpty()) {
            //null check passed

            // email validity check passed, , good to proceed with adding the event to the calendar
            if (areAllEmailsValid(eInviteesEmails)) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);

                intent.putExtra(CalendarContract.Events.TITLE, eTitle).putExtra(CalendarContract.Events.ALL_DAY, fullDayEventFlag).putExtra(CalendarContract.Events.DESCRIPTION, eDesc).putExtra(Intent.EXTRA_EMAIL, eInviteesEmails);

                if (accessType.equals("public"))
                    intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PUBLIC);
                else
                    intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);

                //need to convert time to millisecond before pushing (date and time) to the calendar
                long startTimeMs = startCalendar.getTimeInMillis();
                long endTimeMs = endCalendar.getTimeInMillis();
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMs);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMs);
                //start the actyivity of insert data into the app calendar
                startActivity(intent);
                //success toast
                Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please make sure you have entered valid email addresses!", Toast.LENGTH_SHORT).show();
            }
        } else {
            //something wrong with the input (i.e. not all required fields have data)
            Toast.makeText(this, "Please make sure all the required fields have data!", Toast.LENGTH_SHORT).show();
        }

    }


    //phone number validator helper method
    public boolean validatePhoneNumber(String phoneNumber) {
//        regex for north american phone numbers
        String regex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
        return phoneNumber.matches(regex);
    }

    //email validator helper method
    public static boolean areAllEmailsValid(String eInviteesEmails) {
        //converting the input of string of emails (separated by commas) to individual emails and then storing them in an arraylist
        ArrayList<String> emails = new ArrayList<>(Arrays.asList(eInviteesEmails.split(",")));
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        for (String email : emails) {
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                return false; // return false if any email address is invalid
            }
        }
        return true; // return true if all email addresses are valid
    }

    //methods to set the start/end date/time chosen from the date/time picker dialog box to the app UI
    public void setStartDate(View view) {
        OpenDatePicker(startDateBtn);
    }

    public void setEndDate(View view) {
        OpenDatePicker(endDateBtn);
    }

    public void setStartTime(View view) {
        OpenTimePicker(startTimeBtn);
    }

    public void setEndTime(View view) {
        OpenTimePicker(endTimeBtn);
    }

    //open the native calendar app
    public void openCalendar(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://com.android.calendar/time/"));
        startActivity(intent);
    }

    //helper methods

    //method to store the current date/time to the 4 fields (when the app launches)
    public void setDefaultDateTime(View view1, View view2, View view3, View view4) {
        startDateBtn.setText(defaultCurrentDate);
        endDateBtn.setText(defaultCurrentDate);
        startTimeBtn.setText(defaultCurrentTime);
        //passing 1 (hr) to incrementCurrentTime method to return a string with time plus 1 hr
        endTimeBtn.setText(incrementCurrentTime(1));
    }

    //set date using date picker dialog box
    private void OpenDatePicker(Button btn) {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
//            month starts from 0 in datepicker, so for jan it should be 0+1, hence the plus 1
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                // Format the date string from 642023 to Apr 6, 2023
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String formattedDate = sdf.format(calendar.getTime());

                //set the formatted date to the start/end date in the UI based on btn input
                btn.setText(formattedDate);

                //storing the chosen date in Calendar object (required when creating the event)
                if (btn == startDateBtn) {
                    startCalendar.set(Calendar.YEAR, year);
                    startCalendar.set(Calendar.MONTH, month);
                    startCalendar.set(Calendar.DAY_OF_MONTH, day);
                } else if (btn == endDateBtn) {
                    endCalendar.set(Calendar.YEAR, year);
                    endCalendar.set(Calendar.MONTH, month);
                    endCalendar.set(Calendar.DAY_OF_MONTH, day);
                }
            }
        }, 2023, 04, 22);
        dialog.show();
    }

    //set time using time picker dialog box
    private void OpenTimePicker(Button btn) {
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
//            month starts from 0 in datepicker, so for jan it should be 0+1, hence the plus 1
            public void onTimeSet(TimePicker timePicker, int hour, int min) {

                // am/pm logic
                String ampm = "am";
                if (hour > 12) ampm = "pm";
                btn.setText(String.valueOf(hour) + ":" + String.valueOf(min) + ' ' + ampm);

                //storing the chosen time in Calendar object (required when creating the event)
                if (btn == startTimeBtn) {
                    startCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    startCalendar.set(Calendar.MINUTE, min);
                } else if (btn == endTimeBtn) {
                    endCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    endCalendar.set(Calendar.MINUTE, min);
                }

            }
        }, 12, 00, true);
        dialog.show();
    }

    //increment the input time by a value (1 hour)
    //get the current calendar instance, adding the increment value, store it to a string and return it back
    //also assign it to the endTime (required when data needs to be added to the calendar)
    private String incrementCurrentTime(int incrementValue) {
        Calendar calendarIncremented = Calendar.getInstance();
        calendarIncremented.add(Calendar.HOUR_OF_DAY, incrementValue);
        Date incrementedTime = calendarIncremented.getTime();
        SimpleDateFormat dateFormatTime = new SimpleDateFormat("h:mm a");
        String defaultCurrentTimeIncremented = dateFormatTime.format(incrementedTime);

        endCalendar.set(Calendar.HOUR_OF_DAY, calendarIncremented.HOUR_OF_DAY);
        endCalendar.set(Calendar.MINUTE, calendarIncremented.MINUTE);

        return defaultCurrentTimeIncremented;
    }

}