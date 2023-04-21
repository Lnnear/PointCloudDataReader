package com.lnnear.pcd;

import org.ejml.simple.SimpleMatrix;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

public class PcdFileReader {
    public static PointCloudData readFile(String filepath) {
        File pcdFile = new File(filepath);
        if (!pcdFile.exists()) {
            return null;
        }

        PointCloudData pointCloudData = new PointCloudData();
        pointCloudData.data = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            br.mark(1000);

            String sCurrentLine;
            while (pointCloudData.datatype == null) {
                sCurrentLine = br.readLine();
                String[] words = sCurrentLine.trim().split(" ");
                String firstWord = words[0].toUpperCase();
                switch (firstWord) {
                    case "#":
                        break;
                    case "VERSION":
                        pointCloudData.version = Double.parseDouble(words[1]);
                        break;
                    case "FIELDS":
                        pointCloudData.fields = Arrays.copyOfRange(words, 1, words.length);
                        pointCloudData.colored = pointCloudData.fields[pointCloudData.fields.length - 1].equals("rgb");
                        break;
                    case "SIZE":
                        pointCloudData.size = StringArrayToIntArray(Arrays.copyOfRange(words, 1, words.length));
                        break;
                    case "TYPE":
                        pointCloudData.type = StringArrayToCharArray(Arrays.copyOfRange(words, 1, words.length));
                        break;
                    case "COUNT":
                        pointCloudData.count = StringArrayToIntArray(Arrays.copyOfRange(words, 1, words.length));
                        break;
                    case "WIDTH":
                        pointCloudData.width = Integer.parseInt(words[1]);
                        break;
                    case "HEIGHT":
                        pointCloudData.height = Integer.parseInt(words[1]);
                        break;
                    case "VIEWPOINT":
                        pointCloudData.viewpoint = StringArrayToIntArray(Arrays.copyOfRange(words, 1, words.length));
                        break;
                    case "POINTS":
                        pointCloudData.points = Integer.parseInt(words[1]);
                        break;
                    case "DATA":
                        pointCloudData.datatype = words[1];
                        if (pointCloudData.datatype.equals("ascii")) {
                            pointCloudData.data = new SimpleMatrix(pointCloudData.points, pointCloudData.fields.length);
                        } else if (pointCloudData.datatype.equals("binary")) {
                            pointCloudData.data = new SimpleMatrix(pointCloudData.points, pointCloudData.fields.length);
                        }
                        break;
                }
            }

            // Reading the data in ASCII.
            int currentLine = 0;
            if (Objects.equals(pointCloudData.datatype, "ascii")) {
                while ((sCurrentLine = br.readLine()) != null) {
                    String[] words = sCurrentLine.trim().split(" ");
                    float[] floatWords = StringArrayToFloatArray(words);
                    for (int i = 0; i < floatWords.length; i++) {
                        pointCloudData.data.setRow(currentLine, i, floatWords[i]);
                    }
                    currentLine++;
                }
                br.close();
                System.out.println("Number of points : " + pointCloudData.data.getNumElements());
            } else if (Objects.equals(pointCloudData.datatype, "binary")) {
                // Reading the data in binary. Need to start after the header.
                FileInputStream in = new FileInputStream(filepath);
                br.reset();
                String s = "";
                String tmpS = "";
                tmpS = br.readLine();

                while (tmpS.length() > 4 && !(tmpS.startsWith("DATA"))) {
                    s += tmpS + "\n";
                    tmpS = br.readLine();
                }

                s += tmpS + "\n";
                byte[] header = s.getBytes("UTF-8");
                in.read(header);

                int tmp = 0;
                currentLine = 0;
                int size = pointCloudData.size[0];
                byte[] b = new byte[size];
                int dimensionsAndRgb = pointCloudData.fields.length;

                while (in.read(b) != -1 && currentLine < pointCloudData.points) {
                    float value = 0f;
                    switch (pointCloudData.size[tmp]) {
                        case 2:
                            value = (float) ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
                            break;
                        case 4:
                            value = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            break;
                        case 8:
                            value = (float) ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getDouble();
                            break;
                    }
                    pointCloudData.data.set(currentLine, tmp, value);

                    if (tmp == dimensionsAndRgb - 1) {
                        tmp = -1;
                        currentLine++;
                    }
                    tmp++;
                    size = pointCloudData.size[tmp];
                    b = new byte[size];
                }
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pointCloudData;
    }

    private static int[] StringArrayToIntArray(String[] array) {
        int[] res = new int[array.length];
        for (int i = 0; i < array.length; i++)
            res[i] = Integer.parseInt(array[i]);
        return res;
    }

    private static char[] StringArrayToCharArray(String[] array) {
        char[] res = new char[array.length];
        for (int i = 0; i < array.length; i++)
            res[i] = array[i].charAt(0);
        return res;
    }

    private static float[] StringArrayToFloatArray(String[] array) {
        float[] res = new float[array.length];
        for (int i = 0; i < array.length; i++)
            res[i] = Float.parseFloat(array[i]);
        return res;
    }

}
