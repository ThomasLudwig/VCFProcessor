package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ChunkList implements Collection<Chunk> {
  private final ArrayList<Chunk> innerList;

  public ChunkList() {
    this.innerList =new ArrayList<>();
  }

  @Override
  public boolean add(Chunk chunk) {
    if(this.isEmpty())
      return this.innerList.add(chunk);


    int pos = getInsertPos(chunk);
    boolean ml = isMergeableLeft(pos, chunk);
    boolean mr = isMergeableRight(pos, chunk);

    if(ml && mr) {
      Chunk right = innerList.remove(pos);
      Chunk left = innerList.remove(pos - 1);
      innerList.add(pos - 1, Chunk.merge(left, right));
      return true;
    }
    if(ml) {
      Chunk left = innerList.remove(pos - 1);
      innerList.add(pos - 1, Chunk.merge(left, chunk));
      return true;
    }
    if(mr) {
      Chunk right = innerList.remove(pos);
      innerList.add(pos, Chunk.merge(chunk, right));
      return true;
    }

    this.innerList.add(pos, chunk);
    return true;
  }

  private boolean isMergeableLeft(int pos, Chunk chunk) {
    if(pos < 1)
      return false;
    return chunk.isMergeable(get(pos - 1));
  }

  private boolean isMergeableRight(int pos, Chunk chunk) {
    if(pos > innerList.size() - 1)
      return false;
    return chunk.isMergeable(get(pos));
  }

  private int getInsertPos(Chunk chunk) {
    if (this.isEmpty())
      return 0;
    else {
      int lo = 0;
      int hi = this.size() - 1;

      while(hi - lo > 1) {
        int c = (lo + hi) / 2;
        Chunk g = this.get(c);
        int compare = g.compareTo(chunk);
        if (compare == 0)
          return c;
        if (compare < 0)
          lo = c;
        else
          hi = c;
      }

      if (chunk.compareTo(this.get(lo)) <= 0)
        return lo;
      else if (chunk.compareTo(this.get(hi)) <= 0)
        return hi;
      else
        return hi+1;
    }
  }

  @Override
  public int size() {
    return this.innerList.size();
  }

  @Override
  public boolean isEmpty() {
    return this.innerList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return this.innerList.contains(o);
  }

  @Override
  public Iterator<Chunk> iterator() {
    return this.innerList.iterator();
  }

  @Override
  public Object[] toArray() {
    return this.innerList.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return this.innerList.toArray(a);
  }

  @Override
  public boolean remove(Object o) {
    return this.innerList.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return this.innerList.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends Chunk> c) {
    for(Chunk chunk : c)
      this.add(chunk);
    return true;
  }

  public boolean addAll(Chunk... c) {
    for(Chunk chunk : c)
      this.add(chunk);
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return this.innerList.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return innerList.retainAll(c);
  }

  @Override
  public void clear() {
    this.innerList.clear();
  }

  @Override
  public boolean equals(Object o) {
    if(! (o instanceof ChunkList))
      return false;
    ChunkList that = (ChunkList)o;
    return this.innerList.equals(that.innerList);
  }

  @Override
  public int hashCode() {
    return innerList.hashCode();
  }

  public Chunk get(int idx){
    return this.innerList.get(idx);
  }
}
