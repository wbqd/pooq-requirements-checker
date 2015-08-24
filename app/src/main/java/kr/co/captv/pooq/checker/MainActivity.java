package kr.co.captv.pooq.checker;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private static final boolean DEBUG_MODE = false;
    private static final int CPU_LIMIT = 20;
    private static final int RAM_LIMIT = 120 * (int) Ram.BYTES_TO_KB;

    // TODO: Set proper NETWORK_LIMIT value
    private static final int NETWORK_LIMIT = 0;

    private TextView cpuUsageView;
    private TextView cpuStatusView;
    private TextView ramFreeView;
    private TextView ramStatusView;
    private BenchmarkingTask benchmarkingTask;

    private CardView cpuCard;

    private static void log(String msg) {
        if (DEBUG_MODE) {
            Log.d("DEBUG", msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cpuUsageView = (TextView) findViewById(R.id.cpu_usage);
        cpuStatusView = (TextView) findViewById(R.id.cpu_status);

        // Display total mem in static, because this value not vary.
        ((TextView) findViewById(R.id.ram_total)).setText(Ram.formatBytes(Ram.getTotalRam()));
        ramFreeView = (TextView) findViewById(R.id.ram_free);
        ramStatusView = (TextView) findViewById(R.id.ram_status);

        cpuCard = (CardView) findViewById(R.id.card_cpu);
        cpuCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_LONG).show();
            }
        });

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
        log("benchmarkingTask executing");
        benchmarkingTask = new BenchmarkingTask();
        benchmarkingTask.execute();
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
        benchmarkingTask.cancel(true);
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

    private class BenchmarkingTask extends AsyncTask<Void, Float, Void> {
        // TODO: Experimental trial: Does using field member is OK?
        float totalCpuUsage;
        int ramFree;

        @Override
        protected Void doInBackground(Void... params) {
//            float totalCpuUsage;
//            int ramFree;
            while (!isCancelled()) {
                log("doInBackground");

                // CPU part
                totalCpuUsage = Cpu.getCpuUsage();

                // Ram part
                ramFree = Ram.getFreeRam();

                // Publish part
                log("publishProgress");
                publishProgress(totalCpuUsage, (float) ramFree);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            log("onProgressUpdate");
            // TODO: Modify this method to accept float parameters

//            float totalCpuUsage = values[0];
//            int ramFree = values[1].intValue();

            cpuUsageView.setText(Cpu.formatPercent(totalCpuUsage));
            ramFreeView.setText(Ram.formatBytes(ramFree));
            cpuStatusView.setText(checkCpuStatus(totalCpuUsage));
            ramStatusView.setText(checkRamStatus(ramFree));
        }

        private String checkCpuStatus(float cpuUsage) {
            if (cpuUsage > CPU_LIMIT) {
                return getResources().getString(R.string.status_cpu_bad);
            } else {
                return getResources().getString(R.string.status_cpu_good);
            }
        }

        private String checkRamStatus(int ramFree) {
            // TODO: Beware to distinguish between Dalvik and ART!
            if (ramFree < RAM_LIMIT) {
                return getResources().getString(R.string.status_ram_bad);
            } else {
                return getResources().getString(R.string.status_ram_good);
            }
        }

        // TODO: Implement checkNetworkStatus method
        private String checkNetworkStatus() {
            return null;
        }
    }
}
