package kr.co.captv.pooq.checker;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView cpuUsage;
    private TextView memoryUsage;
    private BenchmarkingTask benchmarkingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToast("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cpuUsage = (TextView) findViewById(R.id.cpu_usage);
        memoryUsage = (TextView) findViewById(R.id.memory_usage);

        benchmarkingTask = new BenchmarkingTask();

        // Immediately start task
        benchmarkingTask.execute();
    }

    @Override
    protected void onStart() {
        showToast("onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        showToast("onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        showToast("onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        showToast("onStop");
        super.onStop();
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

    private class BenchmarkingTask extends AsyncTask<Void, Float, Float> {
        @Override
        protected void onPreExecute() {

        }


        @Override
        protected Float doInBackground(Void... params) {
            float totalCpuUsage = 0;
            while (!isCancelled()) {
                // CPU part
                totalCpuUsage = Cpu.getCpuUsage()[0];

                // Memory part


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
        protected void onProgressUpdate(Float... values) {
            cpuUsage.setText(values[0].toString());
        }

        @Override
        protected void onPostExecute(Float aFloat) {
            cpuUsage.setText(aFloat.toString());

        }

        @Override
        protected void onCancelled(Float aFloat) {
            cpuUsage.setText(aFloat.toString());
        }
    }
}
