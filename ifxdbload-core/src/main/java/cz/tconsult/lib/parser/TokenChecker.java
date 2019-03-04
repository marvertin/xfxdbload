package cz.tconsult.lib.parser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class TokenChecker<T> {


  private final TokenIterator<T> it;

  /**
   * Očekává, jeden ze zadaných token typů.
   * @param types
   * @throws YCannotParse
   */
  T expect(final Object ... types) throws YCannotParse {
    if (! isOneOf(types)) {
      throw new YCannotParse(it.get());
    }
    return shift();
  }

  /**
   * Očekává, jeden ze zadaných token typů.
   * @param types
   * @throws YCannotParse
   */
  T notExpect(final Object ... types) throws YCannotParse {
    if (isOneOf(types)) {
      throw new YCannotParse(it.get());
    }
    return shift();
  }

  /**
   * Pokud čeká token jednoho z typů, vrátí ho a posune se na další, jinak nic..
   * @param types
   * @throws YCannotParse
   */
  Optional<T> optional(final Object ... types) {
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
  T shiftUntil(final Object ... types) {
    while (! isOneOf(types)) {
      shift();
    }
    return it.get();
  }

  /**
   * Počká, až přijde jiný než jeden z uvedených tokenů a pak vrátí celý seznam toho, co se sbíralo.
   * Pokud je na vstupu, tak ho vrátí a pásku neposune.
   * @param types
   * @return
   */
  List<T> shiftWhile(final Object ... types) {
    final List<T> list = new LinkedList<T>();
    while (isOneOf(types)) {
      list.add(shift());
    }
    return list;
  }

  public boolean isOneOf(final Object... types) {
    return Arrays.asList(types).contains(extractType(it.get()));
  }


  protected abstract Object extractType(T token);

  private T shift() {
    onShift(it.get());
    return it.shift();
  }

  protected abstract void onShift(T token);



}
