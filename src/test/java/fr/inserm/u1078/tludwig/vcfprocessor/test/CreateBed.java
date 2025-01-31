package fr.inserm.u1078.tludwig.vcfprocessor.test;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CreateBed {

  public static void main(String... args) {
    String dir = "C:\\Users\\Thomas Ludwig\\Documents\\Projet\\";
    try (PrintWriter o1 = new PrintWriter(new FileWriter(dir+"1.bed"))) {
      o1.println(new Region("chr1", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr2", 100, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr3", 100, 400, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr4", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr5", 100, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr6", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr7", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr8", 300, 400, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr9", 200, 400, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr10", 200, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr11", 100, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o1.println(new Region("chr12", 200, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try (PrintWriter o2 = new PrintWriter(new FileWriter(dir+"2.bed"))) {
      o2.println(new Region("chr1", 300, 400, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr2", 200, 400, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr3", 200, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr4", 100, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr5", 200, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr6", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr8", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr9", 100, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr10", 100, 400, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr11", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr12", 100, 300, Region.Format.FULL_1_BASED, "1").asBed(true));
      o2.println(new Region("chr13", 100, 200, Region.Format.FULL_1_BASED, "1").asBed(true));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
