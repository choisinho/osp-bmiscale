package app.bqlab.bmiscale;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class StatsActivity extends AppCompatActivity {

    //constants
    final int OPTION_HEIGHT = 1;
    final int OPTION_WEIGHT = 2;
    final int OPTION_BMI = 3;
    //variables
    int option;
    //objects
    SharedPreferences mHeightPref, mWeightPref;
    //layouts
    LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        resetLineChart();
    }

    @SuppressLint("SimpleDateFormat")
    private void init() {
        //initialing
        option = OPTION_HEIGHT;
        mHeightPref = getSharedPreferences("height", MODE_PRIVATE);
        mWeightPref = getSharedPreferences("weight", MODE_PRIVATE);
        //setting
        setTitle(getPreviousDay(0));
        setContentView(R.layout.activity_stats);
        ((RadioGroup) findViewById(R.id.stats_option)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup group, int checkedId) {
                option = checkedId;
                resetLineChart();
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String getPreviousDay(int ago) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -ago);
        return new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    private Date getPreviousDayByDate(int ago) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -ago);
        return calendar.getTime();
    }

    @SuppressLint("SimpleDateFormat")
    private void resetLineChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        String[] labels = new String[10];
        for (int i = 0; i < 10; i++) {
            Entry entry = null;
            labels[i] = new SimpleDateFormat("MM/dd").format(getPreviousDayByDate(10 - (i + 1)));
            if (option == OPTION_HEIGHT) {
                String day = getPreviousDay(10 - i);
                String height = mHeightPref.getString(day, "0");
                assert height != null;
                entry = new Entry(i, Integer.parseInt(height));
            } else if (option == OPTION_WEIGHT) {
                String day = getPreviousDay(10 - i);
                String weight = mWeightPref.getString(day, "0");
                assert weight != null;
                entry = new Entry(i, Integer.parseInt(weight));
            } else if (option == OPTION_BMI) {
                try {
                    String day = getPreviousDay(10 - i);
                    int height = Integer.parseInt(Objects.requireNonNull(mHeightPref.getString(day, "0")));
                    int weight = Integer.parseInt(Objects.requireNonNull(mWeightPref.getString(day, "0")));
                    float bmi = (float) weight / ((float) height * (float) height) * 10000f;
                    entry = new Entry(i, bmi);
                } catch (ArithmeticException e) {
                    entry = new Entry(Integer.parseInt(getPreviousDay(10 - i)), 0);
                }
            }
            entries.add(entry);
        }
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setFillAlpha(110);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        LineData data = new LineData(dataSets);
        mChart = findViewById(R.id.stats_chart);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setData(data);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new MyAxisValueFormatter(labels));
        xAxis.setLabelCount(9);
    }

    class MyAxisValueFormatter implements IAxisValueFormatter {
        private String[] values;

        MyAxisValueFormatter(String[] values) {
            this.values = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return values[(int) value];
        }
    }
}
