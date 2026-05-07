package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.FamFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.MaxSampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;

import java.util.*;

public class SampleSet {
  private final Sample[] inputSamples;
  private final Sample[] outputSamples;

  //Warning Samples cannot be used as HashMap keys
  //Mutation a Sample (changing the group, sex, etc.) would break the Hash

  private final HashMap<String, Integer> inputIndices;
  private final HashMap<String, Sample> samplesByID;

  private final ArrayList<String> isFiltered;
  private final HashMap<String, Integer> outputIndices;
  private String[] outputSampleIDs;
  private int[] outputSampleIndices;

  private final VCF vcf;
  private Ped ped;

  public SampleSet(VCF vcf) {
    this.vcf = vcf;
    //process intput (parsing)
    inputIndices = new HashMap<>();
    samplesByID = new HashMap<>();
    this.inputSamples = this.initSamples();

    //process output (filter samples)
    isFiltered = new ArrayList<>();
    outputIndices = new HashMap<>();
    this.filterSamples();
    this.outputSamples = generateOutput();
  }

  /**
   * The indexes are the index of samples, not of column in the VCF line
   */
  private Sample[] initSamples() {
    this.ped = new Ped(vcf.getOriginalChromToSampleHeader().split("\t",-1));
    ArrayList<Sample> samples = ped.getSamples();
    Sample[] ret = new Sample[samples.size()];
    for(int i = 0; i < samples.size(); i++) {
      ret[i] = samples.get(i);
      Message.debug("Add ["+ret[i]+"] -> "+i);
      this.inputIndices.put(ret[i].getId(), i);
      this.samplesByID.put(ret[i].getId(), ret[i]);
    }
    return ret;
  }

  private Sample[] generateOutput() {
    Sample[] ret = new Sample[this.inputSamples.length - isFiltered.size()];
    this.outputSampleIDs = new String[ret.length];
    this.outputSampleIndices = new int[ret.length];
    int outputIndex = 0;
    Message.debug("Generating output samples...");
    for(Sample input : this.inputSamples) {
      Message.debug("Input Sample "+input);
      if (!isFiltered.contains(input.getId())) {
        Message.debug(input+" is not filtered");
        ret[outputIndex] = input;
        Integer inputIndex = inputIndices.get(input.getId());
        Message.debug("From "+inputIndex+" to "+outputIndex);
        this.outputSampleIndices[outputIndex] = inputIndex;
        this.outputSampleIDs[outputIndex] = input.getId();
        outputIndices.put(input.getId(), outputIndex++);
      } else
        Message.debug(input+" is already filtered");
    }
    return ret;
  }

  public int inputFromOutput(int output) { return outputSampleIndices[output]; }

  public Sample[] getOutputSamples() { return outputSamples; }

  public String[] getOutputSampleIDs() { return outputSampleIDs; }

  public int[] getOutputSampleIndices() { return outputSampleIndices; }

  public Sample getSample(String id) { return samplesByID.get(id); }

  public Integer getInputIndex(Sample sample) { return inputIndices.get(sample.getId()); }

  public Integer getOutputIndex(Sample sample) {
    Integer ret = outputIndices.get(sample.getId());
    return ret == null ? -1 : ret;
  }

  public Integer getOutputIndex(String id) { return getOutputIndex(getSample(id)); }

  public Sample getSampleByInputIndex(int i) { return inputSamples[i]; }

  public Sample getSampleByOutputIndex(int i) { return outputSamples[i]; }

  public int getInputSize() { return inputIndices.size(); }

  public int getOutputSize() { return outputIndices.size(); }

  /**
   * Gets the indices of samples for the given group
   *
   * @param group the group to consider
   * @return the list of indices for the samples in the group
   */
  public ArrayList<Integer> getOutputIndicesForGroup(String group) {
    Message.debug("Looking for " + group);
    ArrayList<Integer> members = new ArrayList<>();
    if (group == null || group.isEmpty())
      return members;

    for(int i = 0; i < this.inputSamples.length; i++)
      if(this.inputSamples[i].getGroup().equals(group))
        members.add(i);

    Message.debug(members.size() + " members found for " + group);
    return members;
  }

