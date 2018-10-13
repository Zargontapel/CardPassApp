package net.fastforwardtech.cardpass;

import java.io.Serializable;

/**
 * Created by James on 1/21/2017.
 */

public class ScannedCard extends CardFormat implements Serializable{

    public String value;

    public ScannedCard(String name, int formatInt, int length, String value) {
        super(name, formatInt, length);
        this.value = value;
    }

    public ScannedCard(CardFormat format, String value){
        super(format.name, format.formatInt, format.length);
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }

    public int getFormat()
    {
        return formatInt;
    }

    public void setFormat(int aFormat)
    {
        formatInt = aFormat;
    }
}
