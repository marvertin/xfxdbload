package cz.tconsult.lib.ifxdbload.core.splparser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import cz.tconsult.lib.lexer.LexerToken;
import cz.tconsult.lib.spllexer.ESeplTokenForIgnoring;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class TokenChecker {


  private final TokenIterator<LexerToken> it;

  /**
   * Očekává, jeden ze zadaných token typů.
   * @param types
   * @throws YCannotParse
   */
  LexerToken expect(final Object ... types) throws YCannotParse {
    if (! isOneOf(types)) {
      throw new YCannotParse(get());
    }
    return shift();
  }

  /**
   * Očekává, jeden ze zadaných token typů.
   * @param types
   * @throws YCannotParse
   */
  LexerToken notExpect(final Object ... types) throws YCannotParse {
    if (isOneOf(types)) {
      throw new YCannotParse(get());
    }
    return shift();
  }

  /**
   * Pokud čeká token jednoho z typů, vrátí ho a posune se na další, jinak nic..
   * @param types
   * @throws YCannotParse
   */
  Optional<LexerToken> optional(final Object ... types) {
    if (isOneOf(types)) {
      return Optional.of(shift());
    } else {
      return Optional.empty();
    }
  }


  /**
   * Počká, až přijde jeden z uvedených tokenů a pak ho vrátí.
   * Pokud je na vstupu, tak ho vrátí a ásku neposune.
   * @param types
   * @return
   */
  LexerToken shiftUntil(final Object ... types) {
    while (! isOneOf(types)) {
      shift();
    }
    return get();
  }

  private LexerToken get() {
    shiftAllWitespacesAndComentaries();
    return it.get();
  }

  /**
   * Vrátí token, který je k dispozici, bez ohledu na to, zda je to bílý znak, či nikoli
   * @return
   */
  protected LexerToken rawGet() {
    return it.get();
  }

  private void shiftAllWitespacesAndComentaries() {
    while (it.get().getType() instanceof ESeplTokenForIgnoring) {
      onShift(it.get());
      it.shift();
    }
  }

  /**
   * Počká, až přijde jiný než jeden z uvedených tokenů a pak vrátí celý seznam toho, co se sbíralo.
   * Pokud je na vstupu, tak ho vrátí a pásku neposune.
   * @param types
   * @return
   */
  List<LexerToken> shiftWhile(final Object ... types) {
    final List<LexerToken> list = new LinkedList<LexerToken>();
    while (isOneOf(types)) {
      list.add(shift());
    }
    return list;
  }

  public boolean isOneOf(final Object... types) {
    return Arrays.asList(types).contains(get().getType());
  }


  protected Object extractType(final LexerToken token) {
    return token.getType();
  }

  private LexerToken shift() {
    onShift(get());
    return it.shift();
  }

  protected abstract void onShift(LexerToken token);



}
