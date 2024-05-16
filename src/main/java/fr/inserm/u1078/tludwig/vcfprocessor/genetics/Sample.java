package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import java.util.ArrayList;
import java.util.Collection;

public class Sample implements Comparable<Sample> {

  /* PED File Description
        Family ID
        Individual ID
        Paternal ID
        Maternal ID
        Sex (1=male; 2=female; other=unknown)
        Phenotype
        Group
   */
  private String fid;
  private final String id;
  private String pid;
  private String mid;
  private int sex;
  private int phenotype;
  private String group;

  public Sample(String line) {
    String[] fields = line.split("\\s+");
    this.fid = fields[0];
    this.id = fields[1];
    this.pid = fields[2];
    this.mid = fields[3];
    this.sex = Integer.parseInt(fields[4]);
    this.phenotype = Integer.parseInt(fields[5]);
    this.group = fields[6];
  }

  public Sample(String fid, String id, String pid, String mid, int sex, int phenotype, String group) {
    this.fid = fid;
    this.id = id;
    this.pid = pid;
    this.mid = mid;
    this.sex = sex;
    this.phenotype = phenotype;
    this.group = group;
  }

  @Override
  public boolean equals(Object obj) { //TODO override hashCode
    if(obj == null)
      return false;
    if(obj instanceof Sample){
      Sample that = (Sample)obj;
      
      
      if(!isEqual(this.fid,that.fid))
        return false;
      if(!isEqual(this.id,that.id))
        return false;
      if(!isEqual(this.mid,that.mid))
        return false;
      if(!isEqual(this.pid,that.pid))
        return false;
      if(!isEqual(this.group,that.group))
        return false;
      
      return(this.sex == that.sex && this.phenotype == that.phenotype);
    }
    return false;
  }
  
  private static boolean isEqual(String s1, String s2){
    if(s1 == null)
      return s2 == null;
    else
      return s1.equals(s2);
  }

  public void setFid(String fid) {
    this.fid = fid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }

  public void setMid(String mid) {
    this.mid = mid;
  }

  public void setSex(int sex) {
    this.sex = sex;
  }

  public void setPhenotype(int phenotype) {
    this.phenotype = phenotype;
  }

  public void setGroup(String group) {
    this.group = group;
  }
  
  public String getFid() {
    return fid;
  }

  public String getId() {
    return id;
  }

  public String getPid() {
    return pid;
  }

  public String getMid() {
    return mid;
  }

  public int getSex() {
    return sex;
  }

  public int getPhenotype() {
    return phenotype;
  }
  
  /**
   * Note 1 = Control 2 = Case (won't work with 0 / 1 files
   * @return  true if phenotype != 2
   */
  public boolean isCase(){
    return this.phenotype == 2;
  }

  public String getGroup() {
    return group;
  }

  public boolean isInGroup(String g) {
    return this.group.equals(g);
  }

  @Override
  public String toString() {
    String T = "\t";
    return this.fid + T + this.id + T + this.pid + T + this.mid + T + sex + T + phenotype + T + group;
  }

  public static ArrayList<String> getCommonIDs(Collection<Sample> lefts, Collection<Sample> rights) {
    ArrayList<String> rightIDs = new ArrayList<>();
    for (Sample right : rights)
      rightIDs.add(right.getId());
    ArrayList<String> common = new ArrayList<>();
    for (Sample left : lefts)
      if (rightIDs.contains(left.getId()))
        common.add(left.getId());

    return common;
  }

  public void apply(Sample pedSample) {
    setFid(pedSample.getFid()) ;
    setPid(pedSample.getPid());
    setMid(pedSample.getMid()) ;
    setSex(pedSample.getSex()) ;
    setPhenotype(pedSample.getPhenotype()) ;
    setGroup(pedSample.getGroup()) ;
  }

  @Override
  public int compareTo(Sample o) {
    return this.getId().compareTo(o.getId());
  }
}
