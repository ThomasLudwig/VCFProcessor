package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import fr.inserm.u1078.tludwig.maok.BgzipOutputStream;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileOutputer implements AutoCloseable {
  private static boolean FIRST_CALL_OUTPUT_BGZIPPED = true;
  private static boolean BGZIPPED_OUTPUT = false;

  private final String filename;
  private final PrintWriter out;

  public FileOutputer(String filename) throws IOException {
    this.filename = filename;
    this.out = getPrintWriter(filename);
  }

  public FileOutputer(String filename, boolean bgzip) throws IOException {
    this.filename = filename;
    this.out = getPrintWriter(filename, bgzip);
  }

  private static PrintWriter getPrintWriter(String filename) throws IOException {
    if(filename.endsWith(".gz"))
      return getPrintWriter(filename, true);
    return getPrintWriter(filename, BGZIPPED_OUTPUT);
  }

  private static PrintWriter getPrintWriter(String filename, boolean bgzip) throws IOException{
    if(bgzip)
      return new PrintWriter(new BgzipOutputStream(filename.endsWith(".gz") ? filename : filename + ".gz"));
    return new PrintWriter(new FileWriter(filename));
  }

  public static void setOutputBgzipped(){
    if(FIRST_CALL_OUTPUT_BGZIPPED)
      BGZIPPED_OUTPUT = true;
    else
      Message.die("Call to "+ Function.class.getSimpleName()+".setBgzippedOutput() can only made once");
    FIRST_CALL_OUTPUT_BGZIPPED = false;
  }

  public static boolean isOutputBgzipped(){
    return BGZIPPED_OUTPUT;
  }

  public void println(Println println){ out.println(println.toString()); }

  public String getFilename() { return filename; }

  @Override
  public void close() throws Exception {
    this.out.close();
  }
}
