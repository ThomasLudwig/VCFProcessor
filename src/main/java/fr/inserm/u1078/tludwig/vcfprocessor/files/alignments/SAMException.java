package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

public class SAMException extends Exception {
  public static final String NULL_RAW = "RAW Record is null";
  public SAMException(AlignmentProducer ap, String message) {
    super(getMessage(ap, message));
  }

  public SAMException(AlignmentProducer ap, String message, Throwable cause) {
    super(getMessage(ap, message, cause), cause);
  }

  public SAMException(AlignmentProducer ap, String message, AlignmentRecord record) {
    super(getMessage(ap, message, record));
  }

  public SAMException(AlignmentProducer ap, String message, AlignmentRecord record, Throwable cause) {
    super(getMessage(ap, message, record, cause), cause);
  }

  public static String getMessage(AlignmentProducer ap, String message){
    return getPrefix(ap) + message;
  }

  public static String getMessage(AlignmentProducer ap, String message, AlignmentRecord record) {
    return getMessage(ap, message) + getSuffix(record);
  }

  public static String getMessage(AlignmentProducer ap, String message, Throwable cause){
    return getMessage(ap, message) + " ["+cause.getClass().getSimpleName()+" : "+cause.getMessage()+"]";
  }

  public static String getMessage(AlignmentProducer ap, String message, AlignmentRecord record, Throwable cause){
    return getMessage(ap, message, cause) + getSuffix(record);
  }

  public static String getPrefix(AlignmentProducer ap) {
    return "In SAM/BAM/CRAM file ["+ap.getFilename()+"] :";
  }

  public static String getSuffix(AlignmentRecord record) {
    if(record == null)
      return " | AlignmentRecord is null";
    return " | For AlignmentRecord:\n"+record;
  }

  public static class InvalidCigarException extends SAMException {
    public InvalidCigarException(AlignmentProducer ap, String cigar, String fault) {
      super(ap, "Invalid character in cigar ["+fault+"] for {"+cigar+"}");
    }
  }

}
