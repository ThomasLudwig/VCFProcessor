package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-08-31
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class MinMaxGroupParser {

  public static final int TYPE_RATIO = 1;
  public static final int TYPE_UNBOUNDED = 2;
  private final ArrayList<String> groups;
  private final HashMap<String, Integer[]> mapMinMax;
  private final HashMap<String, ArrayList<Integer>> indices;

  MinMaxGroupParser(Argument argMin, Argument argMax, HashMap<String, String[]> options, VCF vcf, int type) {
    this.groups = new ArrayList<>();
    this.mapMinMax = new HashMap<>();
    this.indices = new HashMap<>();

    final String keyMin = argMin.getKey();
    final String keyMax = argMax.getKey();

    HashMap<String, Integer> minMap = new HashMap<>();
    HashMap<String, Integer> maxMap = new HashMap<>();

    if (options.containsKey(keyMin.toLowerCase()) || options.containsKey(keyMax.toLowerCase())) {

      for (String group : vcf.getSampleSet().getPed().getGroups())
        indices.put(group, vcf.getSampleSet().getMatrixForGroup(group));

      if (options.containsKey(keyMin.toLowerCase()))
        try {
          for (String value : options.get(keyMin.toLowerCase())) {
            String group = value.split(":")[0];
            int min = 0;
            if (this.indices.containsKey(group))
              switch (type) {
                case TYPE_RATIO:
                  int nbS = this.indices.get(group).size();
                  double rate = Double.parseDouble(value.split(":")[1]);
                  min = (int)(rate*nbS*2);
                  break;
                case TYPE_UNBOUNDED:
                  min = Integer.parseInt(value.split(":")[1]);
                  break;
              }
            else
              Message.warning("Unknown Group name [" + group + "] for filter [" + keyMin + "]");

            Message.warning(minMap.containsKey(group), "Duplicate group [" + group + "] for argument [" + keyMin + "]");
            minMap.put(group, min);
          }
        } catch (Exception e) {
          handle(keyMin, type, e);
        }

      if (options.containsKey(keyMax.toLowerCase()))
        try {
          for (String value : options.get(keyMax.toLowerCase())) {
            String group = value.split(":")[0];
            int max = Integer.MAX_VALUE;
            if (this.indices.containsKey(group))
              switch (type) {
                case TYPE_RATIO:
                  int nbS = this.indices.get(group).size();
                  double rate = Double.parseDouble(value.split(":")[1]);
                  max = (int)(rate*nbS*2);
                  break;
                case TYPE_UNBOUNDED:
                  max = Integer.parseInt(value.split(":")[1]);
                  break;
              }
            else
              Message.warning("Unknown Group name [" + group + "] for filter [" + keyMax + "]");

            Message.warning(maxMap.containsKey(group), "Duplicate group [" + group + "] for argument [" + keyMax + "]");
            maxMap.put(group, max);
          }
        } catch (Exception e) {
          handle(keyMax, type, e);
        }

      this.groups.addAll(minMap.keySet());
      for (String group : maxMap.keySet())
        if (!this.groups.contains(group))
          this.groups.add(group);

      for (String group : groups) { //browsing only the groups previously identified
        int min = 0;
        int max = 2*this.indices.get(group).size();
        if (minMap.containsKey(group))
          min = minMap.get(group);
        if (maxMap.containsKey(group))
          max = maxMap.get(group);
          
        mapMinMax.put(group, new Integer[]{min, max});
      }
    }
  }

  private void handle(String key, int type, Exception e) {
    if (type == TYPE_UNBOUNDED)
      Message.warning("Unable to read Argument [" + key.toLowerCase() + "]. Correct syntax is \"" + key + " " + FrequencyArguments.FORMAT_GROUP_AC + "\"");
    else
      Message.warning("Unable to read Argument [" + key.toLowerCase() + "]. Correct syntax is \"" + key + " " + FrequencyArguments.FORMAT_GROUP_AF + "\"");
    Message.error("Error in parsing", e);
  }

  public ArrayList<String> getGroups() {
    return groups;
  }

  public int getMin(String group) {
    return this.mapMinMax.get(group)[0];
  }

  public int getMax(String group) {
    return this.mapMinMax.get(group)[1];
  }

  public ArrayList<Integer> getIndices(String group) {
    return this.indices.get(group);
  }

  public boolean isValid() {
    return !this.groups.isEmpty();
  }
}
