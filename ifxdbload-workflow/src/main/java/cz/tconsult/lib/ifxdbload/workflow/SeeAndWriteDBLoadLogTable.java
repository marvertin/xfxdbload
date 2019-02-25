package cz.tconsult.lib.ifxdbload.workflow;

import java.sql.Connection;
import java.time.Instant;

//import java.sql.Connection;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//

/**
 * Pomocná třída pro olizovač tabulky tc.dbload_log. Pozrie sa do tabulky a vypisuje novo pridavane udaje do logu.
 *
 * @author tomas
 */
public class SeeAndWriteDBLoadLogTable/* extends DbPgmBase */{

  public SeeAndWriteDBLoadLogTable(final Connection connNew, final Instant toolStartTime) {
    // TODO Auto-generated constructor stub
  }

  public void init() {
    // TODO [veverka] Dát sem skutečnou implementaci -- 25. 2. 2019 14:49:36 veverka
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 14:49:23]

  }

  public void endSeeAndWrite() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 14:50:03]

  }

  public void beginSeeAndWrite() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 14:50:15]

  }

  // TODO [veverka] Revidovat a případně obnovit -- 25. 2. 2019 13:14:42 veverka
  //  private static final Logger log = LoggerFactory.getLogger(SeeAndWriteDBLoadLogTable.class);
  //
  //
  //  private final Connection conn;
  //  private final Instant toolStartTime;
  //
  //  private int maxSer;
  //  private SeeAndWriteDBLoadLogTableThread thread;
  //
  //  public SeeAndWriteDBLoadLogTable(final Connection connection, final Instant aToolStartTime){
  //    conn = connection;
  //    toolStartTime = aToolStartTime;
  //  }
  //
  //  public void init(){
  //    // zistim max serno
  //    maxSer = getMaxSernoFromDBLoadLog();
  //  }
  //
  //  public void beginSeeAndWrite() throws Exception {
  //
  //    thread = new SeeAndWriteDBLoadLogTableThread(conn, toolStartTime, maxSer);
  //    thread.start();
  //  }
  //
  //  public void endSeeAndWrite() throws Exception {
  //
  //    thread.interrupt();
  //    maxSer = thread.getCurrentMaxSer();
  //  }
  //
  //  private int getMaxSernoFromDBLoadLog(){
  //
  //    final TcDbLoadLogMaxDac dac = new TcDbLoadLogMaxDac(conn);
  //    if (!dac.next()) {
  //
  //      throw new UnsupportedOperationException();
  //    }
  //    final Integer i = dac.getMaxSerno();
  //    dac.releaseResources();
  //
  //    final int result = i == null ? -1 : i;
  //    return result;
  //  }
  //
  //  private static class SeeAndWriteDBLoadLogTableThread extends Thread {
  //
  //    /**
  //     * Zamykač mezi vlákny.
  //     */
  //    private final Lock lock = new ReentrantLock();
  //
  //    /**
  //     * DB spojení pro toto vlákno.
  //     */
  //    private final Connection conn;
  //
  //    /**
  //     * Předaný čas spuštění celého nástroje.
  //     */
  //    private final Instant toolStartTime;
  //
  //    /**
  //     * Zkonstruovaný DAC preo toto vlákno.
  //     */
  //    private TcDbLoadLogDac dac;
  //
  //    /**
  //     * Nejvyšší ID právě zobrazovaného záznamu.
  //     */
  //    private int currentMaxSer;
  //
  //    /**
  //     * Zda jsme vypsali již celý záznam, anebo zda yobrazujeme průběžku.
  //     */
  //    boolean recordFinished;
  //
  //    public SeeAndWriteDBLoadLogTableThread(final Connection aConn, final Instant aToolStartTime, final int aMaxSer){
  //
  //      conn = aConn;
  //      toolStartTime = aToolStartTime;
  //      currentMaxSer = aMaxSer;
  //      recordFinished = true;
  //    }
  //
  //    public void show() {
  //      dac.setSerno(currentMaxSer);
  //
  //      boolean isNext = dac.next();
  //
  //      final Instant now = Instant.now();
  //      String elapsedTime = FUtils.prettyDuration(Duration.between(toolStartTime, now)
  //          , true/*showLeftZeros*/, true/*showMilliseconds*/);
  //
  //      if (!isNext) {
  //
  //        //RN00389611 - nechce se to [polakm;2014-06-26 08:01:39]
  //        //log.info("%s - neškodné info: nenalezen žádný nový záznam v logovací tabulce", elapsedTime);
  //      }
  //      else {
  //        while (isNext){
  //
  //
  //          final Instant startTime = dac.getStartTime();
  //          final Instant endTime = dac.getEndTime();
  //
  //          //System.out.println("DBG (" + currentMaxSer + "): " + dac.getPopis() + ", " + startTime + ", " + endTime);
  //
  //          if(recordFinished){
  //            log.info("%s - START: %s [%s], Čas startu: %s", elapsedTime, dac.getPopis(), dac.getOwner()
  //                , startTime);
  //          }
  //
  //
  //          if (endTime != null){
  //
  //            recordFinished = true;
  //            currentMaxSer = dac.getSerno();
  //
  //          } else {
  //
  //            recordFinished = false;
  //          }
  //
  //          if (recordFinished){
  //
  //            final String duration = FUtils.prettyDuration(Duration.between(startTime, endTime), false /*showLeftZeros*/, true/*showMilliseconds*/);
  //            final Instant actualEndTime = Instant.now();
  //            elapsedTime = FUtils.prettyDuration(Duration.between(toolStartTime, actualEndTime)
  //                , true/*showLeftZeros*/, true/*showMilliseconds*/);
  //            log.info("%s - KONEC: %s [%s], Čas ukončení: %s, Celkový čas: %s"
  //                , elapsedTime, dac.getPopis(), dac.getOwner()
  //                , FUtils.getHourMinSecMili(endTime), duration);
  //          }
  //          else {
  //
  //            final Instant at = Instant.now();
  //            final Duration atDuration =  Duration.between(startTime,  at);
  //            final String atDur = FUtils.prettyDuration(atDuration
  //                , true/*showLeftZeros*/, true/*showMilliseconds*/);
  //
  //            log.info(" ..... průběh: %s [%s], průbežný čas: %s", dac.getPopis(), dac.getOwner(), atDur);
  //          }
  //          isNext = dac.next();
  //        }//end of while
  //      }
  //    }//end of show()
  //
  //
  //    public void lastShow() {
  //
  //      show();
  //      dac.releaseResources();
  //    }
  //
  //    public int getCurrentMaxSer() {
  //
  //      int result;
  //      try {
  //
  //        lock.lock();
  //        result = currentMaxSer;
  //      }
  //      finally {
  //
  //        lock.unlock();
  //      }
  //      return result;
  //    }
  //
  //    @Override
  //    public void run() {
  //
  //      try {
  //        //zamčeme až do provedení lastShow(), teprve pak je možno zjišťovat například currentMaxSer
  //        lock.lock();
  //        dac = new TcDbLoadLogDac(conn);
  //        while(true){
  //
  //          try {
  //            show();
  //            Thread.sleep(10 * 1000);
  //          } catch (final InterruptedException e) {
  //
  //            lastShow();
  //            return;
  //          }
  //        }
  //      }
  //      finally {
  //        lock.unlock();
  //      }
  //    }
  //  }
}

