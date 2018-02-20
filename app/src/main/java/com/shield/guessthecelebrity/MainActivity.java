package com.shield.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> celebURLs = new ArrayList<>();
    private ArrayList<String> celebNames = new ArrayList<>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    ImageView image;
    Button button0;
    Button button1;
    Button button2;
    Button button3;


    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    public class DownloadTast extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    result += (char) data;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void generateNewQuestion(){

        try{
            Random random = new Random();
            chosenCeleb = random.nextInt(celebURLs.size());
            ImageDownloader imageDownloader = new ImageDownloader();
            Bitmap celebImage = imageDownloader.execute(celebURLs.get(chosenCeleb)).get();
            image.setImageBitmap(celebImage);

            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation = 0;
            for(int i=0; i<4; i++) {
                if(i==locationOfCorrectAnswer){
                    answers[i] = celebNames.get(chosenCeleb);
                }
                else {
                    incorrectAnswerLocation = random.nextInt(celebURLs.size());
                    while(incorrectAnswerLocation==chosenCeleb)
                        incorrectAnswerLocation = random.nextInt(celebNames.size());
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void celebChosen(View view){
        if(view.getTag().toString().equals(locationOfCorrectAnswer+"")){
            Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();
        }
        generateNewQuestion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        DownloadTast downloadTast = new DownloadTast();
        String result = null;

        try {
            result = downloadTast.execute("http://www.posh24.se/kandisar").get();
            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);
            while (m.find()) {
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);
            while (m.find()) {
                celebNames.add(m.group(1));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        generateNewQuestion();
    }
}
