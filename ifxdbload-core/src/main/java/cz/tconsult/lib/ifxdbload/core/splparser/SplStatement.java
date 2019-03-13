package cz.tconsult.lib.ifxdbload.core.splparser;

import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.primitives.Longs;

import cz.tconsult.lib.ifxdbload.core.tw.CachedValue;
import cz.tconsult.lib.lexer.LexerTokenLocator;
import cz.tconsult.lib.spllexer.SplDirective;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * Rozpársrovaný SPL příkaz.
 * @author veverka
 *
 */
@Data
public class SplStatement {
  /** seznam direktiv u tohoto příkazu */
  private final Set<SplDirective> directives;

  /** typ objektu */
  private final EStmType stmType;

  /** jméno objektu, tedy procedury, triggeru atd, pokud jméno existuje */
  private final String name;

  /** Text SQL příkazu */
  private final String text;

  /** Text SQL příkazu kanonizovaný pro výpočet hešíka nejspíš onceskriptu */
  @Getter(AccessLevel.NONE)
  private final String textForHash;

  /** Lokátor prvního tokenu příkazu, je zajímavý pro určení pozice */
  private final LexerTokenLocator firstTokenLocator;

  /** Kešovaný hash pro zrychlení výpočtu */
  @Getter(AccessLevel.NONE)
  private final CachedValue<String> sha1Hash = new CachedValue<>( () -> DigestUtils.sha1Hex(checkedTextForHash()));

  @Getter(AccessLevel.NONE)
  private final CachedValue<Long> longHash = new CachedValue<>( () -> Longs.fromByteArray(DigestUtils.sha1(checkedTextForHash())));

  public String getNameLower() {
    if (name == null) {
      throw new NullPointerException("Chtěno jména po bezemenném objektu: " + this);
    }
    return name.toLowerCase();
  }


  /**
   * Hash v podobě longu
   * @return
   */
  //  public long getLongHash() {
  //    checkedTextForHash();
  //    if (cachedHashSha1 == null) {
  //      cachedHashSha1 = DigestUtils.sha1Hex(textForHash);
  //    }
  //    return cachedHashLong;
  //  }

  /**
   * Sha1 hash.
   * @return
   */
  public String getSha1Hash() {
    return sha1Hash.get();
  }

  public long getLongHash() {
    return longHash.get();
  }


  private String checkedTextForHash() {
    if (textForHash == null) {
      throw new RuntimeException("Objekt nebyl svtořen s textem pro výpočet hešů.");
    }
    return textForHash;
  }



}
