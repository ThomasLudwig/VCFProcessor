package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Fasta;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class FastaFileParameter extends FileParameter {

  public FastaFileParameter(String key, String example, String description) {
    super(key, example, description);
  }
  
  public FastaFileParameter(){
    super(Function.OPT_REF, "Reference.fasta", "Fasta File containing the reference genome");
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"fasta", "fa"};
  }

  public Fasta getFasta() throws FastaException {
    return new Fasta(this.getFilename());
  }
}
