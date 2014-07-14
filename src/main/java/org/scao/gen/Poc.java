package org.scao.gen;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by scao on 7/12/14.
 */
public class Poc {
  public static void main(String[] args) {
    String map = "java.util.Map<java.lang.String, java.util.Set>";
    String list = "java.util.List<com.rccl.esl.services.content.entities.v2.PortGeoLocationType>";
    String simple = "java.util.String";
    String primitive = "int";

    Pattern p1 = Pattern.compile("([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+)");
    Pattern p2 =
      Pattern.compile("([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+)\\s*<([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+)>");
    Pattern p3 = Pattern.compile(
      "([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+)\\s*<([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+),\\s*([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+)>");

    String toMatch = list;


    Matcher mapMapges = p1.matcher(toMatch);



    if (mapMapges.matches()) {
      System.out.println("p1");
      System.out.println(mapMapges.groupCount());
      for (int i = 1; i <= mapMapges.groupCount(); i++) {
        System.out.println(mapMapges.group(i));
      }
    }

    mapMapges = p2.matcher(toMatch);

    if (mapMapges.matches()) {
      System.out.println("p2");
      System.out.println(mapMapges.groupCount());
      for (int i = 1; i <= mapMapges.groupCount(); i++) {
        System.out.println(mapMapges.group(i));
      }
    }

    if (mapMapges.matches()) {
      System.out.println("p2");
      System.out.println(mapMapges.groupCount());
      for (int i = 1; i <= mapMapges.groupCount(); i++) {
        System.out.println(mapMapges.group(i));
      }
    }

    mapMapges = p3.matcher(
      toMatch);

    if (mapMapges.matches()) {
      System.out.println("p3");
      System.out.println(mapMapges.groupCount());
      for (int i = 1; i <= mapMapges.groupCount(); i++) {
        System.out.println(mapMapges.group(i));
      }
    }
  }
}
