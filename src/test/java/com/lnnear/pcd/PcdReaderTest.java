package com.lnnear.pcd;

/**
 * @author ZhaoYong
 */

public class PcdReaderTest {
    public static void main(String[] args) {
        PointCloudData pointCloudData = PcdFileReader.readFile("E:\\DocZcnest\\tag.pcd");
        System.out.println(pointCloudData.getPoints());
    }
}
