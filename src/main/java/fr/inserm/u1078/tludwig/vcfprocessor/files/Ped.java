package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Object Representing the content of a PED File
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 2015/03/17
 */
public class Ped implements FileFormat {
  public static final String NO_GROUP = "NO_GROUP";
  public static final int SEX_MALE = 1;
  public static final int SEX_FEMALE = 2;
  public static final int PHENO_UNAFFECTED = 1;
  public static final int PHENO_AFFECTED = 2;

  //ped file data are overwritten by default ped ?
  //15:19:19 INFO  Information about this PED File
  //15:19:19 INFO  3 samples
  //15:19:19 INFO  1 groups
  //15:19:19 INFO  Group fam - 3 samples
  //15:19:19 INFO  Parsing of PED File ended.
  //15:19:19 INFO  Information about this PED File
  //15:19:19 INFO  3 samples
  //15:19:19 INFO  1 groups
  //15:19:19 INFO  Group NO_GROUP - 3 samples
  //15:19:19 INFO  Parsing of PED File ended.
  //15:19:19 INFO  Sample kept : 3/3

  //2024-06-25 No :
  //1. Loading the Ped Samples (mssage 1)
  //2. Loading the VCF Samples (message 2)
  //3. Binding the Ped to the VCF

  private final String filename;
  private final ArrayList<Sample> samples;
  private ArrayList<Sample>[] samplesByGroup;
  private final ArrayList<String> groups;

