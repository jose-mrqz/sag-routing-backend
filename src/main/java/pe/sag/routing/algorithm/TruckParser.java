package pe.sag.routing.algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TruckParser {
    int nTruck;
    ArrayList<Double[]> params;

    public TruckParser() {
        nTruck = 0;
        params = new ArrayList<>();
    }

    public void readFile(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] data = line.split(",");
            Double[] args = new Double[2];
            //capacidad total de glp
            args[0] = Double.parseDouble(data[0]);
            //peso tara
            args[1] = Double.parseDouble(data[1]);
            params.add(args);
        }
        nTruck = params.size();
        reader.close();
    }

    public Double[][] getParams() {
        Double[][] data = new Double[params.size()][2];
        int i = 0;
        for (Double[] record : params) {
            data[i] = record;
            i++;
        }
        return data;
    }
}
