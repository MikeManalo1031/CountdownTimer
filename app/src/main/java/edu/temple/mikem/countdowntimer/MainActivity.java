package edu.temple.mikem.countdowntimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends Activity {

    EditText timerEditText;
    TextView startButton;

    boolean paused = false;
    Object pauseLock = new Object();
    boolean finished = false;
    int state = 0;
    int countdownNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerEditText = (EditText) findViewById(R.id.timerTextView);
        startButton = (TextView) findViewById(R.id.startButton);

        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View view) {

                countdownNumber = Integer.parseInt(timerEditText.getText().toString());

                switch (state) {
                    case 0:
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                finished = false;
                                while (!finished) {
                                    for (int i = countdownNumber; i >= 0; i--) {

                                        synchronized (pauseLock) {
                                            while (paused) {
                                                try {
                                                    pauseLock.wait();
                                                } catch (InterruptedException e) {

                                                }
                                            }
                                        }

                                        // The line below throws an exception. You cannot interact with the
                                        // UI thread from a worker thread
                                        /* timerTextView.setText(String.valueOf(i)); */

                                        // Obtain a message from the global message pool
                                        Message msg = timerHandler.obtainMessage();

                                        // Assign a value to the 'what' variable. Could have also used
                                        // overloaded obtainMessage(int what) method
                                        msg.what = i;

                                        // Send message to the handler
                                        timerHandler.sendMessage(msg);

                                        Log.d("Thread timer value", String.valueOf(i));

                                        try {
                                            // Sleep for 1 second
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        };

                        // Start the thread
                        t.start();
                        startButton.setText(R.string.pause_label);
                        state = 1;
                        break;

                    case 1:
                        synchronized (pauseLock) {
                            paused = true;
                            state = 2;
                            Log.d("state:", "paused");
                            startButton.setText(R.string.resume_label);
                        }

                        break;

                    case 2:
                        synchronized (pauseLock) {
                            paused = false;
                            pauseLock.notifyAll();
                            Log.d("state:", "resumed");
                            startButton.setText(R.string.pause_label);
                            state = 1;
                        }

                        break;
                }


            }

            // Handler that will received and process messages in the UI thread
            Handler timerHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {

                    // Retrieve the message and update the textview
                    timerEditText.setText(String.valueOf(message.what));
                    if (message.what <= 0) {
                        startButton.setText(R.string.start_label);
                        finished = true;
                        state = 0;
                        Log.d("state:", "Start");
                        Toast.makeText(getApplicationContext(), "GO!!!", Toast.LENGTH_SHORT).show();
                    }
                    return false;

                }
            }

            );


        });
    }
}

