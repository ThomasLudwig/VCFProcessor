/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class FileParameter extends Parameter {

  private String value;
  private Path path;
  private UniversalReader reader = null;

  public FileParameter(String key, String example, String description) {
    super(key, example, description);
  }

  public String[] getExtensions() {
    return new String[]{};
  }

  @Override
  public String toString() {
    return this.value;
  }

  public String getFilename() {
    return this.value;
  }
  
  public String getFullPath(){
    try {
      File f = new File(this.getFilename());
      return f.getAbsolutePath();
    } catch (Exception e) {
      return this.getFilename();
    }    
  }
  
  public Path getPath(){
    if(this.path == null)
      path = Paths.get(this.getFilename());
    return path;
  }

  @Override
  public String showAllowedValues() {
    return "Filename";
  }

  @Override
  public void parseParameter(String s) {
    this.value = s;
  }

  public UniversalReader getReader() throws IOException {
    if(this.reader == null || this.reader.isClosed())
      this.reader = new UniversalReader(this.getFilename());
    return this.reader;
  }

  public String getBasename() {
    String ret = this.getFilename();
    for (String ext : this.getExtensions()) {
      String tmp = this.getFilename();
      if (tmp.toLowerCase().endsWith("." + ext.toLowerCase()))
        tmp = tmp.substring(0, tmp.length() - (1 + ext.length()));
      if (ret.length() > tmp.length())
        ret = tmp;
    }
    
    return ret.substring(ret.lastIndexOf(File.separatorChar)+1);
  }
}
