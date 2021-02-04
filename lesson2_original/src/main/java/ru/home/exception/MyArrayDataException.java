package ru.home.exception;

public class MyArrayDataException extends MyArrayException {

    private int x;
    private int y;
    private String value;

    public MyArrayDataException(Throwable e) {
        this.initCause(e);
    }


    public MyArrayDataException(int x, int y, String value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getValue() {
        return value;
    }


    @Override
    public String toString() {
        return "MyArrayDataException{" +
               "x=" + x +
               ", y=" + y +
               ", value='" + value + '\'' +
               '}';
    }

}
