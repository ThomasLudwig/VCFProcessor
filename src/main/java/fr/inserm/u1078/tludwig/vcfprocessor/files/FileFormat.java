package fr.inserm.u1078.tludwig.vcfprocessor.files;

public interface FileFormat {

  public String[] knownExtensions();

  public String fileFormatDescription();
}
