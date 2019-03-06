package cz.tconsult.lib.ifxdbload.workflow.process;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.workflow.data.LoFaze;

/**
 * Vykonání jed né konkrétní fáze pro jeden konkrétní druh a schema.
 * Metody se volají ve workflow v uvedeném pořadí.
 *
 * @author veverka
 *
 */
public interface FazeExecutor {


  /**
   * Inicializuje exekutor. Bude volána jako první metoda exekutoru.
   * Jediné, co se smí zde udělat, je umístit si předané hdonoty do sebe, popřípadě provést jednoduché in memory zpracování a připravit se tak na zavádění.
   * Nesmí selhat, nelze zde nic kontrolovat, nesmí trvat. Metoda je volána pro každou fázi bez ohledu na to, zda bude či nebude přeskočena.
   *
   * Metoda dostává data, která bude zavádět.
   * @param board
   * @param faze
   */
  public void init(LoFaze faze, InterFazeBoard board);

  /**
   * Fáze může říct, že může být zavolána i když neobsahuje žádná data k zavádění.
   * Dobré pro speciální fáze, které mají udělat něco, co není zavádění dat.
   * @return Konstantu.
   */
  public boolean isLoadIfEmty();


  /**
   * Umožní rozhodnout, zda zavádět, či nikoli.
   * Metoda nesmí trvat dlouho, protože, pokud vrátí false, považuje se fáze za přeskočenou.
   * Metoda je volána jen tehdy, když jsou nějaká data na zavedení nebo když sice data nejsou, ale isLoadIfEmpty, vrátilo false.
   *
   * Musí rozhodnotu bez ohledu na to, co říkají jiné fáze.
   *Smí použít data vyrobená v init, ale ne v prepare.
   * @return
   */
  public boolean shouldLoad();

  /**
   * Provede přípravné práce pro zavádění, ještě bez databází.
   * Metoda bude provolána pro všechny fáze a může selhat při závažném problému. V tom případě se nezavádí a veškeré přípravy se okamžitě zastaví.
   * Nesmí selhat při syntaktických chybách v zaváděných datech.
   *
   * Metoda je zavolána jen pro fáze, které se nakonec budou zavádět.
   */
  public void prepare();

  /**
   * Provede zavedení. Již se ví, že je co zavádět a, že se má zavádět.
   * @param ctx
   * @return Zpráva do logu o tom, kolik se toho zavedlo.
   */
  public String execute(LoadContext ctx);


  /**
   * Fáze má být vynechána. Přesto může být potřeba něco udělat, aby další fáze uspěly. Třeba dát něco do boardu.
   * V žádném případě by se však zde nemělo zavádět.
   * @param ctx
   */
  public void skip();


}
