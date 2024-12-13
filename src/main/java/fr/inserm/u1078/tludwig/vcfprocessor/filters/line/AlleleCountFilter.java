package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.MinMaxGroupParser;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;

import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class AlleleCountFilter extends LineFilter {

  private final int maxAC;
  private final int minAC;
  private final MinMaxGroupParser parser;

  public static final int TYPE_MINOR = 1;
  public static final int TYPE_REF = 2;
  public static final int TYPE_NON_REF_ALL = 3;
  public static final int TYPE_NON_REF_ANY = 4;

  private final int type;

  private AlleleCountFilter(int minAC, int maxAC, MinMaxGroupParser parser, int type) {
    super(true);
    this.minAC = minAC;
    this.maxAC = maxAC;
    this.parser = parser;
    this.type = type;
  }

  public AlleleCountFilter(int minAC, int maxAC, int type) {
    this(minAC, maxAC, null, type);
  }

  public AlleleCountFilter(MinMaxGroupParser parser, int type) {
    this(0, Integer.MAX_VALUE, parser, type);
  }

  @Override
  public boolean pass(VariantRecord record) {
    if (this.parser == null) { //non group mode
      int[] acs = record.getAllACs();

      switch (type) {
        case TYPE_MINOR://look at minor among ref,alt1,alt2...,altN
          int minor = acs[0];
          for (int a = 1; a < acs.length; a++)
            minor = Math.min(minor, acs[a]);
          return (minAC <= minor && minor <= maxAC);
        case TYPE_REF://look at allele 0 (ref)
          return (minAC <= acs[0] && acs[0] <= maxAC);
        case TYPE_NON_REF_ALL://all alt are between min and max
          for (int a = 1; a < acs.length; a++)
            if (!(minAC <= acs[a] && acs[a] <= maxAC))
              return false;
          return true;
        case TYPE_NON_REF_ANY://any alt is between min and max
          for (int a = 1; a < acs.length; a++)
            if (minAC <= acs[a] && acs[a] <= maxAC)
              return true;
          return false;
      }
    } else {
      ArrayList<String> groups = parser.getGroups();
      switch (type) {
        case TYPE_MINOR:
          //First identify the minor, then reject if one group is not within bound for the minor

          int[] tacs = new int[record.getAltString().split(",").length + 1];
          int[][] gacs = new int[parser.getGroups().size()][tacs.length];
          for (int g = 0; g < groups.size(); g++) {
            String group = groups.get(g);
            ArrayList<Integer> indices = parser.getIndices(group);

            for (int i : indices) {
              int[] alleles = Genotype.getAlleles(record.getGT(i));
              if(alleles != null)
                for(int a : alleles) {
                  tacs[a]++;
                  gacs[g][a]++;
                }
            }
          }

          int minorC = tacs[0];
          int minorA = 0;
          for (int a = 1; a < tacs.length; a++)
            if (tacs[a] < minorC) {
              minorC = tacs[a];
              minorA = a;
            }

          for (int g = 0; g < groups.size(); g++) {
            String group = groups.get(g);
            int ac = gacs[g][minorA];
            int gMinAC = parser.getMin(group);
            int gMaxAC = parser.getMax(group);
            if (!(gMinAC <= ac && ac <= gMaxAC))
              return false;
          }
          return true;
        case TYPE_REF:
          for (String group : parser.getGroups()) {
            ArrayList<Integer> indices = parser.getIndices(group);
            int gMinAC = parser.getMin(group);
            int gMaxAC = parser.getMax(group);
            int ac = 0;
            for (int i : indices) {
              int[] alleles = Genotype.getAlleles(record.getGT(i));
              if(alleles != null)
                for(int a : alleles)
                  if(a == 0)
                    ac++;
            }
            if (!(gMinAC <= ac && ac <= gMaxAC))
              return false;
          }
          return true;
        case TYPE_NON_REF_ALL:
          for (String group : parser.getGroups()) {
            ArrayList<Integer> indices = parser.getIndices(group);
            int gMinAC = parser.getMin(group);
            int gMaxAC = parser.getMax(group);
            int[] acs = new int[record.getAltString().split(",").length + 1];

            for (int i : indices) {
              int[] alleles = Genotype.getAlleles(record.getGT(i));
              if(alleles != null)
                for(int a : alleles)
                  acs[a]++;
            }

            for (int a = 1; a < acs.length; a++)
              if (!(gMinAC <= acs[a] && acs[a] <= gMaxAC))
                return false;
          }
          return true;
        case TYPE_NON_REF_ANY:
          boolean[] any = new boolean[record.getAltString().split(",").length + 1];
          for (int a = 1; a < any.length; a++)
            any[a] = true;

          for (String group : parser.getGroups()) {
            ArrayList<Integer> indices = parser.getIndices(group);
            int gMinAC = parser.getMin(group);
            int gMaxAC = parser.getMax(group);
            int[] acs = new int[any.length];

            for (int i : indices) {
              int[] alleles = Genotype.getAlleles(record.getGT(i));
              if(alleles != null)
                for(int a : alleles)
                  acs[a]++;
            }

            for (int a = 1; a < acs.length; a++)
              if (!(gMinAC <= acs[a] && acs[a] <= gMaxAC))
                any[a] = false;
          }
          for (int a = 1; a < any.length; a++)
            if (any[a])
              return true;
          return false;
      }
    }
    return false;
  }

  @Override
  public boolean leftColumnsOnly() {
    return false;
  }

  @Override
  public String getDetails() {
    if(this.parser == null)
      return getDetailsForMinMax();
    return getDetailsForGroup();
    
  }
  
  private String typeString(){
    switch(type) {
      case TYPE_MINOR : 
        return "MinorAC";
      case TYPE_REF : 
        return "RefAC";
      case TYPE_NON_REF_ALL : 
        return "AllNonRefAC";
      case TYPE_NON_REF_ANY : 
        return "AnyNonRefAC";
    }
    return "????";
  }
  
  private String getDetailsForGroup(){
    ArrayList<String> details = new ArrayList<>();
    for(String group : this.parser.getGroups())
      details.add(this.parser.getMin(group) + " <= " + group + " <= " + this.parser.getMax(group));
    return typeString() + " between : "+StringTools.startOf(6, details);
  }
  
  private String getDetailsForMinMax(){
    return this.minAC + " <= " + typeString() + " <= "+this.maxAC;
  }
}
