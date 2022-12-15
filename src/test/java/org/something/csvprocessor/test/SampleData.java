package org.something.csvprocessor.test;

import java.util.ArrayList;

public class SampleData {
  public static ArrayList<String> sampleInputData1() {
    ArrayList<String> data = new ArrayList<>();
    data.add("P|Carl Gustaf|Bernadotte");
    data.add("T|0768-101801|08-101801");
    data.add("A|Drottningholms slott|Stockholm|10001");
    data.add("F|Victoria|1977");
    data.add("A|Haga Slott|Stockholm|10002");
    data.add("F|Carl Philip|1979");
    data.add("T|0768-101802|08-101802");
    data.add("P|Barack|Obama");
    data.add("A|1600 Pennsylvania Avenue|Washington, D.C");
    return data;
  }

  public static ArrayList<String> sampleOutputData1() {
    ArrayList<String> data = new ArrayList<>();
    data.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    data.add("<people>");
    data.add("<person>");
    data.add("<firstname>Carl Gustaf</firstname>");
    data.add("<lastname>Bernadotte</lastname>");
    data.add("<phone>");
    data.add("<mobile>0768-101801</mobile>");
    data.add("<landline>08-101801</landline>");
    data.add("</phone>");
    data.add("<address>");
    data.add("<street>Drottningholms slott</street>");
    data.add("<city>Stockholm</city>");
    data.add("<zipcode>10001</zipcode>");
    data.add("</address>");
    data.add("<family>");
    data.add("<name>Victoria</name>");
    data.add("<born>1977</born>");
    data.add("<address>");
    data.add("<street>Haga Slott</street>");
    data.add("<city>Stockholm</city>");
    data.add("<zipcode>10002</zipcode>");
    data.add("</address>");
    data.add("</family>");
    data.add("<family>");
    data.add("<name>Carl Philip</name>");
    data.add("<born>1979</born>");
    data.add("<phone>");
    data.add("<mobile>0768-101802</mobile>");
    data.add("<landline>08-101802</landline>");
    data.add("</phone>");
    data.add("</family>");
    data.add("</person>");
    data.add("<person>");
    data.add("<firstname>Barack</firstname>");
    data.add("<lastname>Obama</lastname>");
    data.add("<address>");
    data.add("<street>1600 Pennsylvania Avenue</street>");
    data.add("<city>Washington, D.C</city>");
    data.add("</address>");
    data.add("</person>");
    data.add("</people>");
    return data;
  }
  public static ArrayList<String> sampleInputData2() {
    ArrayList<String> data = new ArrayList<>();
    data.add("P|Carl Gustaf|Bernadotte");
    data.add("T|0768-101801|08-101801");
    data.add("A|Drottningholms slott|Stockholm|10001");
    data.add("F|Victoria|1977");
    data.add("A|Haga Slott|Stockholm|10002");
    data.add("F|Carl Philip|1979");
    data.add("T|0768-101802|08-101802");
    return data;
  }

  public static ArrayList<String> sampleOutputData2() {
    ArrayList<String> data = new ArrayList<>();
    data.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    data.add("<people>");
    data.add("<person>");
    data.add("<firstname>Carl Gustaf</firstname>");
    data.add("<lastname>Bernadotte</lastname>");
    data.add("<phone>");
    data.add("<mobile>0768-101801</mobile>");
    data.add("<landline>08-101801</landline>");
    data.add("</phone>");
    data.add("<address>");
    data.add("<street>Drottningholms slott</street>");
    data.add("<city>Stockholm</city>");
    data.add("<zipcode>10001</zipcode>");
    data.add("</address>");
    data.add("<family>");
    data.add("<name>Victoria</name>");
    data.add("<born>1977</born>");
    data.add("<address>");
    data.add("<street>Haga Slott</street>");
    data.add("<city>Stockholm</city>");
    data.add("<zipcode>10002</zipcode>");
    data.add("</address>");
    data.add("</family>");
    data.add("<family>");
    data.add("<name>Carl Philip</name>");
    data.add("<born>1979</born>");
    data.add("<phone>");
    data.add("<mobile>0768-101802</mobile>");
    data.add("<landline>08-101802</landline>");
    data.add("</phone>");
    data.add("</family>");
    data.add("</person>");
    data.add("</people>");
    return data;
  }
  public static ArrayList<String> sampleInputData3() {
    ArrayList<String> data = new ArrayList<>();
    data.add("P|Barack|Obama");
    data.add("A|1600 Pennsylvania Avenue|Washington, D.C");
    return data;
  }

  public static ArrayList<String> sampleOutputData3() {
    ArrayList<String> data = new ArrayList<>();
    data.add("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    data.add("<people>");
    data.add("<person>");
    data.add("<firstname>Barack</firstname>");
    data.add("<lastname>Obama</lastname>");
    data.add("<address>");
    data.add("<street>1600 Pennsylvania Avenue</street>");
    data.add("<city>Washington, D.C</city>");
    data.add("</address>");
    data.add("</person>");
    data.add("</people>");
    return data;
  }
}
