package com.just4fan.bilibilikit.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Just4fan on 2017/9/9.
 */

public class Item {
    char symbol;
    Map<String, Object> map = null;
    List array = null;
    int type;	//栈存放的数据类型 Object或Array
    public Item(int type) {
        this.type = type;
        if(type == JSON.TYPE_MAP) {
            symbol = '{';
            map = new HashMap<>();
        }
        else {
            symbol = '[';
            array = new ArrayList();
        }
    }
}