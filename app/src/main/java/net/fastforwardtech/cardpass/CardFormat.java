package net.fastforwardtech.cardpass;

/**
 * Created by James on 1/19/2017.
 */

public class CardFormat {

    public String name;
    public int formatInt;
    public String formatValue;
    public ValueTypeEnum valueType;
    public int length;

    public CardFormat(String name, int formatInt, String formatValue, ValueTypeEnum valueType, int length){
        this.name = name;
        this.formatInt = formatInt;
        this.formatValue = formatValue;
        this.valueType = valueType;
        this.length = length;
    }

    @Override
    public String toString(){
        return name;
    }
}
