package cz.tconsult.lib.ifxdbload.core.loaders.trgxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.lib.ifxdbload.core.loaders.trgxml.AutomaticTriggersSplStatementGenerator.EColumnsHandleMode;

public class XmlAutoTiggerParser {


  private static final Logger log = LoggerFactory.getLogger(XmlAutoTiggerParser.class);

  List<AutoTrigger> iTriggers=new ArrayList<AutoTrigger>();

  private XmlTrigData iFile;

  private AutomaticTriggersSplStatementGenerator iAutCreator;

  public List<AutoTrigger> parse(final XmlTrigData aXtd) {
    iTriggers.clear();
    try {
      parseXMLFileWithAutoTriggers(aXtd);
    } catch (IOException | JDOMException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return iTriggers;
  }


  private void parseXMLFileWithAutoTriggers(final XmlTrigData aXtd) throws IOException,
  JDOMException {
    SAXBuilder saxBuilderA;
    org.jdom.Document docA;
    org.jdom.Element rootElemA;
    List<Element> tableElemsA=null;

    iFile=aXtd;

    // *** Načtení jdom-dokumentu z "aFileEle" do "docA"
    saxBuilderA = new SAXBuilder();

    docA = saxBuilderA.build(new ByteArrayInputStream(aXtd.getData()), aXtd.getPath());
    rootElemA = docA.getRootElement();
    rootElemA = rootElemA.getChild("tables");
    tableElemsA = getJdomChildren(rootElemA, "table");

    // *** Iterace přes všechny tabulky

    for (final Element tableElemA : tableElemsA) {

      final ATableName tableName = ATableName.from(tableElemA.getAttributeValue("name").toLowerCase());
      final List<Element> triggerElemsA = getJdomChildren(tableElemA, "trigger");

      // Pokud v této tabulce nejsou žádné triggery, tak vytvoř
      // trigger typu "NONE" (tj. trigger nettriger) za účelem smazání
      // starých triggerů z této tabulky
      boolean bCreateNoneTriggerA = true;


      // *** Iterace přes všechny triggery v dané tabulce

      for (final Element triggerElemA : triggerElemsA) {
        final AutoTrigger trigger=new AutoTrigger();
        trigger.setTableNameA(tableName);

        bCreateNoneTriggerA = false;

        trigger.setArchTableNameA(ATableName.from(StringUtils.lowerCase(triggerElemA.getAttributeValue("arch-table"))));

        try {
          //typ triggeru
          trigger.setTriggerType(triggerElemA.getAttributeValue("type") != null ? ETriggerType.valueOf(triggerElemA.getAttributeValue("type").toUpperCase()) : null);
          //event triggeru
          trigger.setTriggerEvent(triggerElemA.getAttributeValue("event") != null ? ETriggerEvent.valueOf(triggerElemA.getAttributeValue("event").toUpperCase()) : null);
        }
        catch (final IllegalArgumentException e){
          throw new RuntimeException("\nSpatny typ nebo event triggeru. Typ je :" +triggerElemA.getAttributeValue("type")+ " a event: "+triggerElemA.getAttributeValue("event")+"\ns orig. chybou: "+e);
        }


        org.jdom.Element columnsElemA;

        // Pozn. Následuje čtení sloupců, které se uplatní,
        //       a pak čtení sloupců, které se neuplatní.
        //       Mělo by však platit, že jsou to vzájemně exkluzivní operace.
        //       Tzn. pokud existuje element "incl-columns", tak by neměl existovat
        //       element "excl-columns".

        Set<AColumnName> columnsToIncludeA = null;
        Set<AColumnName> columnsToExcludeA = null;

        // ** Načtení sloupců, které se uplatní
        columnsElemA = triggerElemA.getChild("incl-columns");
        if (columnsElemA != null) {
          columnsToIncludeA = new LinkedHashSet<AColumnName>();
          final List<Element> columnsElemsA = getJdomChildren(columnsElemA, "column");
          for (final Element columnElemA : columnsElemsA) {
            columnsToIncludeA.add(
                AColumnName.from(columnElemA.getText())
                );
          } // for (Iterator k = columnsElemsA.iterator();k.hasNext();)
        } // if (columnsElemA != null)

        // ** Načtení sloupců, které se neuplatní
        columnsElemA = triggerElemA.getChild("excl-columns");
        if (columnsElemA != null) {
          columnsToExcludeA = new LinkedHashSet<>();
          final List<Element> columnsElemsA = getJdomChildren(columnsElemA, "column");
          for (final Element columnElemA : columnsElemsA) {
            columnsToExcludeA.add(
                AColumnName.from(columnElemA.getText())
                );
          } // for (Iterator k = columnsElemsA.iterator();k.hasNext();)
        } // if (columnsElemA != null)

        // ** Nyní máme k dispozici všechny potřebné údaje pro vytvoření automatic-triggeru.
        //    Mezi tyto informace patří: jméno tabulky, typ triggeru a sloupce,
        //    které se mají uplatnit a sloupce, které se naopak uplatnit nemají.

        //iBuilder.buildSoAutomaticTrigger(tableNameA,archTableNameA,triggerTypeA,
        //                                  columnsToIncludeA,columnsToExcludeA);
        trigger.setColumnsToExcludeA(columnsToExcludeA);
        trigger.setColumnsToIncludeA(columnsToIncludeA);
        iTriggers.add(trigger);

      } // for (Iterator j = triggerElemsA.iterator();j.hasNext();)

      if (bCreateNoneTriggerA) {
        // *** Vytvoření triggeru typu "NONE" (resp. "-").
        //     Jde o trigger-nettrigger a slouží pouze k tomu, aby se smazaly
        //     doposud existující triggery na dané tabulce.
        //iBuilder.buildSoAutomaticTrigger(tableNameA,null, "-", null, null);
      } // if (bCreateNoneTriggerA)
    } // for (Iterator i = tablesA.iterator();i.hasNext();)

  } // private void parseXMLFileWithAutoTriggers

  private List<Element> getJdomChildren(final Element aEle, final String aChildName) {
    @SuppressWarnings("unchecked")
    final
    List<Element> children = aEle.getChildren(aChildName);
    return children;
  }

  private void loadAutTriggers2db(final AutomaticTriggersSplStatementGenerator aCreator) throws SQLException {

    iAutCreator=aCreator;
    for(final AutoTrigger trigger : iTriggers){
      log.debug("Zpracovava se trigger typu :"+trigger.getTriggerType()+ " s eventem: "+trigger.getTriggerEvent()+" pro tabulku "+trigger.getTableNameA());

      if (trigger.getColumnsToIncludeA() != null) {
        iAutCreator.makeTrigger(
            trigger.getTableNameA(),
            trigger.getArchTableNameA(),
            trigger.getTriggerType(),
            trigger.getTriggerEvent(),
            trigger.getColumnsToIncludeA(),
            EColumnsHandleMode.INCLUDE,
            iFile);
      }
      else if (trigger.getColumnsToExcludeA() != null) {
        iAutCreator.makeTrigger(
            trigger.getTableNameA(),
            trigger.getArchTableNameA(),
            trigger.getTriggerType(),
            trigger.getTriggerEvent(),
            trigger.getColumnsToExcludeA(),
            EColumnsHandleMode.EXCLUDE,
            iFile);
      }
      else {
        iAutCreator.makeTrigger(
            trigger.getTableNameA(),
            trigger.getArchTableNameA(),
            trigger.getTriggerType(),
            trigger.getTriggerEvent(),
            null,
            EColumnsHandleMode.NOTHING,
            iFile);
      }
    }
  }

  public void loadAutTriggers2db(final XmlTrigData aFile,final AutomaticTriggersSplStatementGenerator aCreator)
      throws JDOMException,IOException,SQLException{
    iAutCreator=aCreator;
    iTriggers.clear();
    parseXMLFileWithAutoTriggers(aFile);
    loadAutTriggers2db(aCreator);
  }



}