  /**
   * Creates a Ped object from a PED file
   *
   * @param filename - the name of the PED file
   * @throws PedException if there is a problem with the ped file
   */
  public Ped(String filename) throws PedException {
    this.filename = filename;
    Message.info("Starting to parse PED File : " + filename);
    samples = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = in.readLine()) != null)
        samples.add(Ped.createSample(line));
      in.close();
    } catch (IOException e) {
      throw new PedException("Error while reading Ped File " + filename, e);
    }

    groups = new ArrayList<>();
    for (Sample sample : samples)
      addGroup(sample.getGroup());
    this.updateSampleByGroup();

    this.printInfo();
    Message.info("Parsing of PED File ended.");
  }
  
  public Ped(String[] vcfHeaders){
    this.filename = "from.vcf.file";
    samples = new ArrayList<>();
    
    for(int i = 9; i < vcfHeaders.length; i++)
      samples.add(new Sample(vcfHeaders[i], vcfHeaders[i], null, null, 0, 0, NO_GROUP));
   
    groups = new ArrayList<>();
    for (Sample sample : samples)
      addGroup(sample.getGroup());
    this.updateSampleByGroup();

    this.printInfo();
    Message.info("Parsing of PED File ended.");
  }

  public String getFilename() {
    return this.filename;
  }

  /**
   * Keep only samples common to Ped and the list
   *
   * @param extSamples the external list of samples
   */
  public void keepOnly(Collection<Sample> extSamples) {
    //get common list of samples, ordered as target (VCF)
    ArrayList<Sample> common = new ArrayList<>();
    for (Sample sample : extSamples)
      if (this.samples.contains(sample))
        common.add(sample);

    //clear current samples (global and by groups)
    this.samples.clear();
    for (int i = 0; i < this.groups.size(); i++)
      this.samplesByGroup[i].clear();

    //add common samples to the list and to its group
    for (Sample sample : common) {
      this.samples.add(sample);
      int group = groups.indexOf(sample.getGroup());
      this.samplesByGroup[group].add(sample);
    }
  }
  @SuppressWarnings("unchecked")
  private void updateSampleByGroup() {
    this.samplesByGroup = new ArrayList[this.groups.size()];

    for (Sample sample : samples) {
      int group = groups.indexOf(sample.getGroup());
      if (samplesByGroup[group] == null)
        samplesByGroup[group] = new ArrayList<>();
      this.samplesByGroup[group].add(sample);
    }
  }

  private void printInfo() {
    Message.info("Information about this PED File");
    Message.info(samples.size() + " samples");
    Message.info(groups.size() + " groups");
    for (String group : groups)
      Message.info("Group " + group + " - " + this.samplesByGroup[this.getGroupIndex(group)].size() + " samples");
  }

  public Sample getSample(String individualId) {
    for (Sample sample : samples)
      if (sample.getId().equals(individualId))
        return sample;
    return null;
  }

  public Sample getSample(int index) {
    return this.samples.get(index);
  }

  public int getGroupSize(String group) {
    return this.samplesByGroup[this.getGroupIndex(group)].size();
  }

  public Map<String, Integer> getGroupSizes(){
    Map<String, Integer> sizes = new TreeMap<>();
    for(String group : getGroups())
      sizes.put(group, this.getGroupSize(group));
    return sizes;
  }

  /**
   * Adds the group to groups
   * So that groups is sorted, and has a unique occurrence of each groups
   *
   * @param group - the group to add
   */
  private void addGroup(String group) {
    boolean added = false;
    for (int i = 0; i < groups.size(); i++) {
      int compare = group.compareTo(groups.get(i));
      if (compare < 0) {
        groups.add(i, group);
        added = true;
        break;
      }
      if (compare == 0)
        return;
    }
    if (!added)
      groups.add(group);
  }

  public ArrayList<String> getGroups() {
    return groups;
  }

  public int getGroupIndex(String group) {
    return groups.indexOf(group);
  }

  public int getSampleSize() {
    return this.samples.size();
  }

  public ArrayList<Sample> getSamples() {
    return this.samples;
  }

  public ArrayList<Sample> getSamplesForGroup(String group){
    int idx = this.groups.indexOf(group);
    if(idx < 0 || idx > this.samplesByGroup.length)
      return null;
    return this.samplesByGroup[idx];
  }
  
  public ArrayList<Sample>[] getSamplesByGroup() {
    return this.samplesByGroup;
  }

  public static Sample createSample(String line) {
    String[] fields = line.split("\\s+");
    if(fields.length == 6)
      return new Sample(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), Integer.parseInt(fields[5]), NO_GROUP);
    return new Sample(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), Integer.parseInt(fields[5]), fields[6]);
  }

  public ArrayList<String> getFamilies() {
    ArrayList<String> ret = new ArrayList<>();
    for (Sample s : this.samples) {
      String fam = s.getFid();
      boolean added = false;
      for (int i = 0; i < ret.size(); i++) {
        String current = ret.get(i);
        int comp = fam.compareTo(current);
        if (comp <= 0) {
          added = true;
          if (comp < 0)
            ret.add(i, fam);
          break;
        }
      }

      if (!added)
        ret.add(fam);
    }
    return ret;
  }

  public ArrayList<String> getIDs() {
    ArrayList<String> ret = new ArrayList<>();
    for (Sample s : this.samples) {
      String id = s.getId();
      boolean added = false;
      for (int i = 0; i < ret.size(); i++) {
        String current = ret.get(i);
        int comp = id.compareTo(current);
        if (comp <= 0) {
          added = true;
          if (comp < 0)
            ret.add(i, id);
          break;
        }
      }

      if (!added)
        ret.add(id);
    }
    return ret;
  }

  public ArrayList<Sample> getCases() {
    ArrayList<Sample> ret0 = new ArrayList<>();
    ArrayList<Sample> ret1 = new ArrayList<>();
    ArrayList<Sample> ret2 = new ArrayList<>();

    for (Sample s : samples) {
      if (s.getPhenotype() == 0)
        ret0.add(s);
      if (s.getPhenotype() == 1)
        ret1.add(s);
      if (s.getPhenotype() == 2)
        ret2.add(s);
    }

    //Cases should be 2 and Controls 1, but we allow Cases to be 1 and controls to be 0
    if (!ret0.isEmpty() && !ret1.isEmpty() && ret2.isEmpty())
      return ret1;

    return ret2;
  }

  public ArrayList<Sample> getControls() {
    ArrayList<Sample> ret0 = new ArrayList<>();
    ArrayList<Sample> ret1 = new ArrayList<>();
    ArrayList<Sample> ret2 = new ArrayList<>();

    for (Sample s : samples) {
      if (s.getPhenotype() == 0)
        ret0.add(s);
      if (s.getPhenotype() == 1)
        ret1.add(s);
      if (s.getPhenotype() == 2)
        ret2.add(s);
    }

    //Cases should be 2 and Controls 1, but we allow Cases to be 1 and controls to be 0
    if (!ret0.isEmpty() && !ret1.isEmpty() && ret2.isEmpty())
      return ret0;

    return ret1;
  }

  public ArrayList<Sample> getMales() {
    ArrayList<Sample> ret0 = new ArrayList<>();
    ArrayList<Sample> ret1 = new ArrayList<>();
    ArrayList<Sample> ret2 = new ArrayList<>();

    for (Sample s : samples) {
      if (s.getSex() == 0)
        ret0.add(s);
      if (s.getSex() == 1)
        ret1.add(s);
      if (s.getSex() == 2)
        ret2.add(s);
    }

    //Females should be 2 and Males 1, but we allow Females to be 1 and Males to be 0
    if (!ret0.isEmpty() && !ret1.isEmpty() && ret2.isEmpty())
      return ret0;

    return ret1;
  }

  public ArrayList<Sample> getFemales() {
    ArrayList<Sample> ret0 = new ArrayList<>();
    ArrayList<Sample> ret1 = new ArrayList<>();
    ArrayList<Sample> ret2 = new ArrayList<>();

    for (Sample s : samples) {
      if (s.getSex() == 0)
        ret0.add(s);
      if (s.getSex() == 1)
        ret1.add(s);
      if (s.getSex() == 2)
        ret2.add(s);
    }

    //Females should be 2 and Males 1, but we allow Females to be 1 and Males to be 0
    if (!ret0.isEmpty() && !ret1.isEmpty() && ret2.isEmpty())
      return ret1;

    return ret2;
  }

  public static ArrayList<String> getGroups(Collection<Sample> samples) {
    ArrayList<String> groups = new ArrayList<>();
    for (Sample sample : samples)
      if (!groups.contains(sample.getGroup()))
        groups.add(sample.getGroup());
    return groups;
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"ped"};
  }

  @Override
  public String fileFormatDescription() {
    return "Pedigree file";
  }
}
