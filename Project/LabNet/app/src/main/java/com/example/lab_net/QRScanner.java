package com.example.lab_net;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Objects;

// referenced from https://youtu.be/Fe7F4Jx7rwo
/**
 * An activity meant to scan QR codes and then go to the respective location.
 * Start this activity for a result and it will return
 * @author Marcus
 */
public class QRScanner extends AppCompatActivity {


    private static final String QR_RESULT_EXTRA = "com.example.lab_net.qrscanner.result_extra";
    private boolean resultMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    // basically: this activity listens for itself, and then returns the result of the QR scanner
    // to whatever activity happens to be listening to it.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        // putting this in a try catch cleans up the messy if logic.
        try {
            String s = result.getContents();
            Objects.requireNonNull(s);

            Toast.makeText(this, "Reading QR...",Toast.LENGTH_SHORT);
            handleResult(s);
        } catch (Exception e) { // too lazy to handle individual exceptions. Just catch them all
            Toast.makeText(this, "Error in reading QR:\n"
                    + e.getClass().getSimpleName(),Toast.LENGTH_LONG);
            setResult(AppCompatActivity.RESULT_CANCELED);
        }

        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    /**
     * Handles the result of a QR or barcode.
     * @param r The String result of the barcode
     */
    private void handleResult(String r) {
        String data = null;
        if(r.startsWith(QRManager.LAB_NET_PREPEND)) {
            data = r.substring(QRManager.LAB_NET_PREPEND.length());
        } else {
            data = r;
        }

        String[] pieces = data.split(QRManager.LAB_NET_JOIN);

        Intent resultIntent = new Intent();
        ArrayList<String> returnData = new ArrayList<>();
        for(String s : pieces)
            returnData.add(s);
        resultIntent.putStringArrayListExtra(QRScanner.QR_RESULT_EXTRA, returnData);
        setResult(AppCompatActivity.RESULT_OK,resultIntent);

        // todo: need support for parsing this raw QR data into a real command that the program
        // can execute
    }
}