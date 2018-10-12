package net.fastforwardtech.cardpass.Graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.TextAlignment;
import org.krysalis.barcode4j.output.CanvasProvider;

import static android.support.constraint.Constraints.TAG;

public class AndroidCanvasProvider extends Picture implements CanvasProvider {

    private Bitmap bitmap;

    private BarcodeDimension dimension;

    public AndroidCanvasProvider()
    {
        bitmap = Bitmap.createBitmap(500, 100, Bitmap.Config.ALPHA_8);
    }

    @Override
    public void deviceCenteredText(String text, double x1, double x2, double y1, String fontName, double fontSize)
    {

    }

    @Override
    public void deviceFillRect(double x, double y, double w, double h)
    {
//        Log.w(TAG, "X: " + x + ", Y: " + y + ", width: " + w + ", height: " + h);

        x = (int)Math.round(x);
        y = (int)Math.round(y);
        w = (int)Math.round(w);
        h = (int)Math.round(h);

//        Log.w(TAG, "After rounding -- X: " + x + ", Y: " + y + ", width: " + w + ", height: " + h);

        // i is x coordinate, j is y coordinate
        for(int i = (int)x; i < (x + w); i++)
        {
            for(int j = (int)y; j < (y + h); j++)
            {
                bitmap.setPixel(i, j, Color.BLACK);
            }
        }
    }

    @Override
    public void deviceJustifiedText(String text, double x1, double x2, double y1, String fontName, double fontSize)
    {

    }

    @Override
    public void deviceText(String aText, double x1, double x2, double y1, String fontName, double fontSize, TextAlignment textAlign)
    {

    }

    @Override
    public void establishDimensions(BarcodeDimension dim)
    {
        Log.w(TAG, "Setting dimension width: " + dim.getWidth() + ", height: " + dim.getHeight());
        bitmap.setWidth((int)dim.getWidth() + 1);  // add 1 to make up for loss of precision
        bitmap.setHeight((int)dim.getHeight() + 1);  // add 1 to make up for loss of precision
        dimension = dim;
    }

    @Override
    public BarcodeDimension getDimensions()
    {
        return dimension;
    }

    @Override
    public int getOrientation()
    {
        return 0;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }
}
