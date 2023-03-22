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

public class MainActivity extends AppCompatActivity {
    private static final int PHOTO_REQUEST = 300;
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


    //setting the current date to defaultCurrentDate
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
        //so that it can display the default date/time
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
                }else{
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
        //invitees emails (storing all emails in an array list)
        eventEmailsInput = findViewById(R.id.eventEmailsInput);
        String eInviteesEmails = eventEmailsInput.getText().toString();
        ArrayList<String> inviteesEmailList = new ArrayList<>(Arrays.asList(eInviteesEmails.split(",")));
        //access type(public/private)
        eventAccessTypeGroup = findViewById(R.id.eventAccessTypeGroup);
        int selectedRadioButton = eventAccessTypeGroup.getCheckedRadioButtonId();
        if(selectedRadioButton== R.id.radioButtonPublic) accessType = "public";
        else accessType = "private";


        //validation
        if(!eTitle.isEmpty() && !eStartDate.isEmpty() && !eEndDate.isEmpty() && !inviteesEmailList.isEmpty()){
            //validated, good to proceed
        }else{
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
        endTimeBtn.setText(defaultCurrentTime);
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

                btn.setText(formattedDate);
            }
        }, 2023, 03, 06);
        dialog.show();
    }

    //set time using time picker dialog box
    private void OpenTimePicker(Button btn) {
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
//            month starts from 0 in datepicker, so for jan it should be 0+1, hence the plus 1
            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                String ampm = "am";
                if (hour > 12) ampm = "pm";
                btn.setText(String.valueOf(hour) + ":" + String.valueOf(min) + ' ' + ampm);
            }
        }, 12, 00, true);
        dialog.show();
    }

}