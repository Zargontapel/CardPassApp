package net.fastforwardtech.cardpass;

import java.io.Serializable;

/**
 * Created by James on 1/21/2017.
 */

public class ScannedCard extends CardFormat implements Serializable{

    public String value;

    public ScannedCard(String name, int formatInt, String formatValue, ValueTypeEnum valueType, int length, String value) {
        super(name, formatInt, formatValue, valueType, length);
        this.value = value;
    }

    public ScannedCard(CardFormat format, String value){
        super(format.name, format.formatInt, format.formatValue, format.valueType, format.length);
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

    public String getformatValue()
    {
        return formatValue;
    }

    public void setformatValue(String aFormatValue)
    {
        formatValue = aFormatValue;
    }
}
