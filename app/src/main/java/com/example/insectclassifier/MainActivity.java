package com.example.insectclassifier;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_TAKE_IMAGE = 2;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout buttonLoadImage = (LinearLayout) findViewById(R.id.button_gallery);
        LinearLayout buttonLoadCamera = (LinearLayout) findViewById(R.id.button_camera);
        LinearLayout detectButton = (LinearLayout) findViewById(R.id.detect);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }

        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        buttonLoadCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(intent.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(intent, RESULT_TAKE_IMAGE);
                }
            }
        });

        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Bitmap bitmap = null;
                ImageView imageView = (ImageView) findViewById(R.id.image);
                try {
                    bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    bitmap = Bitmap.createScaledBitmap(bitmap, 244, 244, true);

                    Classifier classifier = new Classifier(Utils.assetFilePath(MainActivity.this, "mobilenet_traced_new.pt"));
                    String detected_class = classifier.makePrediction(bitmap);

                    TextView textView = findViewById(R.id.result_text);
                    textView.setText(detected_class);
                }
                catch(Exception e){
                    TextView textView = findViewById(R.id.result_text);
                    textView.setText("No image");
                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView textView = findViewById(R.id.result_text);
        textView.setText("Press the identify button");
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            imageView.setImageURI(null);
            imageView.setImageURI(selectedImage);
        }

        if (requestCode == RESULT_TAKE_IMAGE && resultCode == RESULT_OK){
            Bundle extra = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extra.get("data");
            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(imageBitmap);
        }

    }

}
