/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fastforwardtech.cardpass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;
    private File file;
    private LinearLayout linearLayout;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);

        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        findViewById(R.id.read_barcode).setOnClickListener(this);

        String FILENAME = "card_inventory";

        Log.w(TAG, "Position 1");

        File directory = getFilesDir();
        file = new File(directory, FILENAME);
        if (!file.exists())
        {
            Log.w(TAG, "Position 2");
            try {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        FileInputStream fis;
        try {
            Log.w(TAG, "Position 3");
            ArrayList<ScannedCard> savedCards;
            fis = openFileInput(FILENAME);
            if(fis.available() > 0) {
                Log.w(TAG, "Position 4");
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    Log.w(TAG, "Position 5");
                    savedCards = (ArrayList<ScannedCard>)ois.readObject();
                    ois.close();

                    linearLayout = findViewById(R.id.barcode_cards);
                    for(int i = 0; i < savedCards.size(); i++)
                    {
                        ScannedCard card = savedCards.get(i);
                        Log.w(TAG, "Loaded value " + card.getValue());
                        TextView textView = new TextView(this);
                        textView.setText(card.name + ": " + card.getValue());
                        linearLayout.addView(textView);
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    final Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.rawValue);

                    //Deserialize the card format list
                    CardFormatParser parser = new CardFormatParser();
                    InputStream XmlFileInputStream = getResources().openRawResource(R.raw.cardformats); // getting XML
                    try {
                        //the type of value stored in barcode (e.g. alphanumeric, numeric, alpha-only)
                        ValueTypeEnum valueType;
                        try{
                            Long.parseLong(barcode.displayValue); //*WARNING* This cannot hold raw value greater than 2^63-1
                            //value is an integer
                            valueType = ValueTypeEnum.NUMERIC;
                        } catch (NumberFormatException e){
                            //value is not an integer
                            //so check to see if it contains any integers amongst the letters
                            e.printStackTrace();
                            if(barcode.displayValue.matches(".*\\d+.*")){
                                valueType = ValueTypeEnum.ALPHANUMERIC;
                            } else {
                                valueType = ValueTypeEnum.ALPHA;
                            }
                        }

                        Log.d(TAG, "Value Type: " + valueType);

                        //go through the list of possible formats and return the first one that matches
                        final CardFormat matchedFormat = parser.findInJsonStream(XmlFileInputStream, valueType, barcode.displayValue.length());

                        if(matchedFormat != null){

                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            // 2. Chain together various setter methods to set the dialog characteristics
                            String completeMessage = String.format("Is this a %s?", matchedFormat.name);
                            builder.setMessage(completeMessage)
                                    .setTitle(R.string.match_found);

                            // Add the buttons
                            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked OK button
                                    // Add gift card to inventory
                                    ScannedCard scannedCard = new ScannedCard(matchedFormat, barcode.displayValue);

                                    FileOutputStream fos;
                                    FileInputStream fis;
                                    Log.w(TAG, "Position 5");
                                    ArrayList<ScannedCard> scannedCards = new ArrayList<ScannedCard>();
                                    try {
                                        fis = new FileInputStream(file);
                                        Log.w(TAG, "Position 6");
                                        if(fis.available() > 0) {
                                            Log.w(TAG, "Position 7");
                                            ObjectInputStream ois = new ObjectInputStream(fis);
                                            try {
                                                scannedCards = (ArrayList<ScannedCard>)ois.readObject();
                                                Log.w(TAG, "Position 8");
                                                scannedCards.add(scannedCard);

                                            } catch (ClassNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else
                                        {
                                            scannedCards.add(scannedCard);
                                        }

                                        fos = new FileOutputStream(file);
                                        ObjectOutputStream out = new ObjectOutputStream(fos);
                                        out.writeObject(scannedCards);
                                        out.close();
                                        fos.close();

                                        Log.w(TAG, "Position 9");

                                        //tell the user it was successfully saved
                                        Context context = getApplicationContext();
                                        CharSequence text = "Card Successfully Saved";
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();

                                    } catch (IOException e){
                                        e.printStackTrace();
                                    }

                                }
                            });
                            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    // Maybe present user with list of options?
                                }
                            });

                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            Log.d(TAG, "No matching format found.");

                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage(R.string.no_match)
                                    .setTitle(R.string.error);

                            // Add the buttons
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked OK button
                                }
                            });

                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    Log.d(TAG, "Barcode raw value: " + barcode.rawValue);
                    Log.d(TAG, "Barcode format: " + barcode.format);
                    Log.d(TAG, "Barcode format value: " + barcode.valueFormat);
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
