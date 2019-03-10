package ru.home.geekbrains;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class PhoneEntry implements Iterable<String> {

    private Set<String> set = new TreeSet<>();

    private String name;

    public PhoneEntry(String name, String phone) {
        this.name = name;
        set.add(phone);
    }

    public void add(String phone) {

        set.add(phone);
    }


    @Override
    public Iterator<String> iterator() {
        return set.iterator();
    }

    @Override
    public String toString() {
        return name + set.toString();
    }

}
