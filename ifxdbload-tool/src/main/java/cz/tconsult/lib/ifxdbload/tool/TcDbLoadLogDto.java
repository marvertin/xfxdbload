package cz.tconsult.lib.ifxdbload.tool;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TcDbLoadLogDto {

  private String kod;
  private String popis;
  private String owner;
  private LocalDate starttime;
  private LocalDate endtime;

}
