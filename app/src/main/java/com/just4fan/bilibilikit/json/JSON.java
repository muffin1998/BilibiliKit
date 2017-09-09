package com.just4fan.bilibilikit.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Just4fan on 2017/9/10.
 */

public class JSON {

    static final int TYPE_MAP = 0x0;
    static final int TYPE_ARRAY = 0x1;

    String json;
    int len;
    int pos;
    Stack<Item> stack;
    Map<String, Object> root;
    public JSON(String json) {
        this.json = json;
        len = json.length();
        pos = 0;
        stack = new Stack<>();
    }

    public Map<String, Object> getRoot() {
        return root;
    }

    public void parse() throws SyntaxException {
        int index = json.indexOf('{');
        if(index == -1)
            throw new SyntaxException();
        pos = index;
        parseObject(null);
    }

    private void parseObject(String p) throws SyntaxException {
        stack.push(new Item(TYPE_MAP));
        char c;
        ++pos;			//移动到起始字符 { 后一位
        while(pos < len &&
                (c = json.charAt(pos)) != '}') {
            if(c != '"') {
                ++pos;
                continue;
            }
            ++pos;
            StringBuffer key_sb = new StringBuffer();
            while((c = json.charAt(pos)) != '"') {
                key_sb.append(c);
                ++pos;
            }
            String key = new String(key_sb);
            ++pos;
            while((c = json.charAt(pos)) != '"' &&
                    c != '[' &&
                    c != '{' &&
                    (c < '0' || c > '9') &&
                    (c != 't' && c != 'f' && c != 'n')) {
                ++pos;
            }
            if(c == '"')			//字符串开始字符 “ ，注意结束字符 ”
                parseString(key);
            else if(c == '[')		//array开始字符 [ , 注意结束字符 ]
                parseArray(key);
            else if(c == '{')		//object开始字符 { , 注意结束字符 }
                parseObject(key);
            else if(c >= '0' && c <= '9')		//数字开始字符 ’0~9‘
                parseNumber(key);
            else if(c == 't' || c == 'f' || c == 'n')	//true、false、null 开始字符
                parseDetermine(key);
        }
        ++pos;
        if(stack.peek().symbol == getSymmetricalSymbol('}')) {
            Item cur = stack.pop();
            if(!stack.empty()) {
                Item parent = stack.peek();
                if(parent.type == JSON.TYPE_ARRAY)
                    parent.array.add(cur.map);
                else
                    parent.map.put(p, cur.map);
            }
            else
                root = cur.map;
        }
        else
            throw new SyntaxException();
    }

    private void parseArray(String key) throws SyntaxException {
        stack.push(new Item(TYPE_ARRAY));
        char c;
        ++pos;  //移动到起始符号 [ 后一位
        while(pos < len && (c = json.charAt(pos)) != ']') {
            if((c = json.charAt(pos)) != '[' &&
                    c != '{' &&
                    c != '"' &&
                    (c < '0' || c > '9') &&
                    (c != 't' && c != 'f' && c != 'n')) {
                ++pos;
                continue;
            }
            if(c == '"')			//字符串开始字符 “ ，注意结束字符 ”
                parseString(null);
            else if(c == '[')		//array开始字符 [ , 注意结束字符 ]
                parseArray(null);
            else if(c == '{')		//object开始字符 { , 注意结束字符 }
                parseObject(null);
            else if(c >= '0' && c <= '9')		//数字开始字符 ’0~9‘
                parseNumber(null);
            else if(c == 't' || c == 'f' || c == 'n')	//true、false、null 开始字符
                parseDetermine(null);
        }
        ++pos; 				//移动到结束字符 ] 后一位
        if(stack.peek().symbol == getSymmetricalSymbol(']')) {
            Item cur = stack.pop();
            if(!stack.empty()) {
                Item parent = stack.peek();
                if(parent.type == JSON.TYPE_ARRAY)
                    parent.array.add(cur.array);
                else
                    parent.map.put(key, cur.array);
            }
            else
                root = cur.map;
        }
        else
            throw new SyntaxException();
    }

    private void parseString(String key) {
        char c;
        ++pos;
        StringBuffer sb = new StringBuffer();
        while((c = json.charAt(pos)) != '"') {
            sb.append(c);
            ++pos;
        }
        String value = new String(sb);
        ++pos;  //移动到字符串结束符 " 后一位
        Item parent = stack.peek();
        if(parent.type == JSON.TYPE_MAP)
            parent.map.put(key, value);
        else
            parent.array.add(value);
    }

    private void parseNumber(String key) {
        char c;
        StringBuffer sb = new StringBuffer();
        while(((c = json.charAt(pos)) >= '0' &&
                c <= '9') ||
                c =='.') {
            sb.append(c);
            ++pos;
        }
        String s = new String(sb);
        if(s.contains(".")) {
            Double value = Double.parseDouble(s);
            Item parent = stack.peek();
            if(parent.type == JSON.TYPE_MAP)
                parent.map.put(key, value);
            else
                parent.array.add(value);
        }
        else {
            Long value = Long.parseLong(s);
            Item parent = stack.peek();
            if(parent.type == JSON.TYPE_MAP)
                parent.map.put(key, value);
            else
                parent.array.add(value);
        }
    }

    private void parseDetermine(String key) {
        char c;
        StringBuffer sb = new StringBuffer();
        while((c = json.charAt(pos)) >= 'a' &&
                c <= 'z') {
            sb.append(c);
            ++pos;
        }
        String s = new String(sb);
        Boolean value;
        if("true".equals(s))
            value = true;
        else if("false".equals(s))
            value = false;
        else
            value = null;
        Item parent = stack.peek();
        if(parent.type == JSON.TYPE_MAP)
            parent.map.put(key, value);
        else
            parent.array.add(value);
    }

    private char getSymmetricalSymbol(char c) {
        if(c == '}')
            return '{';
        else
            return '[';
    }
}