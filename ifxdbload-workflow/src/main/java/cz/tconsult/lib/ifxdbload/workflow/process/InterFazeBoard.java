package cz.tconsult.lib.ifxdbload.workflow.process;

import lombok.Data;

/**
 * Tabule, na kterou si mohou fáze mezi sebou předávat data.
 * Objekt bude sdílený všem fázím.
 *
 * Co sem fáze umístí, tak to další fáze najde.
 * Musí si však dát ůozor, že fáze mohla být přeskočena, takže data zde nemusí být.
 * Nebo jde problém obejít implementací metody skip, která nějaká prázdná data na board umístí.
 *
 * Mělo by toho zde být co možná nejméně, jinak to přestane být přehledné. V každém případě to chce dobrý popis.
 * @author veverka
 *
 */
@Data
public class InterFazeBoard {
  private int dummy;

}
