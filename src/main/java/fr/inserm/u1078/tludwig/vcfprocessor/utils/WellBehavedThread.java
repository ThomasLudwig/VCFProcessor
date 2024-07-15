package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import fr.inserm.u1078.tludwig.maok.tools.Message;

public class WellBehavedThread extends Thread implements Thread.UncaughtExceptionHandler {
  private final Runnable delegate;

  public WellBehavedThread() {
    this.setUncaughtExceptionHandler(this);
    delegate = null;
  }

  public WellBehavedThread(Runnable target) {
    super(target);
    this.delegate = target;
    this.setUncaughtExceptionHandler(this);
  }

  public WellBehavedThread(ThreadGroup group, Runnable target) {
    super(group, target);
    this.delegate = target;
    this.setUncaughtExceptionHandler(this);
  }

  public WellBehavedThread(String name) {
    super(name);
    delegate = null;
    this.setUncaughtExceptionHandler(this);
  }

  public WellBehavedThread(ThreadGroup group, String name) {
    super(group, name);
    delegate = null;
    this.setUncaughtExceptionHandler(this);
  }

  public WellBehavedThread(Runnable target, String name) {
    super(target, name);
    this.delegate = target;
    this.setUncaughtExceptionHandler(this);
  }

  public WellBehavedThread(ThreadGroup group, Runnable target, String name) {
    super(group, target, name);
    this.delegate = target;
    this.setUncaughtExceptionHandler(this);
  }

  public WellBehavedThread(ThreadGroup group, Runnable target, String name, long stackSize) {
    super(group, target, name, stackSize);
    this.delegate = target;
    this.setUncaughtExceptionHandler(this);
  }

  public WellBehavedThread(ThreadGroup group, Runnable target, String name, long stackSize, boolean inheritThreadLocals) {
    super(group, target, name, stackSize, inheritThreadLocals);
    this.delegate = target;
    this.setUncaughtExceptionHandler(this);
  }

  public void doRun(){};

  @Override
  public final void run() {
    //this is needed in Executors, to wrap Thread and have the Exception caught
    try {
      if (delegate != null)
        delegate.run();
      else
        doRun();
    } catch(Throwable e) {
      uncaughtException(this, e);
    }
  }

  @Override
  public String toString() {
    if(this.delegate != null)
      return this.getClass().getSimpleName()+"["+this.delegate.toString()+"]";
    return this.getClass().getSimpleName()+"("+this.getName()+")";
  }

  @Override
  public final void uncaughtException(Thread t, Throwable e) {
    Message.fatal("This Thread ["+this+"] has stopped", e, false);
  }
}
