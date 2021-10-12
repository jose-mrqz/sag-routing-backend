package pe.sag.routing.algorithm;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class InputParser {
    public int capacity;
    public int nTruck;
    public int smallestOrder;
    public ArrayList<ArrayList<Integer>> params;

    public InputParser() {
        this.capacity = 0;
        this.nTruck = 0;
        this.params = new ArrayList<>();
        this.smallestOrder = 100000;
    }

    public void readFile(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner reader = new Scanner(file);
        for (int i = 0; i < 4; i++) reader.nextLine();
        this.nTruck = reader.nextInt();
        this.capacity = reader.nextInt();
        for (int i = 0; i < 5; i++) reader.nextLine();
        while (reader.hasNextInt()) {
            ArrayList<Integer> args = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                int data = reader.nextInt();
                args.add(data);
                if (i == 3) {
                    if (data < smallestOrder && data != 0)
                        smallestOrder = data;
                }
            }
            reader.nextLine();
            params.add(args);
        }
        reader.close();
    }

    public int[][] getParams() {
        int[][] args = new int[params.size()][6];
        for (int i = 0; i < params.size(); i++) {
            ArrayList<Integer> data = params.get(i);
            args[i][0] = data.get(0);
            args[i][1] = data.get(1);
            args[i][2] = data.get(2);
            args[i][3] = data.get(3);
            args[i][4] = data.get(4);
            args[i][5] = data.get(5);
        }
        return args;
    }
}
