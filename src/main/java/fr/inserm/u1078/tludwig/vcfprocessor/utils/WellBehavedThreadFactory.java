package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import java.util.concurrent.ThreadFactory;

public class WellBehavedThreadFactory implements ThreadFactory {

  @Override
  public Thread newThread(Runnable r) {
    return new WellBehavedThread(r);
  }
}
