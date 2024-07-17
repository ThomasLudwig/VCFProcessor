package fr.inserm.u1078.tludwig.vcfprocessor.files;

/**
 * Exception related to VCF
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 23 juin 2015
 */
public class VCFException extends FileFormatException {

  public VCFException(VCF vcf, String message) {
    super(getMessage(vcf, message));
  }

  public VCFException(VCF vcf, String message, Throwable cause) {
    super(getMessage(vcf, message, cause), cause);
  }

  public VCFException(VCF vcf, String message, VariantRecord record) {
    super(getMessage(vcf, message, record));
  }

  public VCFException(VCF vcf, String message, VariantRecord record, Throwable cause) {
    super(getMessage(vcf, message, record, cause), cause);
  }

  public static String getMessage(VCF vcf, String message){
    return getPrefix(vcf) + message;
  }

  public static String getMessage(VCF vcf, String message, VariantRecord record) {
    return getMessage(vcf, message) + getSuffix(record);
  }

  public static String getMessage(VCF vcf, String message, Throwable cause){
    return getMessage(vcf, message) + " ["+cause.getClass().getSimpleName()+" : "+cause.getMessage()+"]";
  }

  public static String getMessage(VCF vcf, String message, VariantRecord record, Throwable cause){
    return getMessage(vcf, message, cause) + getSuffix(record);
  }

  public static String getPrefix(VCF vcf) {
    return "In VCF file ["+vcf.getFilename()+"] :";
  }

  public static String getSuffix(VariantRecord record) {
    if(record == null)
      return " | VariantRecord is null";
    return " | For VariantRecord:\n"+record.summary(10);
  }
/*
  public VCFException(String message) {
    super(message);
  }

  public VCFException(String message, Throwable cause) {
    super(message, cause);
  }

  public VCFException(Throwable cause) {
    super(cause);
  }

  public VCFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }*/

}
