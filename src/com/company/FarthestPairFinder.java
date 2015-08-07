package com.company;

import javax.swing.JFrame;
import java.awt.*;
import java.util.Random;
import java.util.ArrayList;

public class FarthestPairFinder extends JFrame {
    int SCREEN_SIZE = 400;
    int HIGHEST = 200;
    int pointSize = 8;
    int numPoints = 50;
    int SCALING_FACTOR = SCREEN_SIZE/HIGHEST;
    int bruteforce_counter = 0;
    int convexHullCounter = 0;
    int efficient_counter = 0;
    int most_efficient_counter = 0;

    int point1=0,point2=1; // the index of the two farthest points, in the set S

    Point2D[] S = new Point2D[ numPoints ]; //the set S
    Point2D[] farthest_pair = new Point2D[2];
    Point2D[] bounds = new Point2D[2];


    ArrayList<Point2D> convexHull = new ArrayList(); //the vertices of the upper convex hull of S

    Color convexHullColour = Color.red;
    Color genericColour = Color.yellow;


    //fills S with random points
    public void makeRandomPoints() {
        Random rand = new Random();
        for (int i = 0; i < numPoints; i++) {
            double x = HIGHEST/10 + rand.nextDouble()*HIGHEST*8/10;
            double y = HIGHEST/10 + rand.nextDouble()*HIGHEST*8/10;
            S[i] = new Point2D( x, y );
        }
    }

    public void paint(Graphics g) {
        //draw the points in S
        g.setColor(genericColour);
        for (int i = 0; i < numPoints; i++) {
            g.fillOval((int) (S[i].x * SCALING_FACTOR - (pointSize / 2)), (int) (S[i].y * SCALING_FACTOR - (pointSize / 2)), pointSize, pointSize);
        }

        //recolour dots for convex hull
        g.setColor(convexHullColour);
        for (int i = 0; i < convexHull.size(); i++) {
            g.fillOval((int)(convexHull.get(i).x * SCALING_FACTOR - (pointSize / 2)), (int)(convexHull.get(i).y * SCALING_FACTOR - (pointSize / 2)), pointSize, pointSize);
        }

      /*  //brute force line
        g.setColor(Color.red);
        g.drawLine((int)S[point1].x*SCALING_FACTOR,(int)S[point1].y*SCALING_FACTOR,(int)S[point2].x*SCALING_FACTOR,(int)S[point2].y*SCALING_FACTOR);

        //most efficient line
        g.setColor(Color.MAGENTA);
        g.fillOval((int)(bounds[0].x * SCALING_FACTOR - (pointSize / 2)), (int)(bounds[0].y * SCALING_FACTOR - (pointSize / 2)), pointSize, pointSize);
        g.fillOval((int)(bounds[1].x * SCALING_FACTOR - (pointSize / 2)), (int)(bounds[1].y * SCALING_FACTOR - (pointSize / 2)), pointSize, pointSize);
        g.drawLine((int)farthest_pair[0].x*SCALING_FACTOR,(int)farthest_pair[0].y*SCALING_FACTOR,(int)farthest_pair[1].x*SCALING_FACTOR,(int)farthest_pair[1].y*SCALING_FACTOR);
*/
        //draw convex hull
        g.setColor(Color.white);
        for (int i = 0; i < convexHull.size()-1; i++) {
            g.drawLine((int)(convexHull.get(i).x*SCALING_FACTOR),(int)(convexHull.get(i).y*SCALING_FACTOR),(int)(convexHull.get(i+1).x*SCALING_FACTOR),(int)(convexHull.get(i+1).y*SCALING_FACTOR));
        }
        g.drawLine((int)(convexHull.get(0).x*SCALING_FACTOR),(int)(convexHull.get(0).y*SCALING_FACTOR),(int)(convexHull.get(convexHull.size()-1).x*SCALING_FACTOR),(int)(convexHull.get(convexHull.size()-1).y*SCALING_FACTOR));

    }

