package com.example.l1va.imagerenderer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    CustomView customView;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        customView = (CustomView) findViewById(R.id.customView);
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            loadImage(intent.getData());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.save_image).setEnabled(!customView.isEmpty());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_image:
                Intent requestFileIntent = new Intent(Intent.ACTION_PICK);
                requestFileIntent.setType("image/*");
                startActivityForResult(requestFileIntent, 0);
                return true;
            case R.id.save_image:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    customView.saveToMedia();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        if (resultCode == RESULT_OK) {
            loadImage(returnIntent.getData());
        }
    }

    private void loadImage(Uri imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        customView.initBitmap(bitmap);

        try {
            ExifInterface exif = new ExifInterface(getRealPathFromURI(imageUri));
            float[] latLong = new float[2];
            boolean hasLatLong = exif.getLatLong(latLong);
            if (hasLatLong) {
                customView.setLatLong(latLong[0], latLong[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupFilter);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        radioGroup.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioBrightness:
                        customView.setBrightness(progress);
                        break;
                    case R.id.radioBlur:
                        customView.setBlur(progress);
                        break;
                    case R.id.radioExtract:
                        customView.setExtractRed(progress);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.radioBrightness:
                        customView.saveStep();
                        seekBar.setProgress(customView.getBrightness());
                        break;
                    case R.id.radioBlur:
                        customView.saveStep();
                        seekBar.setProgress(customView.getBlur());
                        break;
                    case R.id.radioExtract:
                        customView.saveStep();
                        seekBar.setProgress(customView.getExtractRed());
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                    customView.saveToMedia();
                    break;
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
