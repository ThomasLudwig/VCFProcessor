package fr.inserm.u1078.tludwig.vcfprocessor.files;

public class AbstractRecord {
  public static final String T = "\t";
  private boolean isFiltered = false;

  /**
   * Flag the Record as "Filtered"
   * @param rp the RecordProducer that filters the Record
   */
  public final void filter(RecordProducer rp){
    if(!this.isFiltered)
      rp.filter();
    this.isFiltered = true;
  }

  public final boolean isFiltered(){
    return this.isFiltered;
  }
}
