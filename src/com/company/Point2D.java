package com.company;

import java.awt.*;

public class Point2D {
    double x, y;
    boolean inHull; //might need this in the convex hull finding algorithm
    Color color;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.inHull = false;
        this.color = Color.yellow;
    }

    //Returns the vector that stretches between this and other.
    public Vector subtract( Point2D other ) {
        return new Vector( this.x - other.x, this.y - other.y);
    }
}
