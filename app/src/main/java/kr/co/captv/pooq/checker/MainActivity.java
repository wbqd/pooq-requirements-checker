package kr.co.captv.pooq.checker;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView cpuUsage;
    private TextView memoryUsage;
    private BenchmarkingTask benchmarkingTask;

    private static int log(String msg) {
        return Log.d("DEBUG", msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cpuUsage = (TextView) findViewById(R.id.cpu_usage);
        memoryUsage = (TextView) findViewById(R.id.memory_usage);

        log("onCreate complete");
    }

    @Override
    protected void onStart() {
        log("onStart start");

        super.onStart();

        log("onStart complete");
    }

    @Override
    protected void onResume() {
        log("onResume start");
        super.onResume();
        if (benchmarkingTask == null) {
            log("benchmarkingTask executing");
            benchmarkingTask = new BenchmarkingTask();
            benchmarkingTask.execute();
        }
        log("onResume complete");
    }

    @Override
    protected void onPause() {
        log("onPause start");
        super.onPause();
        log("onPause complete");
    }

    @Override
    protected void onStop() {
        log("onStop start");
        super.onStop();
        log("stop benchmarkingTask");
        benchmarkingTask = null;
        log("onStop complete");
    }

    @Override
    protected void onRestart() {
        log("onRestart start");
        super.onRestart();
        log("onRestart complete");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private class BenchmarkingTask extends AsyncTask<Void, String, String> {
        @Override
        protected void onPreExecute() {

        }


        @Override
        protected String doInBackground(Void... params) {
            String totalCpuUsage = "";
            while (!isCancelled()) {
                // CPU part
                totalCpuUsage = Cpu.getCpuUsage();

                // Memory part
//                Log.d("DEBUG", Memory.getTotalMemory());


                // Publish part
                publishProgress(totalCpuUsage);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return totalCpuUsage;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            cpuUsage.setText(values[0]);
        }

        @Override
        protected void onPostExecute(String values) {
            cpuUsage.setText(values);

        }

        @Override
        protected void onCancelled(String values) {
            cpuUsage.setText(values.toString());
        }
    }
}
