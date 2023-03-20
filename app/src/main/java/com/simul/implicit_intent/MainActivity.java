 package com.simul.implicit_intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

 public class MainActivity extends AppCompatActivity {
     private static final int PHOTO_REQUEST = 300 ;
     private ActivityResultLauncher<Intent> cameraLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fetches the photo taken by the camera through the intent and displays it in activity_main
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                //Get the photo into a Bitmap object and display it in the imageView
                Bitmap image = (Bitmap) result.getData().getExtras().get("data");
                ImageView imageview = (ImageView) findViewById(R.id.imageView);
                imageview.setImageBitmap(image);
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
         EditText phoneNumberField = findViewById(R.id.phoneNumberField);
         String phoneNumber = phoneNumberField.getText().toString();

         //contact number validation
         if(validatePhoneNumber(phoneNumber)){
             //create an intent for call
             Intent callIntent = new Intent(Intent.ACTION_DIAL);
             callIntent.setData(Uri.parse("tel:" + phoneNumber));
             startActivity(callIntent);
         }else{
             Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show();
         }
     }

     public void addEvent(View view) {
        //event title
         EditText eventTitleInput = findViewById(R.id.eventTitleInput);
         String eTitle = eventTitleInput.getText().toString();
         //event title

     }


     //phone number validator helper method
     public boolean validatePhoneNumber(String phoneNumber) {
//        regex for north american phone numbers
         String regex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
         return phoneNumber.matches(regex);
     }

 }