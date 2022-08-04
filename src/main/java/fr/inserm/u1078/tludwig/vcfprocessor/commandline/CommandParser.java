package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.StartUpException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype.GenotypeISKSVAFFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.AlleleCountFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.AlleleNumberFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.DPVariantFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.FamFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.Filter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.VariantFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.FlagFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.GQVariantFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype.GenotypeDPFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype.GenotypeFlagFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype.GenotypeGQFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.HWEFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.IDFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.SNPIndelFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.InfoFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.MaxSampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.MissingFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.PhaseFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.PositionFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.QualityFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.SampleFamilyFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.SampleGroupFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.SampleIDFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.SamplePhenotypeFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.SampleSexFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.ThinFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.FunctionFactory;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class CommandParser {
  //TODO every command line filters must be listed as INFO lines
  //TODO case insensitive
  //TODO famfilter must be created before filters that depends on it
  //TODO if there are args that start with - or -- but are unknown ==> WARNING

  private final String[] args;
  private final ArrayList<String> keys;
  private final ArrayList<Filter> filters;
  private final ArrayList<SampleFilter> sampleFilters;
  private final ArrayList<LineFilter> lineFilters;
  private final ArrayList<VariantFilter> variantFilters;
  private final ArrayList<GenotypeFilter> genotypeFilters;
  private final HashMap<String, String[]> options;

  private static final boolean KEEP = true;
  private static final boolean REMOVE = false;
  private static final boolean OVERLAP = true;
  private static final boolean ON_START = false;
  private static final boolean ALL = true;
  private static final boolean ANY = false;

  public CommandParser(String[] args) {
    this.filters = new ArrayList<>();
    this.lineFilters = new ArrayList<>();
    this.sampleFilters = new ArrayList<>();
    this.variantFilters = new ArrayList<>();
    this.genotypeFilters = new ArrayList<>();
    this.options = new HashMap<>();
    this.args = args;
    this.keys = this.initKeys();
  }
  
  public static ArrayList<String> getAllowedKeys(String args[]){
    ArrayList<String> ret = new ArrayList<>();
    for(String k : Main.ALLOWED_KEYS)
      ret.add(k);
    //Get function arguments
    Function f = FunctionFactory.getFunction(args); 
    for(Parameter param : f.getParameters()){
      String key = param.getKey().toLowerCase();
      if(ret.contains(key))
        throw new StartUpException("Function ["+args[0]+"] uses the key ["+param.getKey()+"] which is already used by a Filtering Argument");
      ret.add(key);
    }        

    //Get all authorized filter arguments
    ret.addAll(Argument.getAllowedKeys());
    return ret;
  }

  private  ArrayList<String> initKeys() {
    ArrayList<String> allowedKeys = getAllowedKeys(args);
    // check validity : no duplicate keys        
    ArrayList<String> keyList = new ArrayList<>();
    for (String arg : args)
      if (arg != null && arg.startsWith("--")){
        String low = arg.toLowerCase();
        if(!allowedKeys.contains(low))
          Message.warning("unknown argument ["+arg+"] in command line.");
        if (keyList.contains(low))//TODO not detected 
          Message.warning("duplicate key [" + arg + "] in command line. Only the first one will be processed");
        else
          keyList.add(low);
      }

    //parse key/values options
    for (String key : keyList) {
      String[] val = parseOptions(args, key);
      if (val == null)
        Message.info("Argument : " + key);
      else
        Message.info("Argument : " + key + " " + String.join(" ", val));
      options.put(key, val);
    }

    return keyList;
  }

  /**
   * Filters lines based on the variants position
   */
  public void processPositionArguments() {
    //PositionArguments.KEEP_POS
    //PositionArguments.KEEP_POSITIONS
    if (hasAnyArgument(PositionArguments.KEEP_POS, PositionArguments.KEEP_POSITIONS)) {
      PositionFilter keepPositionSimple = new PositionFilter(KEEP, ON_START);
      if (hasArgument(PositionArguments.KEEP_POS))
        keepPositionSimple.addPositions(getListOptions(PositionArguments.KEEP_POS));
      if (hasArgument(PositionArguments.KEEP_POSITIONS))
        keepPositionSimple.addPositionFilenames(getListOptions(PositionArguments.KEEP_POSITIONS));
      addFilter(keepPositionSimple);
    }

    //PositionArguments.REMOVE_POS
    //PositionArguments.REMOVE_POSITIONS
    if (hasAnyArgument(PositionArguments.REMOVE_POS, PositionArguments.REMOVE_POSITIONS)) {
      PositionFilter removePositionSimple = new PositionFilter(REMOVE, ON_START);
      if (hasArgument(PositionArguments.REMOVE_POS))
        removePositionSimple.addPositions(getListOptions(PositionArguments.REMOVE_POS));
      if (hasArgument(PositionArguments.REMOVE_POSITIONS))
        removePositionSimple.addPositionFilenames(getListOptions(PositionArguments.REMOVE_POSITIONS));
      addFilter(removePositionSimple);
    }

    //PositionArguments.KEEP_POS_OVERLAP
    //PositionArguments.KEEP_POSITIONS_OVERLAP
    //PositionArguments.KEEP_BED
    if (hasAnyArgument(PositionArguments.KEEP_POS_OVERLAP, PositionArguments.KEEP_POSITIONS_OVERLAP, PositionArguments.KEEP_BED)) {
      PositionFilter keepPositionOverlap = new PositionFilter(KEEP, OVERLAP);
      if (hasArgument(PositionArguments.KEEP_POS_OVERLAP))
        keepPositionOverlap.addPositions(getListOptions(PositionArguments.KEEP_POS_OVERLAP));
      if (hasArgument(PositionArguments.KEEP_POSITIONS_OVERLAP))
        keepPositionOverlap.addPositionFilenames(getListOptions(PositionArguments.KEEP_POSITIONS_OVERLAP));
      if (hasArgument(PositionArguments.KEEP_BED))
        keepPositionOverlap.addBedFilename(getListOptions(PositionArguments.KEEP_BED));
      addFilter(keepPositionOverlap);
    }

    //PositionArguments.REMOVE_POS_OVERLAP
    //PositionArguments.REMOVE_POSITIONS_OVERLAP
    //PositionArguments.REMOVE_BED
    if (hasAnyArgument(PositionArguments.REMOVE_POS_OVERLAP, PositionArguments.REMOVE_POSITIONS_OVERLAP, PositionArguments.REMOVE_BED)) {
      PositionFilter removePositionOverlap = new PositionFilter(REMOVE, OVERLAP);
      if (hasArgument(PositionArguments.REMOVE_POS_OVERLAP))
        removePositionOverlap.addPositions(getListOptions(PositionArguments.REMOVE_POS_OVERLAP));
      if (hasArgument(PositionArguments.REMOVE_POSITIONS_OVERLAP))
        removePositionOverlap.addPositionFilenames(getListOptions(PositionArguments.REMOVE_POSITIONS_OVERLAP));
      if (hasArgument(PositionArguments.REMOVE_BED))
        removePositionOverlap.addBedFilename(getListOptions(PositionArguments.REMOVE_BED));
      addFilter(removePositionOverlap);
    }
    
    //PositionArguments.THIN     
    if (hasArgument(PositionArguments.THIN)) {
      int interval;
      try {
        interval = convertToDistance(getStringOption(PositionArguments.THIN));
        ThinFilter thinFilter = new ThinFilter(interval);
        addFilter(thinFilter);
      } catch (NumberFormatException e) {
        Message.warning("The option [" + PositionArguments.THIN + "] must be followed by an integer");
      }
    }
  }

  /**
   * Filters genotypes depending on their properties
   */
  public void processGenotypeArguments() {
    //GenotypeArguments.REMOVE_FILTERED_GENO_ALL
    //GenotypeArguments.KEEP_FILTERED_GENO
    if (hasAnyArgument(GenotypeArguments.REMOVE_FILTERED_GENO_ALL, GenotypeArguments.KEEP_FILTERED_GENO)) {
      GenotypeFlagFilter keepGenotypeFlag = new GenotypeFlagFilter(KEEP);

      if (hasArgument(GenotypeArguments.KEEP_FILTERED_GENO))
        for (String flag : getListOptions(GenotypeArguments.KEEP_FILTERED_GENO))
          keepGenotypeFlag.add(flag);

      this.addFilter(keepGenotypeFlag);
    }
    //GenotypeArguments.REMOVE_FILTERED_GENO
    if (hasArgument(GenotypeArguments.REMOVE_FILTERED_GENO)) {
      GenotypeFlagFilter removeGenotypeFlag = new GenotypeFlagFilter(REMOVE);
      for (String flag : getListOptions(GenotypeArguments.REMOVE_FILTERED_GENO))
        removeGenotypeFlag.add(flag);
      this.addFilter(removeGenotypeFlag);
    }

    //GenotypeArguments.MINDP
    //GenotypeArguments.MAXDP
    MinMaxParser keepDP = new MinMaxParser(GenotypeArguments.MINDP, GenotypeArguments.MAXDP, MinMaxParser.TYPE_UNBOUNDED, "DP (integer)");
    if (keepDP.isValid())
      addFilter(new GenotypeDPFilter(keepDP.getMinInt(), keepDP.getMaxInt(Integer.MAX_VALUE)));
    
    //GenotypeArguments.MINGQ
    //GenotypeArguments.MAXGQ
    MinMaxParser keepGQ = new MinMaxParser(GenotypeArguments.MINGQ, GenotypeArguments.MAXGQ, MinMaxParser.TYPE_UNBOUNDED, "GQ (integer)");
    if (keepGQ.isValid())
      addFilter(new GenotypeGQFilter(keepGQ.getMinInt(), keepGQ.getMaxInt(99)));

    //GenotypeArguments.MINVAF
    //GenotypeArguments.MAXVAF
    MinMaxParser keepVAF = new MinMaxParser(GenotypeArguments.MINVAF, GenotypeArguments.MAXVAF, MinMaxParser.TYPE_RATIO, "VAF (ratio)");
    if(keepVAF.isValid())
      addFilter(new GenotypeISKSVAFFilter(keepVAF.getMinDouble(), keepVAF.getMaxDouble()));
  }

  /**
   * Filters on various variant properties
   */
  public void processPropertyArguments() {
    //PropertyArguments.KEEP_ID
    //PropertyArguments.KEEP_IDS
    if (hasAnyArgument(PropertyArguments.KEEP_ID, PropertyArguments.KEEP_IDS)) {
      IDFilter keepIDFilter = new IDFilter(KEEP);
      if (hasArgument(PropertyArguments.KEEP_ID))
        keepIDFilter.addIDs(getListOptions(PropertyArguments.KEEP_ID));
      if (hasArgument(PropertyArguments.KEEP_IDS))
        keepIDFilter.addFilenames(getListOptions(PropertyArguments.KEEP_IDS));
      addFilter(keepIDFilter);
    }

    //PropertyArguments.REMOVE_ID
    //PropertyArguments.REMOVE_IDS
    if (hasAnyArgument(PropertyArguments.REMOVE_ID, PropertyArguments.REMOVE_IDS)) {
      IDFilter removeIDFilter = new IDFilter(REMOVE);
      if (hasArgument(PropertyArguments.REMOVE_ID))
        removeIDFilter.addIDs(getListOptions(PropertyArguments.REMOVE_ID));
      if (hasArgument(PropertyArguments.REMOVE_IDS))
        removeIDFilter.addFilenames(getListOptions(PropertyArguments.REMOVE_IDS));
      addFilter(removeIDFilter);
    }
    
    //PropertyArguments.MIN_ALLELES
    //PropertyArguments.MAX_ALLELES
    MinMaxParser keepAlleles = new MinMaxParser(PropertyArguments.MIN_ALLELES, PropertyArguments.MAX_ALLELES, MinMaxParser.TYPE_UNBOUNDED, "integer");
    if (keepAlleles.isValid())
      addFilter(new AlleleNumberFilter(keepAlleles.getMinInt(), keepAlleles.getMaxInt(Integer.MAX_VALUE)));

    //PropertyArguments.KEEP_SNVS
    if (hasArgument(PropertyArguments.KEEP_SNVS))
      addFilter(new SNPIndelFilter(SNPIndelFilter.SNP, KEEP));
    //PropertyArguments.REMOVE_SNVS
    if (hasArgument(PropertyArguments.REMOVE_SNVS))
      addFilter(new SNPIndelFilter(SNPIndelFilter.SNP, REMOVE));

    //PropertyArguments.KEEP_INDELS
    if (hasArgument(PropertyArguments.KEEP_INDELS))
      addFilter(new SNPIndelFilter(SNPIndelFilter.INDEL, KEEP));
    //PropertyArguments.REMOVE_INDELS
    if (hasArgument(PropertyArguments.REMOVE_INDELS))
      addFilter(new SNPIndelFilter(SNPIndelFilter.INDEL, REMOVE));

    ////////////////////////////////
    for (Argument arg : new Argument[]{
      PropertyArguments.KEEP_FILTERED_ALL,
      PropertyArguments.REMOVE_FILTERED_ALL,
      PropertyArguments.KEEP_FILTERED_ANY,
      PropertyArguments.REMOVE_FILTERED_ANY,
      PropertyArguments.STRICT_KEEP_FILTERED_ALL,
      PropertyArguments.STRICT_REMOVE_FILTERED_ALL,
      PropertyArguments.STRICT_KEEP_FILTERED_ANY,
      PropertyArguments.STRICT_REMOVE_FILTERED_ANY})
      if (hasArgument(arg)) {
        FlagFilter removeAllFlagFilter = new FlagFilter(arg.getKey());
        try {
          for (String flag : getListOptions(arg))
            removeAllFlagFilter.add(flag);
        } catch (Exception e) {
          Message.warning("Could not parse arguments for [" + arg + "]");
        }
        addFilter(removeAllFlagFilter);
      }

    //PropertyArguments.KEEP_ANY_INFO
    if (hasArgument(PropertyArguments.KEEP_ANY_INFO)) {
      InfoFilter keepAny = new InfoFilter(KEEP, ANY);
      try {
        for (String info : getListOptions(PropertyArguments.KEEP_ANY_INFO))
          keepAny.add(info);
      } catch (Exception e) {
        Message.warning("Could not parse arguments for [" + PropertyArguments.KEEP_ANY_INFO + "]");
      }
      addFilter(keepAny);
    }
    //PropertyArguments.KEEP_ALL_INFO
    if (hasArgument(PropertyArguments.KEEP_ALL_INFO)) {
      InfoFilter keepAll = new InfoFilter(KEEP, ALL);
      try {
        for (String info : getListOptions(PropertyArguments.KEEP_ALL_INFO))
          keepAll.add(info);
      } catch (Exception e) {
        Message.warning("Could not parse arguments for [" + PropertyArguments.KEEP_ALL_INFO + "]");
      }
      addFilter(keepAll);
    }
    //PropertyArguments.REMOVE_ANY_INFO
    if (hasArgument(PropertyArguments.REMOVE_ANY_INFO)) {
      InfoFilter removeAny = new InfoFilter(REMOVE, ANY);
      try {
        for (String info : getListOptions(PropertyArguments.REMOVE_ANY_INFO))
          removeAny.add(info);
      } catch (Exception e) {
        Message.warning("Could not parse arguments for [" + PropertyArguments.REMOVE_ANY_INFO + "]");
      }
      addFilter(removeAny);
    }
    //PropertyArguments.REMOVE_ALL_INFO
    if (hasArgument(PropertyArguments.REMOVE_ALL_INFO)) {
      InfoFilter removeAll = new InfoFilter(REMOVE, ALL);
      try {
        for (String info : getListOptions(PropertyArguments.REMOVE_ALL_INFO))
          removeAll.add(info);
      } catch (Exception e) {
        Message.warning("Could not parse arguments for [" + PropertyArguments.REMOVE_ALL_INFO + "]");
      }
      addFilter(removeAll);
    }
    
    //PropertyArguments.MIN_MEANDP
    //PropertyArguments.MAX_MEANDP
    MinMaxParser keepMeanDP = new MinMaxParser(PropertyArguments.MIN_MEANDP, PropertyArguments.MAX_MEANDP, MinMaxParser.TYPE_UNBOUNDED, "float");
    if (keepMeanDP.isValid())
      addFilter(new DPVariantFilter(keepMeanDP.getMinDouble(), keepMeanDP.getMaxDouble(), DPVariantFilter.TYPE_MEAN));
    //PropertyArguments.MIN_MEDIANDP
    //PropertyArguments.MAX_MEDIANDP
    MinMaxParser keepMedianDP = new MinMaxParser(PropertyArguments.MIN_MEDIANDP, PropertyArguments.MAX_MEDIANDP, MinMaxParser.TYPE_UNBOUNDED, "float");
    if (keepMedianDP.isValid())
      addFilter(new DPVariantFilter(keepMedianDP.getMinDouble(), keepMedianDP.getMaxDouble(), DPVariantFilter.TYPE_MEDIAN));
    //PropertyArguments.MIN_MEANGQ
    //PropertyArguments.MAX_MEANGQ
    MinMaxParser keepMeanGQ = new MinMaxParser(PropertyArguments.MIN_MEANGQ, PropertyArguments.MAX_MEANGQ, MinMaxParser.TYPE_UNBOUNDED, "float");
    if (keepMeanGQ.isValid())
      addFilter(new GQVariantFilter(keepMeanGQ.getMinDouble(), keepMeanGQ.getMaxDouble(), GQVariantFilter.TYPE_MEAN));
    //PropertyArguments.MIN_MEDIANGQ
    //PropertyArguments.MAX_MEDIANGQ
    MinMaxParser keepMedianGQ = new MinMaxParser(PropertyArguments.MIN_MEDIANGQ, PropertyArguments.MAX_MEDIANGQ, MinMaxParser.TYPE_UNBOUNDED, "float");
    if (keepMedianGQ.isValid())
      addFilter(new GQVariantFilter(keepMedianGQ.getMinDouble(), keepMedianGQ.getMaxDouble(), GQVariantFilter.TYPE_MEDIAN));

    //PropertyArguments.HWE
    if (hasArgument(PropertyArguments.HWE))
      try {
        addFilter(new HWEFilter(getDoubleOption(PropertyArguments.HWE), KEEP));
      } catch (Exception e) {
        Message.warning("Argument [" + PropertyArguments.HWE + "] must be followed by a valid p-value");
      }
    //PropertyArguments.REMOVE_HWE
    if (hasArgument(PropertyArguments.REMOVE_HWE))
      try {
        addFilter(new HWEFilter(getDoubleOption(PropertyArguments.REMOVE_HWE), REMOVE));
      } catch (Exception e) {
        Message.warning("Argument [" + PropertyArguments.REMOVE_HWE + "] must be followed by a valid p-value");
      }
    
    //PropertyArguments.PHASED
    if (hasArgument(PropertyArguments.PHASED))
      addFilter(new PhaseFilter(KEEP));
    //PropertyArguments.REMOVE_PHASED
    if (hasArgument(PropertyArguments.REMOVE_PHASED))
      addFilter(new PhaseFilter(REMOVE));

    //PropertyArguments.MINQ
    //PropertyArguments.MAXQ
    MinMaxParser minmaxQ = new MinMaxParser(PropertyArguments.MINQ, PropertyArguments.MAXQ, MinMaxParser.TYPE_UNBOUNDED, "variant quality");
    if (minmaxQ.isValid())
      addFilter(new QualityFilter(minmaxQ.getMinDouble(), minmaxQ.getMaxDouble()));
  }

  /**
   * Filters on Missing rate AF/AC.Rate and AF depends on the number of samples.Thus :
   * This method HAS TO BE called AFTER the vcf/ped bind
   *
   * @param vcf
   * @param ped
   */
  public void processSampleDependantArguments(final VCF vcf, final Ped ped) {
    int size = ped.getSamples().size();
    //difference betweek AF and AC, is the AF depends on missing... think about this
    //Still need to check presence of ped for some filters
    
    //PropertyArguments.MIN_MISSING
    //PropertyArguments.MAX_MISSING
    MinMaxParser missingRate = new MinMaxParser(PropertyArguments.MIN_MISSING, PropertyArguments.MAX_MISSING, MinMaxParser.TYPE_RATIO, "missing rate");
    if (missingRate.isValid())
      addFilter(new MissingFilter(missingRate.getMinFor(size), missingRate.getMaxFor(size)));

    //PropertyArguments.MIN_MISSING_COUNT
    //PropertyArguments.MAX_MISSING_COUNT
    MinMaxParser missingCount = new MinMaxParser(PropertyArguments.MIN_MISSING_COUNT, PropertyArguments.MAX_MISSING_COUNT, MinMaxParser.TYPE_UNBOUNDED, "number of samples");
    if (missingCount.isValid())
      addFilter(new MissingFilter(missingCount.getMinInt(), missingCount.getMaxInt(size)));
    
    //MIN_MAF = new Argument("--min-maf";
    //MAX_MAF = new Argument("--max-maf";
    MinMaxParser keepMAF = new MinMaxParser(FrequencyArguments.MIN_MAF, FrequencyArguments.MAX_MAF, MinMaxParser.TYPE_RATIO, "allele frequency ([0;1])");
    if (keepMAF.isValid())
      addFilter(new AlleleCountFilter(keepMAF.getMinFor(size*2), keepMAF.getMaxFor(size*2), AlleleCountFilter.TYPE_MINOR));

    //MIN_REF_AF = new Argument("--min-ref-af";
    //MAX_REF_AF = new Argument("--max-ref-af";
    MinMaxParser keepRefAF = new MinMaxParser(FrequencyArguments.MIN_REF_AF, FrequencyArguments.MAX_REF_AF, MinMaxParser.TYPE_RATIO, "allele frequency ([0;1])");
    if (keepRefAF.isValid())
      addFilter(new AlleleCountFilter(keepRefAF.getMinFor(size*2), keepRefAF.getMaxFor(size*2), AlleleCountFilter.TYPE_REF));

    //MIN_NON_REF_AF = new Argument("--min-non-ref-af";
    //MAX_NON_REF_AF = new Argument("--max-non-ref-af";
    MinMaxParser keepNonRefAF = new MinMaxParser(FrequencyArguments.MIN_ALL_NON_REF_AF, FrequencyArguments.MAX_ALL_NON_REF_AF, MinMaxParser.TYPE_RATIO, "allele frequency ([0;1])");
    if (keepNonRefAF.isValid())
      addFilter(new AlleleCountFilter(keepNonRefAF.getMinFor(size*2), keepNonRefAF.getMaxFor(size*2), AlleleCountFilter.TYPE_NON_REF_ALL));

    //MIN_NON_REF_ANY_AF = new Argument("--min-non-ref-any-af";
    //MAX_NON_REF_ANY_AF = new Argument("--max-non-ref-any-af";
    MinMaxParser keepNonRefAnyAF = new MinMaxParser(FrequencyArguments.MIN_ANY_NON_REF_AF, FrequencyArguments.MAX_ANY_NON_REF_ANY, MinMaxParser.TYPE_RATIO, "allele frequency ([0;1])");
    if (keepNonRefAnyAF.isValid())
      addFilter(new AlleleCountFilter(keepNonRefAnyAF.getMinFor(size*2), keepNonRefAnyAF.getMaxFor(size*2), AlleleCountFilter.TYPE_NON_REF_ANY));
    
    //FrequencyArguments.MIN_MAC
    //FrequencyArguments.MAX_MAC
    MinMaxParser keepMAC = new MinMaxParser(FrequencyArguments.MIN_MAC, FrequencyArguments.MAX_MAC, MinMaxParser.TYPE_UNBOUNDED, "allele count (integer)");
    if (keepMAC.isValid())
      addFilter(new AlleleCountFilter(keepMAC.getMinInt(), keepMAC.getMaxInt(size*2), AlleleCountFilter.TYPE_MINOR));

    //FrequencyArguments.MIN_REF_AC
    //FrequencyArguments.MAX_REF_AC
    MinMaxParser keepRefAC = new MinMaxParser(FrequencyArguments.MIN_REF_AC, FrequencyArguments.MAX_REF_AC, MinMaxParser.TYPE_UNBOUNDED, "allele count (integer)");
    if (keepRefAC.isValid())
      addFilter(new AlleleCountFilter(keepRefAC.getMinInt(), keepRefAC.getMaxInt(size*2), AlleleCountFilter.TYPE_REF));

    //FrequencyArguments.MIN_ALL_NON_REF_AC
    //FrequencyArguments.MAX_ALL_NON_REF_AC
    MinMaxParser keepNonRefAC = new MinMaxParser(FrequencyArguments.MIN_ALL_NON_REF_AC, FrequencyArguments.MAX_ALL_NON_REF_AC, MinMaxParser.TYPE_UNBOUNDED, "allele count (integer)");
    if (keepNonRefAC.isValid())
      addFilter(new AlleleCountFilter(keepNonRefAC.getMinInt(), keepNonRefAC.getMaxInt(size*2), AlleleCountFilter.TYPE_NON_REF_ALL));

    //FrequencyArguments.MIN_ANY_NON_REF_AC
    //FrequencyArguments.MAX_ANY_NON_REF_AC
    MinMaxParser keepNonRefAnyAC = new MinMaxParser(FrequencyArguments.MIN_ANY_NON_REF_AC, FrequencyArguments.MAX_ANY_NON_REF_AC, MinMaxParser.TYPE_UNBOUNDED, "allele count (integer)");
    if (keepNonRefAnyAC.isValid())
      addFilter(new AlleleCountFilter(keepNonRefAnyAC.getMinInt(), keepNonRefAnyAC.getMaxInt(size*2), AlleleCountFilter.TYPE_NON_REF_ANY));
    
    
    //FrequencyArguments.MIN_GROUP_MAF
    //FrequencyArguments.MAX_GROUP_MAF
    MinMaxGroupParser keepGroupMaf = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_MAF, FrequencyArguments.MAX_GROUP_MAF, options, vcf, MinMaxGroupParser.TYPE_RATIO);
    if (keepGroupMaf.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_MAF, FrequencyArguments.MAX_GROUP_MAF);
      else
        addFilter(new AlleleCountFilter(keepGroupMaf, AlleleCountFilter.TYPE_MINOR));

    //FrequencyArguments.MIN_GROUP_REF_AF
    //FrequencyArguments.MAX_GROUP_REF_AF
    MinMaxGroupParser keepGroupRefAF = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_REF_AF, FrequencyArguments.MAX_GROUP_REF_AF, options, vcf, MinMaxGroupParser.TYPE_RATIO);
    if (keepGroupRefAF.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_REF_AF, FrequencyArguments.MAX_GROUP_REF_AF);
      else
        addFilter(new AlleleCountFilter(keepGroupRefAF, AlleleCountFilter.TYPE_REF));

    //FrequencyArguments.MIN_GROUP_ALL_NON_REF_AF
    //FrequencyArguments.MAX_GROUP_ALL_NON_REF_AF
    MinMaxGroupParser keepGroupNonRefAF = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_ALL_NON_REF_AF, FrequencyArguments.MAX_GROUP_ALL_NON_REF_AF, options, vcf, MinMaxGroupParser.TYPE_RATIO);
    if (keepGroupNonRefAF.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_ALL_NON_REF_AF, FrequencyArguments.MAX_GROUP_ALL_NON_REF_AF);
      else
        addFilter(new AlleleCountFilter(keepGroupNonRefAF, AlleleCountFilter.TYPE_NON_REF_ALL));

    //FrequencyArguments.MIN_GROUP_ANY_NON_REF_AF
    //FrequencyArguments.MAX_GROUP_ANY_NON_REF_AF
    MinMaxGroupParser keepGroupNonRefAnyAF = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_ANY_NON_REF_AF, FrequencyArguments.MAX_GROUP_ANY_NON_REF_AF, options, vcf, MinMaxGroupParser.TYPE_RATIO);
    if (keepGroupNonRefAnyAF.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_ANY_NON_REF_AF, FrequencyArguments.MAX_GROUP_ANY_NON_REF_AF);
      else
        addFilter(new AlleleCountFilter(keepGroupNonRefAnyAF, AlleleCountFilter.TYPE_NON_REF_ANY));
  
    //FrequencyArguments.MIN_GROUP_MAC
    //FrequencyArguments.MAX_GROUP_MAC
    MinMaxGroupParser keepGroupMac = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_MAC, FrequencyArguments.MAX_GROUP_MAC, options, vcf, MinMaxGroupParser.TYPE_UNBOUNDED);
    if (keepGroupMac.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_MAC, FrequencyArguments.MAX_GROUP_MAC);
      else
        addFilter(new AlleleCountFilter(keepGroupMac, AlleleCountFilter.TYPE_MINOR));

    //FrequencyArguments.MIN_GROUP_REF_AC
    //FrequencyArguments.MAX_GROUP_REF_AC
    MinMaxGroupParser keepGroupRefAC = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_REF_AC, FrequencyArguments.MAX_GROUP_REF_AC, options, vcf, MinMaxGroupParser.TYPE_UNBOUNDED);
    if (keepGroupRefAC.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_REF_AC, FrequencyArguments.MAX_GROUP_REF_AC);
      else
        addFilter(new AlleleCountFilter(keepGroupRefAC, AlleleCountFilter.TYPE_REF));

    //FrequencyArguments.MIN_GROUP_ALL_NON_REF_AC
    //FrequencyArguments.MAX_GROUP_ALL_NON_REF_AC
    MinMaxGroupParser keepGroupNonRefAC = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_ALL_NON_REF_AC, FrequencyArguments.MAX_GROUP_ALL_NON_REF_AC, options, vcf, MinMaxGroupParser.TYPE_UNBOUNDED);
    if (keepGroupNonRefAC.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_ALL_NON_REF_AC, FrequencyArguments.MAX_GROUP_ALL_NON_REF_AC);
      else
        addFilter(new AlleleCountFilter(keepGroupNonRefAC, AlleleCountFilter.TYPE_NON_REF_ALL));

    //FrequencyArguments.MIN_GROUP_ANY_NON_REF_AC
    //FrequencyArguments.MAX_GROUP_ANY_NON_REF_AC
    MinMaxGroupParser keepGroupNonRefAnyAC = new MinMaxGroupParser(FrequencyArguments.MIN_GROUP_ANY_NON_REF_AC, FrequencyArguments.MAX_GROUP_ANY_NON_REF_AC, options, vcf, MinMaxGroupParser.TYPE_UNBOUNDED);
    if (keepGroupNonRefAnyAC.isValid())
      if (getFamFile() == null)
        missingPedWarning(FrequencyArguments.MIN_GROUP_ANY_NON_REF_AC, FrequencyArguments.MAX_GROUP_ANY_NON_REF_AC);
      else
        addFilter(new AlleleCountFilter(keepGroupNonRefAnyAC, AlleleCountFilter.TYPE_NON_REF_ANY));    
  }

  /**
   * Filters samples from the VCF file
   */
  public void processSampleArguments() throws PedException {
    //SampleArguments.PED
    if (getFamFile() != null) {
      FamFilter famFilter = new FamFilter(getStringOption(SampleArguments.PED));
      addFilter(famFilter);
    }

    //SampleArguments.KEEP_FAMILY
    if (hasArgument(SampleArguments.KEEP_FAMILY))
      if (getFamFile() != null)
        addFilter(new SampleFamilyFilter(KEEP, this.getListOptions(SampleArguments.KEEP_FAMILY)));
      else
        missingPedWarning(SampleArguments.KEEP_FAMILY);
    //SampleArguments.REMOVE_FAMILY
    if (hasArgument(SampleArguments.REMOVE_FAMILY))
      if (getFamFile() != null)
        addFilter(new SampleFamilyFilter(REMOVE, this.getListOptions(SampleArguments.REMOVE_FAMILY)));
      else
        missingPedWarning(SampleArguments.REMOVE_FAMILY);

    //SampleArguments.KEEP_SEX
    if (hasArgument(SampleArguments.KEEP_SEX))
      if (getFamFile() != null)
        addFilter(new SampleSexFilter(KEEP, this.getListOptions(SampleArguments.KEEP_SEX)));
      else
        missingPedWarning(SampleArguments.KEEP_SEX);
    //SampleArguments.REMOVE_SEX
    if (hasArgument(SampleArguments.REMOVE_SEX))
      if (getFamFile() != null)
        addFilter(new SampleSexFilter(REMOVE, this.getListOptions(SampleArguments.REMOVE_SEX)));
      else
        missingPedWarning(SampleArguments.REMOVE_SEX);

    //SampleArguments.KEEP_PHENOTYPE
    if (hasArgument(SampleArguments.KEEP_PHENOTYPE))
      if (getFamFile() != null)
        addFilter(new SamplePhenotypeFilter(KEEP, this.getListOptions(SampleArguments.KEEP_PHENOTYPE)));
      else
        missingPedWarning(SampleArguments.KEEP_PHENOTYPE);
    //SampleArguments.REMOVE_PHENOTYPE
    if (hasArgument(SampleArguments.REMOVE_PHENOTYPE))
      if (getFamFile() != null)
        addFilter(new SamplePhenotypeFilter(REMOVE, this.getListOptions(SampleArguments.REMOVE_PHENOTYPE)));
      else
        missingPedWarning(SampleArguments.REMOVE_PHENOTYPE);

    //SampleArguments.KEEP_GROUP
    if (hasArgument(SampleArguments.KEEP_GROUP))
      if (getFamFile() != null)
        addFilter(new SampleGroupFilter(KEEP, this.getListOptions(SampleArguments.KEEP_GROUP)));
      else
        missingPedWarning(SampleArguments.KEEP_GROUP);
    //SampleArguments.REMOVE_GROUP
    if (hasArgument(SampleArguments.REMOVE_GROUP))
      if (getFamFile() != null)
        addFilter(new SampleGroupFilter(REMOVE, this.getListOptions(SampleArguments.REMOVE_GROUP)));
      else
        missingPedWarning(SampleArguments.REMOVE_GROUP);

    //SampleArguments.KEEP_SAMPLE
    //SampleArguments.KEEP_SAMPLES
    if (hasAnyArgument(SampleArguments.KEEP_SAMPLE, SampleArguments.KEEP_SAMPLES)) {
      SampleIDFilter keepSamples = new SampleIDFilter(KEEP);
      if (hasArgument(SampleArguments.KEEP_SAMPLE))
        keepSamples.addIDs(getListOptions(SampleArguments.KEEP_SAMPLE));
      if (hasArgument(SampleArguments.KEEP_SAMPLES))
        keepSamples.addFilenames(getListOptions(SampleArguments.KEEP_SAMPLES));
      addFilter(keepSamples);
    }

    //SampleArguments.REMOVE_SAMPLE
    //SampleArguments.REMOVE_SAMPLES
    if (hasAnyArgument(SampleArguments.REMOVE_SAMPLE, SampleArguments.REMOVE_SAMPLES)) {
      SampleIDFilter removeSamples = new SampleIDFilter(REMOVE);
      if (hasArgument(SampleArguments.REMOVE_SAMPLE))
        removeSamples.addIDs(getListOptions(SampleArguments.REMOVE_SAMPLE));
      if (hasArgument(SampleArguments.REMOVE_SAMPLES))
        removeSamples.addFilenames(getListOptions(SampleArguments.REMOVE_SAMPLES));
      addFilter(removeSamples);
    }

    ////////////////////////////////
    //SampleArguments.MAX_SAMPLE
    if (hasArgument(SampleArguments.MAX_SAMPLE)) {
      MaxSampleFilter maxSampleFilter = new MaxSampleFilter(getIntegerOption(SampleArguments.MAX_SAMPLE));
      addFilter(maxSampleFilter);
    }
  }
  
  private void missingPedWarning(Argument arg){
    Message.warning("Argument [" + arg + "] cannot be used without [" + SampleArguments.PED + "]");
  }
  
  private void missingPedWarning(Argument arg1, Argument arg2){
    Message.warning("Arguments [" + arg1 + " / " + arg2 + "] cannot be used without [" + SampleArguments.PED + "]");
  }

  public void printSummary() {
    Message.verbose("Total Filters [" + this.filters.size() + "]");
    Message.verbose("\tSample Filters [" + this.sampleFilters.size() + "]");
    for (Filter f : this.sampleFilters)
      Message.verbose("\t\t" + f.getSummary());
    Message.verbose("\tLine Filters [" + this.lineFilters.size() + "]");
    for (Filter f : this.lineFilters)
      Message.verbose("\t\t" + f.getSummary());
    Message.verbose("\tGenotype Filters [" + this.genotypeFilters.size() + "]");
    for (Filter f : this.genotypeFilters)
      Message.verbose("\t\t" + f.getSummary());
    Message.verbose("\tVariant Filters [" + this.variantFilters.size() + "]");
    for (Filter f : this.variantFilters)
      Message.verbose("\t\t" + f.getSummary());
  }

  private boolean hasArgument(Argument arg) {
    return keys.contains(arg.getKey().toLowerCase());
  }
  
  private boolean hasAnyArgument(Argument... args) {
    for(Argument arg : args)
      if(hasArgument(arg))
        return true;
    return false;
  }

  private int convertToDistance(String s) {
    String num = s.toLowerCase();
    if (num.endsWith("mb")) {
      num = num.substring(0, num.length() - 2);
      return 1000000 * new Integer(num);
    }
    if (num.endsWith("kb")) {
      num = num.substring(0, num.length() - 2);
      return 1000 * new Integer(num);
    }
    if (num.endsWith("m")) {
      num = num.substring(0, num.length() - 1);
      return 1000000 * new Integer(num);
    }
    if (num.endsWith("k")) {
      num = num.substring(0, num.length() - 1);
      return 1000 * new Integer(num);
    }
    return new Integer(num.replaceAll("b", "b"));

  }

  private String getFamFile() {
    if (hasArgument(SampleArguments.PED)) {
      String filename = getStringOption(SampleArguments.PED);
      if (!"null".equals(filename))
        return filename;
    }
    return null;
  }

  private double getDoubleOption(Argument arg) {
    String[] op = getListOptions(arg);
    try {
      return Double.parseDouble(op[0]);
    } catch (Exception e) {
      throw new StartUpException("Double not found for argument " + arg + " value [" + Arrays.deepToString(op) + "]", e);
    }
  }

  private int getIntegerOption(Argument arg) {
    String[] op = getListOptions(arg);
    try {
      return Integer.parseInt(op[0]);
    } catch (Exception e) {
      throw new StartUpException("Double not found for argument " + arg + " value [" + Arrays.deepToString(op) + "]", e);
    }
  }

  private String getStringOption(Argument arg) {
    String[] op = getListOptions(arg);
    try {
      return op[0];
    } catch (Exception e) {
      throw new StartUpException("Double not found for argument " + arg + " value [" + Arrays.deepToString(op) + "]", e);
    }
  }

  private String[] getListOptions(Argument arg) {
    return options.get(arg.getKey().toLowerCase());
  }

  private void addFilter(Filter filter) {
    if (filter != null) {
      this.filters.add(filter);
      if (filter instanceof SampleFilter)
        this.sampleFilters.add((SampleFilter) filter);
      if (filter instanceof LineFilter)
        this.lineFilters.add((LineFilter) filter);
      if (filter instanceof VariantFilter)
        this.variantFilters.add((VariantFilter) filter);
      if (filter instanceof GenotypeFilter)
        this.genotypeFilters.add((GenotypeFilter) filter);
    }
  }

  public String getCommandLine() {
    String ret = "";
    for (String arg : args)
      ret += " " + arg;
    return ret;
  }

  public ArrayList<Filter> getFilters() {
    return filters;
  }

  public ArrayList<SampleFilter> getSampleFilters() {
    return sampleFilters;
  }

  public ArrayList<LineFilter> getLineFilters() {
    return lineFilters;
  }

  public ArrayList<VariantFilter> getVariantFilters() {
    return variantFilters;
  }

  public ArrayList<GenotypeFilter> getGenotypeFilters() {
    return genotypeFilters;
  }

  public static String[] parseOptions(String[] args, String key) {
    for (int i = 0; i < args.length; i++)
      if (key.equals(args[i].toLowerCase())) {
        try {
          if (!args[i + 1].startsWith("--"))
            return args[i + 1].split(",");
        } catch (Exception e) {
        }
        break;
      }
    return null;
  }

  private class MinMaxParser {

    public static final int TYPE_RATIO = 1;
    public static final int TYPE_UNBOUNDED = 2;

    private double min = 0;
    private double max = Double.MAX_VALUE;

    private final boolean valid;

    MinMaxParser(Argument argMin, Argument argMax, int type, String description) {
      if (type == TYPE_RATIO) {
        min = 0;
        max = 1;
      }

      boolean isValid = false;
      if (hasArgument(argMin) || hasArgument(argMax)) { //TODO check here
        if (hasArgument(argMin))
          try {
            min = getDoubleOption(argMin);
            isValid = true;
          } catch (NumberFormatException e) {
            Message.warning("Argument [" + argMin.getKey() + "] must be followed by a valid " + description);
          }
        if (hasArgument(argMax))
          try {
            max = getDoubleOption(argMax);
            isValid = true;
          } catch (NumberFormatException e) {
            Message.warning("Argument [" + argMax.getKey() + "] must be followed by a valid " + description);
          }
      }
      this.valid = isValid;
    }

    public int getMinInt() {
      return (int) min;
    }

    public int getMaxInt(int def) {
      return Math.min((int) max, def);
    }
 
    public int getMinFor(int s){
      return (int) (min*s);
    }
    
    public int getMaxFor(int s){
      return (int) (max*s);
    }

    public double getMinDouble() {
      return min;
    }

    public double getMaxDouble() {
      return max;
    }

    public boolean isValid() {
      return valid;
    }
  }
}
