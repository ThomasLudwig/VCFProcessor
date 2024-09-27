package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BAMHeader {
  private final String[] samHeader;
  private final Reference[] references;

  public BAMHeader(InputStream in) throws IOException {
    int headerLength = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    byte[] header = in.readNBytes(headerLength);
    String hString = new String(header);
    this.samHeader = hString.split("\n");
    int nRef = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    this.references = new Reference[nRef];
    for(int i = 0 ; i < nRef; i++) {
      int nLength = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      byte[] nByte = in.readNBytes(nLength);
      String name = new String(nByte);
      name = name.substring(0,name.length() - 1);
      int length = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      this.references[i] = new Reference(name, length);
    }
  }

  public String[] getSamHeader() {
    return samHeader;
  }

  public Reference[] getReferences() {
    return references;
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
}
