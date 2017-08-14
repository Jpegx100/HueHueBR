package com.jpegx.huehuebr;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpegx on 8/11/17.
 */

public class Meme extends SugarRecord<Meme>{
    String tags;
    String path;

    public Meme(){}

    public Meme(String path, String tags){
        this.path = path;
        this.tags = tags;
    }
}
