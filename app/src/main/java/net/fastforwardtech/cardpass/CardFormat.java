package net.fastforwardtech.cardpass;

import java.io.Serializable;

/**
 * Created by James on 1/19/2017.
 */

public class CardFormat implements Serializable {

    public String name;
    public int formatInt;
    public int length;

    public CardFormat(String name, int formatInt, int length){
        this.name = name;
        this.formatInt = formatInt;
        this.length = length;
    }

    @Override
    public String toString(){
        return name;
    }
}
