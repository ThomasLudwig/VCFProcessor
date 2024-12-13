package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import java.util.ArrayList;

public class Cigar {
  public static final int M = 0;
  public static final int I = 1;
  public static final int D = 2;
  public static final int N = 3;
  public static final int S = 4;
  public static final int H = 5;
  public static final int P = 6;
  public static final int EQ = 7;
  public static final int X = 8;

  public static final char[] CIGAR = {'M','I','D','N','S','H','P','=','X'}; // 0 --> 8
  public static final byte INVALID_BYTE = (byte)0xFF;

  int[] lengths;
  byte[] types;

  public static byte getByte(char c){
    switch(c){
      case 'M':
        return (byte)M;
      case 'I':
        return (byte)I;
      case 'D':
        return (byte)D;
      case 'N':
        return (byte)N;
      case 'S':
        return (byte)S;
      case 'H':
        return (byte)H;
      case 'P':
        return (byte)P;
      case '=':
        return (byte)EQ;
      case 'X':
        return (byte)X;
      default :
        return INVALID_BYTE;
    }
  }

  public Cigar(final int[] lengths, final byte[] types) {
    this.lengths = lengths;
    this.types = types;
  }

  public Cigar(final ArrayList<Integer> lengths, final ArrayList<Byte> types) {
    this.lengths = new int [lengths.size()];
    for(int i = 0 ; i < this.lengths.length; i++)
      this.lengths[i] = lengths.get(i);
    this.types = new byte[types.size()];
    for(int i = 0 ; i < this.types.length; i++)
      this.types[i] = types.get(i);
  }

  /**
   * @return The number of reference bases that the read covers, excluding padding.
   */
  public int getReferenceLength() {
    int length = 0;
    for (int i = 0; i < types.length; i++) {
      switch (types[i]) {
        case M:
        case D:
        case N:
        case EQ:
        case X:
          length += lengths[i];
          break;
        default: break;
      }
    }
    return length;
  }

  @Override
  public String toString() {
    if(lengths.length == 0)
      return "*";
    final StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < lengths.length; i++){
      ret.append(lengths[i]);
      ret.append(CIGAR[types[i]]);
    }
    return ret.toString();
  }
}
