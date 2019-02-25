package cz.tconsult.tw.util;

import cz.tconsult.tw.annotation.DeveloperSettings;

/**
 * Základní nastavení jádra aplikací.
 */
@DeveloperSettings(prefix="base")
public interface DeveloperSettingBase {

  public static final DeveloperSettingBase cfg = DeveloperSettingProvider.register(DeveloperSettingBase.class);

  /**
   * Dumpovanou výjimku také vypíše do standardního výpisu, aby
   * ve vývojovém prostředí bylo možné klikat na metody v zásobníku
   * za účelem pozicování kurzoru ve zdrojovém kódu.
   */
  public boolean isPrintDumpedExceptionMessageToStdErr();

  /**
   * Za normálních okolností, pokud volám classloader.getResourceAsStream("..."),
   * Java neumožňuje "hot deploy" resourců. Aby se nová verze resourců projevila, musí se aplikace restartovat.
   * Toto nastavení způsobí, že se veškeré resourcy získávané přes FResource.getResourceAsStream
   * otvírají pokaždé znovu, takže mají čerstvý obsah. Tato funkcionalita je nyní podporována pouze pro resourcy v souborech.
   * Proto doporučuji v programech používat pro otvírání resourců vždy FResource.getResourceAsStream.
   */
  public boolean isNotCacheResources();

  /**
   * Způsobí, že SQL příkazy natahované pomocí <tt>SqlCommandManager</tt>
   * nebudou cachovány, ale budou vždy načítány znovu.
   * To umožní vývojáři měnit soubor s SQL příkazem za běhu aplikace. Změny se okamžitě projeví.
   * Tato volba bude však mít účinnost pouze s volbou <tt>notCacheResources</tt>
   * v prostředí, kde resourcy jsou ve filesystému.
   */
  public boolean isNotCacheSqlCommands();


  /**
   * Způsobí, že na webové stránce s výjimkou bude odkaz, po jehož prokliknutí
   * bude výjimka přímo zobrazena.
   */
  public boolean isExceptionsAccessibleInUserInterface();

  /**
   * Způsobí, že se při každé výjimce bude zaznamenávat stack trace všech threadů.
   * Slouží především k hledání problémů se zamykáním tabulek.
   */
  public boolean isDumpStackForAllThreadsOnException();


  /**
   * Zajistí, že budou získány a zobrazeny informace o verzích programu, aplikace a systému ze speciálního souboru <tt>developer_wholeversion.properties</tt>.
   * V tomto souboru se liší všechny názvy, takže lze sledovat, zda umísťujeme správné názvy na správné místo uživatelského rozhraní.
   */
  public boolean isDeveloperVersionInfo();


  /**
   * Zapíná podporu diagnostiky bez ohledu na práva. Při zapnutí tohoto nastavení je diagnostika zapnutá,
   * diagnostický bar je zobrazen neustále a všechny diagnostické informace jsou k dispozici.
   * Pokud není toto vývojářské nastavení zapnuto, lze diagnostiku zapnout na základě speciálních oprávnění a to
   * "system-administration.services.debug-j2ee".
   */
  public boolean isDiagnosticsMode();

  /**
   * Potlačí šifrování hodnot typu SERIAL posílaných přes HTTP.
   * Umožňuje programátorům zjišťovat, zda zjištěný problém není způsoben právě šifrováním hodnot SERIAL.
   * Používejte výjimečně, jen pokud máte podezření, že jsou s šifrováním problémy. Protože je součástí
   * šifrované hodnoty též označení Java třídy, může se stát, že bez šifrování některé úlohy nebudou fungovat.
   */
  public boolean isSernoEncryptionSuppressed();

  /**
   * Zobrazí původní nezašifrovanou hodnotu jako součást zašifrované hodnoty typu SERIAL.
   * Při rozšifrování se na tuto hodnotu nebere ohled.
   * Toto nastavení je vhodné při ladění byznysu, kdy chceme ve zdrojovém HTML kódu
   * vidět nezašifrované hodnoty a párovat je jednoduše s databází.
   */
  public boolean isSernoDisplayRawValue();

  /**
   * Zobrazí jméno třídy jako součást zašifrované hodnoty typu SERIAL.
   * Při rozšifrování se na tuto hodnotu nebere ohled.
   * Toto nastavení je vhodné při ladění byznysu, kdy chceme ve zdrojovém HTML kódu
   * vidět hodnoty typu SERIAL a párovat je jednoduše s databází.
   */
  public boolean isSernoDisplayClassName();

  /**
   * Sice šifruje hodnoty typu SERIAL, ale akceptuje i samostatné číslo jako nezašifrovanou hodnotu.
   * Tato volba je vhodná pro případ, kdy potřebujeme napsat při ladění byznysu URL ručně, známe hodnotu typu SERIAL
   * a potřebujeme ho předat. Pozor, že v tomto případě není předáno jméno třídy a tak některé úlohy nemusejí fungovat.
   */
  public boolean isSernoAcceptRawValue();


  /**
   * Použije vždy stejný NENÁHODNÝ šifrovací klíč bez ohledu na sezení i na spuštění serveru.
   * Je vhodné pro vývoj, kdy potřebujeme měnit program zobrazující nějaký detail, znovu spouštět aplikaci
   * a nechceme znovu procházet všechny stránky. Toto nastavení vyžaduje restart serveru, aby začalo fungovat.
   */
  public boolean isSernoAllwaysTheSameKey();


  /**
   * Zablokuje šifrování hodnot typu SERIAL v JSON. Byla opravena bezpečnostní chyba, kdy se v JSON hodnoty typu SERIAL nešifrovaly.
   * Tato změna by mohla vést k dalekosáhlým dopadům v aplikaci. Aby bylo snažší zjistit, zda se daný problém týká
   * šifrování hodnot typu SERIAL přes JSON, bylo zavedeno toto nastavení.
   */
  public boolean isSernoJsonCryptingDisable();

  /**
   * Potlačí zastavení načítání aplikace při těžkých duplicitách na classpath.
   */
  public boolean isSuppressClasspathHeavyDuplicityAbort();

  /**
   * Potlačí podrobné logování při startu aplikací.
   * Potlačení se provede nastavením úrovně WARN ve všech loggerech, kde je úroveň podrobnější.
   * Toto nastavení zrychluje start aplikace při vývoji, přičemž zachovává podrobnost startovacích logů v provozu.
   */
  public boolean isSuppressStartupLogging();


  /**
   * Umožní nastavit jiný (obvykle kratší) takto pro mazání keše. Hodnota je v milisekundách. Keš bude vymazána, když
   * aktuální čas přechází přes hranici taktu. Například pro hodnotu 30000 bude keš vymazána každou celou minutu a každou půlminutu,
   * tedy, když je vteřinovka nahoře nebo dole, tedy 7:15:00, 7:15:30, 7:16:00 atd.
   */
  public Long getCacheClearTakt();


  /**
   * Umožní přes aplikaci CIBIS-ADM provádět automatizované integrační testy (AIT).
   * Je testován na několika místech a týká se několika věcí. Ke dni 2013-09-09
   * je to volání procedurálních testů přes ADM a obnovení přístupu (hodnota kapči a hesla).
   * Lze očekávat, že toto bude narůstat.
   */
  public boolean isAitEnabled();


  /**
   * Umožní dynamickou změnu URL šablon bez ohledu na keše, aniž by bylo třeba restartovat aplikaci.
   */
  public boolean isRefreshJasperReportsPath();

  /**
   * Zapne zaznamenávání místa vytvoření TwTextu.
   */
  public boolean isTwTextSourceDescriptionGenerated();
}
