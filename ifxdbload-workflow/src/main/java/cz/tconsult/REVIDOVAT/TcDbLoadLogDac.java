package cz.tconsult.REVIDOVAT;

/**
 * !!! zatial nevyskusany dac ... nutne preskusat, opravit
 *
 * @author tomas
 *
 */
public class TcDbLoadLogDac  {
  // TODO [veverka] revidovat a když tak udělat přes spring-jdbc -- 25. 2. 2019 13:15:15 veverka

  //  private static final String SQL = "SELECT serno, kod, popis, owner, starttime, endtime FROM tcdbload_log where serno > ? ";
  //
  //  public TcDbLoadLogDac(final Connection aConn) {
  //    super(aConn, SQL, EDacCardinality.ANY);
  //  }
  //
  //  public void setSerno(final int aSerno){
  //    pstm().setInt(0, new Integer(aSerno));
  //  }
  //
  //  // serno
  //  public int getSerno() {
  //    return rs().getInt(0);
  //  }
  //
  //  // kod
  //  public String getKod() {
  //    return StringUtils.trimToEmpty(rs().getString(1));
  //  }
  //
  //  // popis
  //  public String getPopis() {
  //    return StringUtils.trimToEmpty(rs().getString(2));
  //  }
  //
  //  // owner
  //  public String getOwner() {
  //    return StringUtils.trimToEmpty(rs().getString(3));
  //  }
  //
  //  // starttime
  //  public ATimestamp getStartTime() {
  //    return rs().getATimestamp(4);
  //  }
  //
  //  // endtime
  //  public ATimestamp getEndTime() {
  //    return rs().getATimestamp(5);
  //  }

}