    public void findConvexHull() {
        convexHull.clear();

        // get the lowest y value in the set of S
        double lowest_y = HIGHEST;
        Point2D current_point = null;
        for (int i = 0; i < numPoints; i++) {
            if (S[i].y<lowest_y) {
                current_point = S[i];
                lowest_y = S[i].y;
            }
            // this case needs to be considered in case two points have the same y value
            // if we happen to get a lowest y value in the center, while another point has
            // has the same lowest y value in a corner, then when we check for the convex
            // hull, this middle point will never be returned to, and the while loop will never end.
            else if(S[i].y == lowest_y){
                if(S[i].x<current_point.x){
                    current_point=S[i];
                }
            }
        }
        // set default starting vector as a horizontal unit vector
        Vector currentVector = new Vector(-1,0);
        Vector new_vector = null, next_vector = null;
        double current_angle;
        Point2D next_point;
        int index;
        do{
            double lowest_angle = 3.2; // larger than pi
            convexHull.add(current_point);
            next_point = S[0];
            // iterate through all points in S and comparing the angle between the currentVector and the vector formed with each point
            for (int i = 0; i < numPoints; i++) {
                if(!S[i].inHull){
                    convexHullCounter++;
                    new_vector = S[i].subtract(current_point);
                    current_angle = currentVector.getAngle(new_vector);
                    // if we haven't changed the next point yet or if the S[i] forms a smaller angle, make next_point = S[i]
                    if(next_point == current_point || current_angle<lowest_angle){
                        next_point = S[i];
                        next_vector = new_vector;
                        lowest_angle = current_angle;
                    }
                }
            }
            // shift the perspective again
            currentVector = next_vector;
            current_point = next_point;
        }while(next_point!=convexHull.get(0));

    }
    public void findFarthestPair_MostEfficient(){
        double longest = 0;
        double highest = 0, lowest = HIGHEST;
        int highest_index =-1,lowest_index=-1; //troubleshooting value of -1
        // find highest and lowest value in the convex hull
        for (int i = 0; i < convexHull.size(); i++) {
            if(convexHull.get(i).y>highest) {
                highest_index = i;
                highest=convexHull.get(i).y;
            }
            if(convexHull.get(i).y<lowest) {
                lowest_index = i;
                lowest = convexHull.get(i).y;
            }
        }

        // stores the min and max y values for printing as magenta dots
        bounds[0] = convexHull.get(lowest_index);
        bounds[1] = convexHull.get(highest_index);

        //initializes two horizontal vectors
        Vector upper = new Vector(1,0);
        Vector lower = new Vector(-1,0);
        int current_top = highest_index, current_bot = lowest_index;
        boolean repeat_top=false,repeat_bot=false;
        do{
            most_efficient_counter++;
            // form two new vectors between the next dots in a counter clockwise direction on the convex hull.
            Vector new_upper = convexHull.get((current_top+1)%convexHull.size()).subtract(convexHull.get(current_top));
            Vector new_lower = convexHull.get((current_bot+1)%convexHull.size()).subtract(convexHull.get(current_bot));

            //compare the angle between the two new vectors and the two previous vectors
            double angle_top = new_upper.getAngle(upper);
            double angle_bot = new_lower.getAngle(lower);
            // adjust both vectors by the lower angle.
            // variables repeat bot and repeat top check to see if the rotating calipers have started repeating the loop
            if(angle_top>angle_bot){
                lower = new_lower;
                upper = new Vector(-lower.xComponent,-lower.yComponent);
                current_bot = (current_bot+1)%convexHull.size();
                if(current_bot==lowest_index){
                    repeat_bot = true;
                }
            }
            else if(angle_top==angle_bot){
                lower = new_lower;
                upper = new Vector(-lower.xComponent,-lower.yComponent);
                current_top= (current_top+1)%convexHull.size();
                current_bot = (current_bot+1)%convexHull.size();
                if(current_bot==lowest_index){
                    repeat_bot = true;
                }
                if(current_top==highest_index){
                    repeat_top = true;
                }
            }
            else{
                upper = new_upper;
                lower = new Vector(-upper.xComponent, -upper.yComponent);
                current_top= (current_top+1)%convexHull.size();
                if(current_top==highest_index){
                    repeat_top = true;
                }
            }
            // check the distance between the two starting points of our current vectors
            double dist = Math.sqrt(Math.pow(convexHull.get(current_top).x-convexHull.get(current_bot).x,2)+Math.pow(convexHull.get(current_top).y-convexHull.get(current_bot).y,2));
            if(dist>longest){
                longest=dist;
                farthest_pair[0]=convexHull.get(current_top);
                farthest_pair[1]=convexHull.get(current_bot);
            }
        }while(!(repeat_top&&repeat_bot));
        System.out.println("Using Convex Hull + Rotating Calipers, the longest distance calculated is "+longest + " in " + convexHullCounter + "+" +most_efficient_counter + " iterations.");
    }
    public void findFarthestPair_MoreEfficient() {
        double longest = 0;
        // bruteforce to find longest two points in the convex hull
        for (int i = 0; i < convexHull.size()-1; i++) {
            for (int j = i+1; j < convexHull.size(); j++) {
                efficient_counter++;
                double calc = Math.sqrt(Math.pow(convexHull.get(j).x-convexHull.get(i).x,2)+Math.pow(convexHull.get(j).y-convexHull.get(i).y,2));
                if (calc>longest) {
                    longest = calc;
                    point1=i;
                    point2=j;
                }
            }
        }
        System.out.println("Using Convex Hull + Bruteforce, the longest distance calculated is "+longest + " in " + convexHullCounter+"+"+efficient_counter + " iterations.");
    }

    // absolute brute force method, checking all distances between all points
    public void findFarthestPair_BruteForceWay() {
        double longest = 0;
        for (int i = 0; i < numPoints-1; i++) {
            for (int j = i+1; j < numPoints; j++) {
                bruteforce_counter++;
                double calc = Math.sqrt(Math.pow(S[j].x-S[i].x,2)+Math.pow(S[j].y-S[i].y,2));
                if (calc>longest) {
                    longest = calc;
                    point1=i;
                    point2=j;
                }
            }
        }
        System.out.println("Uaing Bruteforce, the longest distance calculated is " + longest+" in " + bruteforce_counter + " iterations.");
    }


    public static void main(String[] args) {

        FarthestPairFinder fpf = new FarthestPairFinder();

        fpf.setBackground(Color.BLACK);
        fpf.setSize(fpf.SCREEN_SIZE, fpf.SCREEN_SIZE);
        fpf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fpf.setUndecorated(true);

        fpf.makeRandomPoints();
        fpf.findConvexHull();
        fpf.findFarthestPair_MostEfficient();
        fpf.findFarthestPair_MoreEfficient();
        fpf.findFarthestPair_BruteForceWay();

        fpf.setVisible(true);
    }
}
