package com.charitymilescm.android;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.charitymilescm.android.Dialog.FinishDialog;
import com.charitymilescm.android.databinding.ActivityMainBinding;
import com.charitymilescm.android.location.GpsService;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnScrollChangeListener, View.OnClickListener, SensorEventListener {

    private ActivityMainBinding binding;
    private boolean isBlur = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isSensorPresent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setListener();
        setEmptyView();
        setChronometer();
        setMiles();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (null != mSensorManager && null != mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isSensorPresent = true;
        } else {
            isSensorPresent = false;
            Toast.makeText(this, "No Sensor Available", Toast.LENGTH_LONG).show();
        }
    }

    private void setListener() {
        binding.scrollView.setOnScrollChangeListener(this);
        binding.btnTopFinish.setOnClickListener(this);
        binding.btnFinish.setOnClickListener(this);
        binding.ivClick.setOnClickListener(this);
        binding.rlCamera.setOnClickListener(this);
    }

    private void setEmptyView() {
        binding.rlContent.post(new Runnable() {
            @Override
            public void run() {
                int rlContentHeight = binding.rlContent.getHeight();
                int windowHeight = binding.rlMainLayout.getHeight();
                int remainingHeight = windowHeight - rlContentHeight;
                binding.blank.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, remainingHeight));
            }
        });
    }

    private void setChronometer() {
        binding.chronometer.setBase(SystemClock.elapsedRealtime());
        binding.chronometer.start();
        binding.chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                chronometer.setText(String.format("%s:%s:%s", hh, mm, ss));
            }
        });
    }


    private void setMiles() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        } else {
            Intent intent = new Intent(MainActivity.this, GpsService.class);
            startService(intent);
        }
    }


    public void getDistanceRun(long steps) {
        if (steps == 0) {
            binding.tvMiles.setText(getResources().getString(R.string.default_0));
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("#.###");
            binding.tvMiles.setText(decimalFormat.format((steps * 0.76 * 0.000621371)));
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String distance = intent.getStringExtra("distance");
            getDistanceFromGps(distance);
        }
    };

    public void getDistanceFromGps(String distance) {
        double dis = Double.parseDouble(distance);
        if (dis == 0.0) {
            binding.tvMiles.setText(getResources().getString(R.string.default_0));
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("#.###");
            binding.tvMiles.setText(decimalFormat.format((dis * 0.000621371)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSensorPresent) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            registerReceiver(broadcastReceiver, new IntentFilter(GpsService.BROADCAST_ACTION));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (!isSensorPresent) {
            unregisterReceiver(broadcastReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, GpsService.class);
                startService(intent);
            } else {
                finish();
            }
        }
    }

    @Override
    public void onScrollChange(View view, int i, int i1, int i2, int i3) {
        int[] position = new int[2];
        binding.btnFinish.getLocationOnScreen(position);
        if (position[1] < 0) {
            binding.btnTopFinish.setVisibility(View.VISIBLE);
            binding.btnFinish.setVisibility(View.GONE);
        } else {
            binding.btnTopFinish.setVisibility(View.GONE);
            binding.btnFinish.setVisibility(View.VISIBLE);
        }

        int[] rlContentPosition = new int[2];
        binding.rlContent.getLocationOnScreen(rlContentPosition);
        if (rlContentPosition[1] < 912) {
            if (!isBlur) {
                isBlur = true;
                binding.imageView.setImageBitmap(BlurBuilder.blur(this, getBitmapFromView(binding.imageView)));
                binding.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        } else {
            isBlur = false;
            binding.imageView.setImageResource(R.drawable.bg);
            binding.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    public Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return bitmap;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFinish: {
                openDialog();
                break;
            }
            case R.id.btnTopFinish: {
                openDialog();
                break;
            }
            case R.id.ivClick: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://charitymiles.aftershokz.com/"));
                startActivity(intent);
                break;
            }
            case R.id.rlCamera: {

                Intent photo = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(photo, 1002);
                break;
            }
        }
    }

    private void openDialog() {
        new FinishDialog(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double miles = Double.parseDouble(binding.tvMiles.getText().toString());
                long time = (binding.chronometer.getDrawingTime() / (1000 * 60 * 60));
                DecimalFormat decimalFormat = new DecimalFormat("##.##");
                binding.tvMph.setText(decimalFormat.format(miles / time));
                binding.chronometer.stop();
            }
        }).show();
    }

    private long steps = 0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        float[] values = sensorEvent.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }
        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
        }
        getDistanceRun(steps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
