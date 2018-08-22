package com.bonade.xxp.xqc_android_im.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Person implements Serializable{

    private String name;
    private String pinyin;

    public Person(String name, String pinyin) {
        this.name = name;
        this.pinyin = pinyin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    /***
     * 获取悬浮栏文本，（#、定位、热门 需要特殊处理）
     * @return
     */
    public String getSection(){
        if (TextUtils.isEmpty(pinyin)) {
            return "#";
        } else {
            String c = pinyin.substring(0, 1);
            Pattern p = Pattern.compile("[a-zA-Z]");
            Matcher m = p.matcher(c);
            if (m.matches()) {
                return c.toUpperCase();
            }
            //在添加定位和热门数据时设置的section就是‘定’、’热‘开头
            else if (TextUtils.equals(c, "我") || TextUtils.equals(c, "新") || TextUtils.equals(c, "群"))
                return pinyin;
            else
                return "#";
        }
    }

    public static List<Person> getAllPersons() {
        List<Person> people = new ArrayList<>();
        people.add(new Person("紫雪", "zixue"));
        people.add(new Person("惜玉", "xiyu"));
        people.add(new Person("昊伟", "haowei"));
        people.add(new Person("德泽", "deze"));
        people.add(new Person("飞昂", "feiang"));
        people.add(new Person("子昂", "ziang"));
        people.add(new Person("静珊", "jingshan"));
        people.add(new Person("半梅", "banmei"));
        people.add(new Person("平安", "pingan"));
        people.add(new Person("元冬", "yuandong"));
        people.add(new Person("听南", "tingnan"));
        people.add(new Person("香巧", "xiangqiao"));
        people.add(new Person("凡梦", "fanmeng"));
        people.add(new Person("碧菡", "bihan"));
        people.add(new Person("寄风", "jifeng"));
        people.add(new Person("冰真", "bingzhen"));
        people.add(new Person("思萱", "sixuan"));
        people.add(new Person("依波", "yibo"));
        people.add(new Person("曜文", "yaowen"));
        people.add(new Person("华晖", "huahui"));
        people.add(new Person("旭尧", "xurao"));
        people.add(new Person("运凯", "yunkai"));
        PersonComparator comparator = new PersonComparator();
        Collections.sort(people, comparator);
        return people;
    }

    /**
     * sort by a-z
     */
    private static class PersonComparator implements Comparator<Person> {
        @Override
        public int compare(Person lhs, Person rhs) {
            String a = lhs.getPinyin().substring(0, 1);
            String b = rhs.getPinyin().substring(0, 1);
            return a.compareTo(b);
        }
    }
}
