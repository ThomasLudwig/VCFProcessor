package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 20 nov. 2015
 */
public abstract class VCFFunction extends Function implements VCFHandling {
  public final VCFFileParameter vcfFile = new VCFFileParameter(OPT_VCF, "input.vcf(.gz)", "VCF file to use as an input. Can be bgzipped");

  //TODO also in Functions using --vcf but that do not extend VCFFunction (ie MergeVQSR)
  @Override
  public final Description getDescription() {
    Description desc = this.getDesc();
    if (getVCFPolicies().isNeedVEP())
      desc.addWarning("The input VCF File must have been previously annotated with vep.");
    for(String custom : getVCFPolicies().getCustomRequirements())
      if (custom != null && !custom.isEmpty())
        desc.addWarning(custom);
    VCFPolicies.MultiAllelicPolicy multi = getVCFPolicies().getMultiAllelicPolicies();
    if(!VCFPolicies.MultiAllelicPolicy.NA.equals(multi)) desc.addNote(multi.getDescription());
    return desc;
  }
}
