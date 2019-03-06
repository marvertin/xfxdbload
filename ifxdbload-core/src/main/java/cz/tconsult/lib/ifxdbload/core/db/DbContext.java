package cz.tconsult.lib.ifxdbload.core.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Data;


@Data
public class DbContext {
  private final TransactionTemplate tt;
  private final JdbcTemplate jt;

}
