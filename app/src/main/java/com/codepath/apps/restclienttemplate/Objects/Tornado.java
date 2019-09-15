package com.codepath.apps.restclienttemplate.Objects;

public class Tornado {
    public String CELL_TYPE;
    public String SHAPE;
    public String MAX_SHEAR;
    public String WSR_ID;
    public String MXDV;
    public String CELL_ID;
    public String ZTIME;
    public String AZIMUTH;
    public String RANGE;
    public double latitude;
    public double longitude;

    public void fixCoordinates() {
        String shape = this.SHAPE;
        int first = shape.indexOf(" ");
        int last = shape.lastIndexOf(" ");
        double longit = Double.parseDouble(shape.substring(first+2, last));
        double latit = Double.parseDouble(shape.substring(last+1, shape.length()-1));
//        System.out.println(latit);
        this.latitude = latit;
        this.longitude = longit;
    };
};

