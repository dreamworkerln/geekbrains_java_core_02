package ru.home.geekbrains;

import java.util.*;

public class PhoneBook implements Iterable<Map.Entry<String,PhoneEntry>> {

    private Map<String, PhoneEntry> map = new TreeMap<>();



    public void add(String name, String phone) {

        PhoneEntry pe = map.get(name);

        if (pe == null) {
            map.put(name, new PhoneEntry(name, phone));
        }
        else {
            pe.add(phone);
        }
    }

    public PhoneEntry get(String name) {

        return map.get(name);
    }

    @Override
    public Iterator<Map.Entry<String, PhoneEntry>> iterator() {

        return map.entrySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("PhoneBook\n{\n");

        for (Map.Entry<String, PhoneEntry> entry : map.entrySet()) {

            sb.append("\t");
            sb.append(entry.getValue()).append("\n");
        }
        sb.append("}");

        return sb.toString();


    }


}