  public boolean hasSample(String id) {
    if(id == null || id.isEmpty()) return false;

    for (Sample sample : this.outputSamples)
      if (id.equals(sample.getId()))
        return true;

    return false;
  }

  public static ArrayList<String> commonOutputSamples(SampleSet set1, SampleSet set2) {
    ArrayList<String> ret = new ArrayList<>();

    for (Sample sample : set1.outputSamples)
      if(set2.hasSample(sample.getId()))
          ret.add(sample.getId());
    return ret;
  }

  private void filter(Sample sample, SampleFilter filter) {
    if (!isFiltered.contains(sample.getId()) && !(filter.pass(sample))) {
      Message.verbose("Sample [" + sample.getId() + "] has been filtered out by " + filter.getClass().getSimpleName());
      this.isFiltered.add(sample.getId());
    }
  }

  private ArrayList<String> getUnfilteredSampleIDs(){
    ArrayList<String> ret = new ArrayList<>();
    for (Sample sample : this.inputSamples)
      if (!isFiltered.contains(sample.getId()))
        ret.add(sample.getId());
    return ret;
  }

  public void applyFilter(MaxSampleFilter filter) {
    if (filter == null)
      return;
    filter.setSamples(getUnfilteredSampleIDs());

    for (Sample sample : this.inputSamples)
      filter(sample, filter);
  }

  public void applyFilter(FamFilter famFilter) {
    for (Sample sample : this.inputSamples)
      filter(sample, famFilter);
    this.bindToPed(famFilter.getFam());
  }

  /**
   * Gets the indices of samples for the given group
   *
   * @param group the group to consider
   * @return the list of indices for the samples in the group
   */
  public ArrayList<Integer> getMatrixForGroup(String group) {
    Message.debug("Looking for " + group);
    ArrayList<Integer> members = new ArrayList<>();
    if (group == null || group.isEmpty())
      return members;
    for (Sample sample : getOutputSamples())
      if (sample.isInGroup(group))
        members.add(getOutputIndex(sample));
    Message.debug(members.size() + " members found for " + group);
    return members;
  }

  public Ped getPed() {
    return ped;
  }

  private void filterSamples() {
    FamFilter famFilter = null;
    MaxSampleFilter maxSampleFilter = null;
    ArrayList<SampleFilter> sampleFilters = new ArrayList<>();

    for (SampleFilter filter : vcf.getCommandParser().getSampleFilters())
      if (filter instanceof FamFilter)
        famFilter = (FamFilter) filter;
      else if (filter instanceof MaxSampleFilter)
        maxSampleFilter = (MaxSampleFilter) filter;
      else
        sampleFilters.add(filter);

    //First apply famFilter
    if (famFilter != null)
      applyFilter(famFilter);

    //Apply Other Filters
    for (SampleFilter filter : sampleFilters)
      for (Sample sample : this.inputSamples)
        filter(sample, filter);
    //Last apply maxSampleFilter
    applyFilter(maxSampleFilter);

    generateOutput();

    Message.info("Sample kept : " + getOutputSize() + "/" + getInputSize());
    Message.warning(getOutputSize() == 0 && getInputSize() > 0, "No Samples left in the VCF file");
    //throw new VCFException("No sample remaining after filtering");
  }

  /**
   * Doesn't remove the samples anymore, this will be done from the famFilter
   *
   * @param ped the ped file to bind
   */
  public void bindToPed(Ped ped) {
    Message.verbose("Binding ped file [" + ped.getFilename() + "] to VCF file [" + vcf.getFilename() + "]");
    this.ped = ped;
    //TODO update stored sample

    List<Sample> keep = this.applyPedSamples();
    ped.keepOnly(keep);
    //ped.keepOnly(this.sampleIndices.navigableKeySet());
  }

  private List<Sample> applyPedSamples(){
    List<Sample> keep = new ArrayList<>();
    for (Sample pedSample : ped.getSamples()) {
      Sample s = this.getSample(pedSample.getId());
      if(s != null) {
        s.apply(pedSample);
        keep.add(s);
      }
    }
    return keep;
  }
}
