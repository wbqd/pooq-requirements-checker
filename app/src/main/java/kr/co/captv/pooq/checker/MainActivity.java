package kr.co.captv.pooq.checker;

import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
    private static final int RAM_LIMIT = 120 * (int) Utils.BYTES_TO_KB;
    private static final int API = Build.VERSION.SDK_INT;

    // TODO: Set proper NETWORK_LIMIT value
    private static final double NETWORK_LIMIT = 1.0;

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
    private boolean isDownloading;

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

        // Init field members
        downloadReference = -1;
        isDownloading = false;

        // CPU
        cpuUsageView = (TextView) findViewById(R.id.cpu_usage);
        cpuStatusView = (TextView) findViewById(R.id.cpu_status);

        // Display total RAM in static, because this value not vary.
        ((TextView) findViewById(R.id.ram_total)).setText(Utils.formatBytes(Ram.getTotalRam()));

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
                    case 404:
                        reasonText = "404 Not Found";
                        break;
                    default:
                        reasonText = "UNKNOWN";
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
                Uri downloadUri = Uri.parse(Constants.TEST_URL_1);
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                //Set the title of this download, to be displayed in notifications (if enabled).
                request.setTitle("Test");
                //Set a description of this download, to be displayed in notifications (if enabled)
                request.setDescription("Test Data download using DownloadManager.");
                //Set the local destination for the downloaded file to a path within the application's external files directory
                request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, "test");

//                if (API >= Build.VERSION_CODES.HONEYCOMB) {
//                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
//                } else {
//                    request.setShowRunningNotification(false);
//                }

                //Enqueue a new download and same the referenceId
                downloadReference = downloadManager.enqueue(request);
                isDownloading = true;

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
                cursor.close();
                break;

            case R.id.button_network_stop_download:
                downloadManager.remove(downloadReference);
                networkDownloadStatusView.setText("Download file canceled...");
                networkStartDownloadButton.setEnabled(true);
                networkQueryDownloadStatus.setEnabled(false);
                networkStopDownloadButton.setEnabled(false);
                isDownloading = false;
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

    private class BenchmarkingTask extends AsyncTask<Void, Void, Void> {
        // TODO: Experimental trial: Does using field member is OK?
        private float totalCpuUsage;
        private long ramFree;
        private DownloadManager.Query query;
        private long bytesDownloadedSoFarBefore;
        private long bytesDownloadedSoFarAfter;
        private long timeTagA;
        private long timeTagB;
        private double timeDiff;
        private double bytesDiff;
        private double bps;

        @Override
        protected void onPreExecute() {
            query = new DownloadManager.Query();
            bytesDownloadedSoFarBefore = 0;
            bytesDownloadedSoFarAfter = 0;
            timeTagA = 0;
            timeTagB = 0;
            timeDiff = 0;
            bytesDiff = 0;
            bps = 0;
        }

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

                // Network speed part
                if (isDownloading) {
                    // Set the query filter to our previously Enqueued download
                    query.setFilterById(downloadReference);

                    // Query the download manager about downloads that have been requested.
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        Trace.d("COLUMN_STATUS: " + status);
                        int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                        Trace.d("COLUMN_REASON: " + reason);

                        if (status == DownloadManager.STATUS_RUNNING) {
                            int columnBytesDownloadedSoFar = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                            bytesDownloadedSoFarBefore = cursor.getLong(columnBytesDownloadedSoFar);
                            timeTagA = System.currentTimeMillis();
                            Trace.d("bytes_downloaded: " + bytesDownloadedSoFarBefore);
                        }
                    }
                    cursor.close();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isDownloading) {
                    // Query the download manager about downloads that have been requested.
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        int columnStatusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(columnStatusIndex);

                        if (status == DownloadManager.STATUS_RUNNING) {
                            int columnBytesDownloadedSoFar = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                            bytesDownloadedSoFarAfter = cursor.getLong(columnBytesDownloadedSoFar);
                            timeTagB = System.currentTimeMillis();

                            timeDiff = timeTagB - timeTagA;
                            bytesDiff = bytesDownloadedSoFarAfter - bytesDownloadedSoFarBefore;

                            if (bytesDiff != 0) {
                                bps = (bytesDiff / (timeDiff / 1000));
                            }
                        }
                    }
                    cursor.close();
                }

                // Publish part
                log("publishProgress");
                publishProgress();

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            log("onProgressUpdate");
            // TODO: Modify this method to accept float parameters

//            float totalCpuUsage = values[0];
//            int ramFree = values[1].intValue();

            cpuUsageView.setText(Utils.formatPercent(totalCpuUsage));
            Trace.d(Utils.formatPercent(totalCpuUsage));
            cpuStatusView.setText(checkCpuStatus(totalCpuUsage));

            ramFreeView.setText(Utils.formatBytes(ramFree));
            Trace.d(Utils.formatBytes(ramFree));
            ramStatusView.setText(checkRamStatus(ramFree));

            networkSpeedView.setText(Utils.formatBytes((int) bps)+"/s");
        }

        private String checkCpuStatus(float cpuUsage) {
            if (cpuUsage > CPU_LIMIT) {
                return getResources().getString(R.string.status_cpu_danger);
            } else {
                return getResources().getString(R.string.status_cpu_good);
            }
        }

        private String checkRamStatus(long ramFree) {
            // TODO: Beware to distinguish between Dalvik and ART!
            if (ramFree < RAM_LIMIT) {
                return getResources().getString(R.string.status_ram_danger);
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
