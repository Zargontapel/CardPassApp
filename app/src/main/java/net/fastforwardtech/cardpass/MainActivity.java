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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import net.fastforwardtech.cardpass.Graphics.AndroidCanvasProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.tools.UnitConv;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private CompoundButton useFlash;
    private File file;
    private LinearLayout linearLayout;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        linearLayout = findViewById(R.id.barcode_cards);

        findViewById(R.id.read_barcode).setOnClickListener(this);

        String FILENAME = "card_inventory";

        File directory = getFilesDir();
        file = new File(directory, FILENAME);
        if (!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        FileInputStream fis;
        try {
            ArrayList<ScannedCard> savedCards;
            fis = openFileInput(FILENAME);
            if(fis.available() > 0) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    savedCards = (ArrayList<ScannedCard>)ois.readObject();
                    ois.close();

                    for(int i = 0; i < savedCards.size(); i++)
                    {
                        ScannedCard card = savedCards.get(i);

                        addBarcodeDisplay(card);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void addBarcodeDisplay(ScannedCard card){
        try {

            AbstractBarcodeBean bean;
            switch (card.getFormat())
            {
                case Barcode.CODE_128:  // code 128
                    bean = new Code128Bean();
                    break;

                case Barcode.CODABAR:
                    bean = new CodabarBean();
                    break;

                case Barcode.CODE_39:
                    bean = new Code39Bean();
                    break;

                case Barcode.DATA_MATRIX:
                    bean = new DataMatrixBean();
                    break;

                case Barcode.EAN_13:
                    bean = new EAN13Bean();
                    break;

                case Barcode.EAN_8:
                    bean = new EAN8Bean();
                    break;

                case Barcode.ITF:
                    bean = new Interleaved2Of5Bean();
                    break;

                case Barcode.PDF417:
                    bean = new PDF417Bean();
                    break;

                case Barcode.UPC_A:
                    bean = new UPCABean();
                    break;

                case Barcode.UPC_E:
                    bean = new UPCEBean();
                    break;

                default:
                    Log.w(TAG, "Unrecognized card format. Aborting");
                    Log.w(TAG, "Format int: " + card.formatInt);
                    Log.w(TAG, "Value: " + card.value);
                    return;
            }

            final int dpi = 200;


            // Configure the barcode generator
            bean.setModuleWidth(UnitConv.in2mm(8.0f / dpi));  // makes a dot/module exactly eight pixels
            bean.doQuietZone(false);

            try {
                AndroidCanvasProvider canvas = new AndroidCanvasProvider();

                bean.generateBarcode(canvas, card.getValue());

                Bitmap bitmap = canvas.getBitmap();

                // scale it to 3x
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 5, bitmap.getHeight() * 5, false);

                CardView cView = (CardView) getLayoutInflater().inflate(R.layout.barcode_view, linearLayout, false);

                ImageView iView = cView.findViewById(R.id.card_image);

                iView.setImageBitmap(resizedBitmap);

                TextView tView = cView.findViewById(R.id.card_text);

                tView.setText(card.name + ": " + card.value);

                linearLayout.addView(cView);

            } finally {
            }
        } catch (Exception e) {
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

                    //tell the user it was successfully saved
                    Context context = getApplicationContext();
                    CharSequence text = "Barcode read successfully";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("New Card");

                            final EditText input = new EditText(this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);

                            builder.setView(input);

                            // 2. Chain together various setter methods to set the dialog characteristics
                            String completeMessage = String.format("What would you like to name this card?");
                            builder.setMessage(completeMessage)
                                    .setTitle(R.string.match_found);

                            // Add the buttons
                            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked OK button
                                    // Add gift card to inventory
                                    ScannedCard scannedCard = new ScannedCard(input.getText().toString(), barcode.format, barcode.rawValue.length(), barcode.rawValue);

                                    addBarcodeDisplay(scannedCard);

                                    FileOutputStream fos;
                                    FileInputStream fis;
                                    ArrayList<ScannedCard> scannedCards = new ArrayList<ScannedCard>();
                                    try {
                                        fis = new FileInputStream(file);
                                        if(fis.available() > 0) {
                                            ObjectInputStream ois = new ObjectInputStream(fis);
                                            try {
                                                scannedCards = (ArrayList<ScannedCard>)ois.readObject();
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
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    // Maybe present user with list of options?
                                }
                            });

                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();


                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    Log.d(TAG, "Barcode raw value: " + barcode.rawValue);
                    Log.d(TAG, "Barcode format: " + barcode.format);
                    Log.d(TAG, "Barcode format value: " + barcode.valueFormat);
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                //tell the user it was successfully saved
                Context context = getApplicationContext();
                CharSequence text = String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode));
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
