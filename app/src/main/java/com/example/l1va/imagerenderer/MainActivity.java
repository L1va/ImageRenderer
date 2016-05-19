package com.example.l1va.imagerenderer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    CustomView customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_image:
                Intent requestFileIntent = new Intent(Intent.ACTION_PICK);
                requestFileIntent.setType("image/*");
                startActivityForResult(requestFileIntent, 0);
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

        customView = (CustomView) findViewById(R.id.customView);
        customView.initBitmap(bitmap);

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
}
