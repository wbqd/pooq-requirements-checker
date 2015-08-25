package kr.co.captv.pooq.checker;

import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private static final boolean DEBUG_MODE = false;
    private static final int CPU_LIMIT = 20;
    private static final int RAM_LIMIT = 120 * (int) Ram.BYTES_TO_KB;

    // TODO: Set proper NETWORK_LIMIT value
    private static final int NETWORK_LIMIT = 0;

    private TextView cpuUsageView;
    private TextView cpuStatusView;
    private TextView ramFreeView;
    private TextView ramStatusView;
    private TextView networkSpeedView;
    private TextView networkStatusView;
    private TextView networkDownloadStatusView;

    private BenchmarkingTask benchmarkingTask;

    private DownloadManager downloadManager;
    private long downloadReference;

    private Button networkStartDownloadButton;
    private Button networkStopDownloadButton;
    private Button networkShowDownloadButton;
    private AppCompatButton networkQueryDownloadStatus;

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

        downloadReference = -1;

        cpuUsageView = (TextView) findViewById(R.id.cpu_usage);
        cpuStatusView = (TextView) findViewById(R.id.cpu_status);

        // Display total mem in static, because this value not vary.
        ((TextView) findViewById(R.id.ram_total)).setText(Ram.formatBytes(Ram.getTotalRam()));
        ramFreeView = (TextView) findViewById(R.id.ram_free);
        ramStatusView = (TextView) findViewById(R.id.ram_status);

        // Find network associated views & button.
        networkSpeedView = (TextView) findViewById(R.id.network_speed);
        networkStatusView = (TextView) findViewById(R.id.network_status);
        networkDownloadStatusView = (TextView) findViewById(R.id.network_download_status);

        networkStartDownloadButton = (Button) findViewById(R.id.button_network_start_download);
        networkStartDownloadButton.setOnClickListener(this);

        networkStopDownloadButton = (Button) findViewById(R.id.button_network_stop_download);
        networkStopDownloadButton.setEnabled(false);
        networkStopDownloadButton.setOnClickListener(this);

        networkQueryDownloadStatus = (AppCompatButton) findViewById(R.id.button_network_query_download_status);
        networkQueryDownloadStatus.setEnabled(false);
        networkQueryDownloadStatus.setOnClickListener(this);

        networkShowDownloadButton = (Button) findViewById(R.id.button_network_show_download);
        networkShowDownloadButton.setOnClickListener(this);

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
        if (downloadManager != null && downloadReference != -1) {
            downloadManager.remove(downloadReference);
        }
        log("onStop complete");
    }

    @Override
    protected void onRestart() {
        log("onRestart start");
        super.onRestart();
        log("onRestart complete");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private void checkStatus(Cursor cursor) {
        //column for status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        //get the download filename
        int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        String filename = cursor.getString(filenameIndex);
        String statusText = "";
        String reasonText = "";

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                reasonText = "Filename:\n" + filename;
                break;
        }

        Toast toast = Toast.makeText(this, statusText + "\n" + reasonText, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 25, 400);
        toast.show();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_network_start_download:
                downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Uri downloadUri = Uri.parse("http://192.168.10.147/e.iso");
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                //Set the title of this download, to be displayed in notifications (if enabled).
                request.setTitle("Test");
                //Set a description of this download, to be displayed in notifications (if enabled)
                request.setDescription("Test Data download using DownloadManager.");
                //Set the local destination for the downloaded file to a path within the application's external files directory
                request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, "test");

                //Enqueue a new download and same the referenceId
                downloadReference = downloadManager.enqueue(request);

                networkDownloadStatusView.setText("Testing...");
                networkStopDownloadButton.setEnabled(true);
                networkQueryDownloadStatus.setEnabled(true);
                networkStartDownloadButton.setEnabled(false);
                break;

            case R.id.button_network_query_download_status:
                DownloadManager.Query query = new DownloadManager.Query();
                // Set the query filter to our previously Enqueued download
                query.setFilterById(downloadReference);

                // Query the download manager about downloads that have been requested.
                Cursor cursor = downloadManager.query(query);
                if (cursor.moveToFirst()) {
                    checkStatus(cursor);
                }
                break;

            case R.id.button_network_stop_download:
                downloadManager.remove(downloadReference);
                networkDownloadStatusView.setText("Download file canceled...");
                networkStartDownloadButton.setEnabled(true);
                networkQueryDownloadStatus.setEnabled(false);
                networkStopDownloadButton.setEnabled(false);
                break;

            case R.id.button_network_show_download:
                Intent intent = new Intent();
                intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                startActivity(intent);
                break;
            default:
                break;
        }
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
