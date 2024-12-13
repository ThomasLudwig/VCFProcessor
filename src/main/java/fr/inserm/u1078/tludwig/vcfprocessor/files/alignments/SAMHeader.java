package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class SAMHeader {
  private final ArrayList<HeaderRecord> samHeaders;
  private final Reference[] references;

  private int byteLength = 0;

  public SAMHeader(String filename) throws IOException {
    this.samHeaders = new ArrayList<>();
    ArrayList<Reference> refs = new ArrayList<>();
    String line;
    UniversalReader in = new UniversalReader(filename);
    while((line = in.readLine()).startsWith("@")){
      samHeaders.add(new HeaderRecord(line));
      if(line.startsWith("@SQ")){
        String name = "";
        int length = -1;
        for(String field : line.split("\t")){
          String[] f = field.split(":");
          switch(f[0]){
            case "SN":
              name = f[1];
              break;
            case "LN":
              length = Integer.parseInt(f[1]);
              break;
          }
        }
        refs.add(new Reference(name, length));
      }
    }
    in.close();
    this.references = refs.toArray(new Reference[0]);
  }

  public SAMHeader(InputStream in) throws IOException {

    int nbBytes= ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    this.byteLength += 4;
    byte[] header = in.readNBytes(nbBytes);
    this.byteLength += nbBytes ;
    String hString = new String(header);
    this.samHeaders = new ArrayList<>();
    for(String line : hString.split("\n"))
      this.samHeaders.add(new HeaderRecord(line));

    int nRef = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    this.byteLength += 4;
    this.references = new Reference[nRef];
    for(int i = 0 ; i < nRef; i++) {
      int nLength = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      this.byteLength += 4;
      byte[] nByte = in.readNBytes(nLength);
      this.byteLength += nLength;
      String name = new String(nByte);
      name = name.substring(0,name.length() - 1);
      int length = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      this.byteLength += 4;
      this.references[i] = new Reference(name, length);
    }
  }

  public long getByteLength() {
    return byteLength;
  }

  public void addExtraHeaders(String[] extraHeaders) {
    for(String line : extraHeaders)
      this.samHeaders.add(new HeaderRecord(line));
  }

  public HeaderRecord[] getHeaderRecords() {
    return samHeaders.toArray(new HeaderRecord[0]);
  }

  public Reference[] getReferences() {
    return references;
  }

  public int getRefFor(String name){
    int chrNumber = Variant.chromToNumber(name);
    for(int i = 0 ; i < references.length; i++){
      if(Variant.chromToNumber(references[i].getName()) == chrNumber)
        return i;
    }
    return -1;
  }

  public static class Reference {
    private final String name;
    private final int length;

    public Reference(String name, int length) {
      this.name = name;
      this.length = length;
    }

    public String getName() {
      return name;
    }

    public int getLength() {
      return length;
    }
  }

  public static class HeaderRecord {
    private final String recordKey;
    private final ArrayList<Map.Entry<String, String>> keyValues;

    public HeaderRecord(String line){
      this.keyValues = new ArrayList<>();
      if(line == null){
        this.recordKey = null;
      } else {
        String[] f = line.split("\t");
        if(f[0].startsWith("@"))
          this.recordKey = f[0].substring(1);
        else{
          this.recordKey = f[0];
          Message.warning("Malformed SAM header ["+line+"]");
        }
        for(int i = 1 ; i < f.length; i++){
          this.keyValues.add(getKeyValue(f[1]));
        }
      }
    }

    public HeaderRecord(String recordKey, ArrayList<Map.Entry<String, String>> keyValues) {
      this.recordKey = recordKey;
      this.keyValues = keyValues;
    }

    private Map.Entry<String, String> getKeyValue(String s){
      String[] f = s.split(":");
      if(f.length == 1)
        return new AbstractMap.SimpleEntry<>(s, null);
      return new AbstractMap.SimpleEntry<>(f[0], f[1]);
    }

    @Override
    public String toString() {
      StringBuilder ret = new StringBuilder("@");
      ret.append(this.recordKey);
      for(Map.Entry<String, String> entry : this.keyValues)
        ret.append(entry.getKey()).append(":").append(entry.getValue());
      return ret.toString();
    }
  }
}
