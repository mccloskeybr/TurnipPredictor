package com.wordpress.mccloskeydev.turnippredictor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout l_main;
    private RelativeLayout l_turnip;
    private RelativeLayout l_base;

    private GraphView graph;

    private int basePrice;
    private ArrayList<Integer> turnipPrices;

    private InputMethodManager imm;
    private EditText et_turnip;
    private EditText et_base;
    private TextView tv_patterns;
    private TextView tv_advice;

    private File saveFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadContents();

        File dir = getBaseContext().getExternalFilesDir(null);
        this.saveFile = new File(dir, "data.txt");

        loadData();

        loadGraph();
        updateAnalysis();

    }

    /**
     * loads and adjusts field variables to specific sizes
     */
    private void loadContents() {
        this.imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //setup layouts
        this.l_main = (RelativeLayout) findViewById(R.id.layout_main);
        this.l_turnip = (RelativeLayout) findViewById(R.id.layout_turnipPrice);
        this.l_base = (RelativeLayout) findViewById(R.id.layout_basePrice);

        this.l_main.setVisibility(View.VISIBLE);
        this.l_turnip.setVisibility(View.GONE);
        this.l_base.setVisibility(View.GONE);

        //set up graph
        this.graph = (GraphView) findViewById(R.id.graph);
        this.graph.getViewport().setXAxisBoundsManual(true);
        this.graph.getViewport().setMinX(0);
        this.graph.getViewport().setMaxX(5.5);

        this.graph.getGridLabelRenderer().setNumHorizontalLabels(12);
        this.graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        this.turnipPrices = new ArrayList<>();

        //setup edit texts
        this.et_turnip = (EditText) findViewById(R.id.et_addTurnipPrice);
        this.et_base = (EditText) findViewById(R.id.et_newBasePrice);

        //setup text views
        this.tv_patterns = (TextView) findViewById(R.id.tv_possiblePatternsContent);
        this.tv_advice = (TextView) findViewById(R.id.tv_advice);

    }

    /**
     * Saves the relative information to this.saveFile. Contents are:
     * 1: basePrice
     * 2: turnipPrices.get(0)
     * 3: turnipPrices.get(1)
     *    ...
     * n: turnipPrices.get(turnipPrices.size()-1)
     */
    private void saveData() {
        try {
            FileOutputStream stream = new FileOutputStream(saveFile);

            String toWrite = this.basePrice + "\n";
            stream.write(toWrite.getBytes());

            for (int i = 0; i < this.turnipPrices.size(); i++)
            {
                toWrite = "" + this.turnipPrices.get(i);
                stream.write(toWrite.getBytes());
                if (i < this.turnipPrices.size()-1)
                {
                    stream.write("\n".getBytes());
                }
            }
            stream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error saving!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * loads the data from saveFile and stores it in relative field variables
     */
    private void loadData() {
        this.basePrice = 0;

        try {
            FileInputStream stream = new FileInputStream(this.saveFile);

            int length = stream.available();
            byte[] contents = new byte[length];

            stream.read(contents);
            stream.close();

            String[] chunks = (new String(contents)).split("\n");

            if (chunks.length > 0)
                this.basePrice = Integer.parseInt(chunks[0]);

            if (chunks.length > 1) {
                for (int i = 1; i < chunks.length; i++)
                    this.turnipPrices.add(Integer.parseInt(chunks[i]));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error loading!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * loads the graph and resets all of its data points
     */
    private void loadGraph() {
        this.graph.removeAllSeries();

        LineGraphSeries<DataPoint> base_line = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> turnipLine = new LineGraphSeries<>();

        base_line.setColor(Color.rgb(148, 194, 255));

        turnipLine.setDrawDataPoints(true);
        turnipLine.setColor(Color.rgb(255, 135, 135));

        double x = 0;
        double x_offset = 0.5;

        for (int i = 0; i < 12; i++) {

            base_line.appendData(new DataPoint(x, this.basePrice), true, 12);
            if (i < this.turnipPrices.size())
                turnipLine.appendData(new DataPoint(x, this.turnipPrices.get(i)), true, this.turnipPrices.size());

            x += x_offset;
        }

        this.graph.addSeries(base_line);
        this.graph.addSeries(turnipLine);
    }

    /**
     * Switches to user input page for turnip price
     */
    public void onClick_switchToTurnip(View v) {
        this.l_main.setVisibility(View.GONE);
        this.l_turnip.setVisibility(View.VISIBLE);
        this.l_base.setVisibility(View.GONE);

        this.et_turnip.requestFocus();
        this.imm.showSoftInput(this.et_turnip, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Switches to user input page for base price
     */
    public void onClick_switchToBase(View v) {
        this.l_main.setVisibility(View.GONE);
        this.l_turnip.setVisibility(View.GONE);
        this.l_base.setVisibility(View.VISIBLE);

        this.et_base.setText("");
        this.et_base.requestFocus();
        this.imm.showSoftInput(this.et_base, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * resets all data variables
     */
    public void onClick_reset(View v) {
        this.basePrice = 0;
        if (this.turnipPrices.size() > 0) {
            this.turnipPrices.clear();

            saveData();
            loadGraph();
            updateAnalysis();
        }
    }

    /**
     * removes the most recent turnip price
     */
    public void onClick_deleteOne(View v) {
        if (this.turnipPrices.size() > 0) {
            this.turnipPrices.remove(this.turnipPrices.size() - 1);

            saveData();
            loadGraph();
            updateAnalysis();
        }
    }

    /**
     * Called after user has input a new turnip value
     */
    public void onClick_enterTurnipVal(View v) {
        try {
            int input = Integer.parseInt(this.et_turnip.getText().toString());
            this.turnipPrices.add(input);

            this.et_turnip.setText("");

            saveData();
            loadGraph();
            updateAnalysis();

            onClick_backToMain(v);
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, "Please enter an integer.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * called after user has input a new base value
     */
    public void onClick_enterBaseVal(View v) {
        try {
            int input = Integer.parseInt(this.et_base.getText().toString());
            this.basePrice = input;

            this.et_base.setText("");

            saveData();
            loadGraph();
            updateAnalysis();

            onClick_backToMain(v);
        }
        catch (Exception e) {
            Toast.makeText(MainActivity.this, "Please enter an integer.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clears edit text fields and switches to the main page
     */
    public void onClick_backToMain(View v) {
        this.et_base.setText("");
        this.et_turnip.setText("");

        this.l_main.setVisibility(View.VISIBLE);
        this.l_turnip.setVisibility(View.GONE);
        this.l_base.setVisibility(View.GONE);

        this.l_main.requestFocus();
        this.imm.hideSoftInputFromWindow(this.et_turnip.getWindowToken(), 0);
    }

    /**
     * determines likeliness of any one pattern being present given user input data
     */
    private void updateAnalysis() {
        boolean random = true;
        boolean small_spike = true;
        boolean large_spike = true;
        boolean decreasing = true;

        boolean buyNow = false;

        if (this.turnipPrices.size() > 1) {
            int startOfSpike = 0;

            for (int i = 1; i < this.turnipPrices.size(); i++) {
                if (this.turnipPrices.get(i) > this.turnipPrices.get(i - 1)) {
                    decreasing = false;
                    startOfSpike = i - 1;
                    break;
                }
            }

            if (decreasing && this.turnipPrices.size() >= 8) { //thursday afternoon latest possible start of spike
                random = false;
                small_spike = false;
                large_spike = false;
                buyNow = true;
            }
            else {
                int numDaysIncreasing = 1;
                for (int i = startOfSpike + 1; i < this.turnipPrices.size(); i++) {
                    if (this.turnipPrices.get(i) < this.turnipPrices.get(i - 1)) { //end of spike before 3/4 days = random
                        small_spike = false;
                        large_spike = false;
                        if (this.turnipPrices.get(this.turnipPrices.size() - 1) > this.basePrice)
                            buyNow = true;
                        break;
                    } else if (this.turnipPrices.get(i) >= 250 && numDaysIncreasing == 3) { //end of spike @ 3 days = large
                        random = false;
                        small_spike = false;
                        buyNow = true;
                        break;
                    } else if (this.turnipPrices.get(i) <= 200 && numDaysIncreasing == 3) { //spiking but no big spike @ 3 days = small
                        random = false;
                        large_spike = false;
                        if (i + 1 != this.turnipPrices.size())
                            buyNow = true;
                        break;
                    } else {
                        numDaysIncreasing++;
                    }
                }
            }
        }

        String toDisplay = "";
        if (decreasing)
            toDisplay += "Decreasing\n";
        if (random)
            toDisplay += "Random\n";
        if (small_spike)
            toDisplay += "Small Spike\n";
        if (large_spike)
            toDisplay += "Large Spike\n";

        this.tv_patterns.setText(toDisplay);

        if (buyNow || turnipPrices.size() == 12)
            toDisplay = "Sell your turnips now!";
        else
            toDisplay = "Do not sell your turnips yet.";

        this.tv_advice.setText(toDisplay);
    }
}
