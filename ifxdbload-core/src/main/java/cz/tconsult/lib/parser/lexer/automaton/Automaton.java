/*
 * dk.brics.automaton
 * Copyright (C) 2001-2003 Anders Mřller.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the  Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
 * USA.
 *
 * Changed: 17.2.2003 Martin Veverka, TurboConsult Brno, Czech Respublic:
 *    - Změna balíku, aby se nepletly balíky, pokud by jiné knihovny využívaly stejný SW
 *    - Změna metody union" přibírá parametr říkající, že nemají být klonovány vkládané automaty
 *    - Změna metotody: detrminize: Vrací mapu mapující množinu původních stavů na nový stav
 *         za účelem kosntrukce lexikálního analyzátoru.
 *
 */

package cz.tconsult.lib.parser.lexer.automaton;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finite-state automaton with regular expression operations.
 * <p>
 * Automata are represented using {@link State} and {@link Transition} objects.
 * Implicitly, all states and transitions of an automaton are reachable from its initial state.
 * <p>
 * As a <i>class <a name="invariant">invariant</a></i>, automata are always reduced (see {@link #reduce()})
 * and have no transitions to dead states (see {@link #removeDeadTransitions()}).
 * Furthermore, if the automaton is nondeterministic, then {@link #isDeterministic()} returns false (but
 * the converse is not required, i.e. <tt>isDeterministic()</tt> may return false eventhough the
 * automaton is deterministic).
 * This invariant is automatically preserved by all supported automata operations -
 * however, if states and transitions are manipulated manually, the {@link #reduce()},
 * {@link #removeDeadTransitions()}, and {@link #setDeterministic(boolean)} methods should be used.
 * <p>
 * This code may be used under the terms of the
 * <a href="http://www.gnu.org/copyleft/gpl.html" target="_top">GNU General Public License</a>.
 * @author Anders Mřller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class Automaton implements Serializable
{
  private static final long serialVersionUID = -8935343985924213439L;

  /** Initial state of this automaton. */
  State initial;

  /** If true, then this automaton is definitely deterministic
        (i.e., there are no choices for any run, but a run may crash). */
  boolean deterministic;

  /** Extra data associated with this automaton. */
  Object info;

  /** Hash code. Recomputed by {@link #minimize()}. */
  int hash_code;

  /**
   * Constructs new automaton that accepts the empty language.
   * Using this constructor, automata can be constructed manually from
   * {@link State} and {@link Transition} objects.
   * Note that the other automata operations assume that the
   * <a href="#invariant">class invariant</a> is satisfied.
   * @see #setInitialState(State)
   * @see State
   * @see Transition
   * @see #reduce()
   * @see #removeDeadTransitions()
   */
  public Automaton()
  {
    final State s = new State();
    initial = s;
    deterministic = true;
  }

  static boolean minimize_always;

  /**
   * Sets or resets minimize always flag.
   * If this flag is set, then {@link #minimize()} will automatically
   * be invoked after all operations that otherwise may produce non-minimal automata.
   * By default, the flag is not set.
   * @param flag if true, the flag is set
   */
  static public void setMinimizeAlways(final boolean flag)
  {
    minimize_always = flag;
  }

  void checkMinimizeAlways()
  {
    if (minimize_always) {
      minimize();
    }
  }

  /**
   * Sets initial state.
   * @param s state
   */
  public void setInitialState(final State s)
  {
    initial = s;
  }

  /**
   * Gets initial state.
   * @return state
   */
  public State getInitialState()
  {
    return initial;
  }

  /**
   * Returns deterministic flag for this automaton.
   * @return true if the automaton is definitely deterministic, false if the automaton
   *         may be nondeterministic
   */
  public boolean isDeterministic()
  {
    return deterministic;
  }

  /**
   * Sets deterministic flag for this automaton.
   * This method should (only) be used if automata are constructed manually.
   * @param deterministic true if the automaton is definitely deterministic, false if the automaton
   *                      may be nondeterministic
   */
  public void setDeterministic(final boolean deterministic)
  {
    this.deterministic = deterministic;
  }

  /**
   * Associates extra information with this automaton.
   * @param info extra information
   */
  public void setInfo(final Object info)
  {
    this.info = info;
  }

  /**
   * Returns extra information associated with this automaton.
   * @return extra information
   * @see #setInfo(Object)
   */
  public Object getInfo()
  {
    return info;
  }

  /**
   * Returns the set states that are reachable from the initial state.
   * @return set of {@link State} objects
   */
  public Set<State> getStates()
  {
    final HashSet<State> visited = new HashSet<State>();
    final LinkedList<State> worklist = new LinkedList<State>();
    worklist.add(initial);
    visited.add(initial);
    while (worklist.size()>0) {
      final State s = worklist.removeFirst();
      final Iterator<Transition> i = s.transitions.iterator();
      while (i.hasNext()) {
        final Transition t = i.next();
        if (!visited.contains(t.to)) {
          visited.add(t.to);
          worklist.add(t.to);
        }
      }
    }
    return visited;
  }

  /**
   * Returns the set of reachable accept states.
   * @return set of {@link State} objects
   */
  public Set<State> getAcceptStates()
  {
    final HashSet<State> accepts = new HashSet<State>();
    final HashSet<State> visited = new HashSet<State>();
    final LinkedList<State> worklist = new LinkedList<State>();
    worklist.add(initial);
    visited.add(initial);
    while (worklist.size()>0) {
      final State s = worklist.removeFirst();
      if (s.accept) {
        accepts.add(s);
      }
      final Iterator<Transition> i = s.transitions.iterator();
      while (i.hasNext()) {
        final Transition t = i.next();
        if (!visited.contains(t.to)) {
          visited.add(t.to);
          worklist.add(t.to);
        }
      }
    }
    return accepts;
  }

  /** Assigns consecutive numbers to the given states. */
  void setStateNumbers(final Set<State> states)
  {
    final Iterator<State> i = states.iterator();
    int number = 0;
    while (i.hasNext()) {
      final State s = i.next();
      s.number = number++;
    }
  }

  /** Checks whether there is a loop containing s. */
  boolean isFinite(final State s, final HashSet<State> path)
  {
    path.add(s);
    final Iterator<Transition> i = s.transitions.iterator();
    while (i.hasNext()) {
      final Transition t = i.next();
      if (path.contains(t.to) || !isFinite(t.to, path)) {
        return false;
      }
    }
    path.remove(s);
    return true;
  }

  /** Returns the strings that can be produced from s, returns false if more than
        <tt>limit</tt> strings are found. <tt>limit</tt>&lt;0 means "infinite". */
  boolean getFiniteStrings(final State s, final HashSet<State> pathstates, final HashSet<String> strings, final StringBuffer path, final int limit)
  {
    pathstates.add(s);
    final Iterator<Transition> i = s.transitions.iterator();
    while (i.hasNext()) {
      final Transition t = i.next();
      if (pathstates.contains(t.to)) {
        return false;
      }
      for (int n = t.min; n<=t.max; n++) {
        path.append((char) n);
        if (t.to.accept) {
          strings.add(path.toString());
          if (limit>=0 && strings.size()>limit) {
            return false;
          }
        }
        if (!getFiniteStrings(t.to, pathstates, strings, path, limit)) {
          return false;
        }
        path.deleteCharAt(path.length()-1);
      }
    }
    pathstates.remove(s);
    return true;
  }

  /** Adds transitions to explicit crash state to ensure that
        transition function is total. */
  void totalize()
  {
    final State s = new State();
    s.transitions.add(new Transition(Character.MIN_VALUE, Character.MAX_VALUE, s));
    final Iterator<State> i = getStates().iterator();
    while (i.hasNext()) {
      final State p = i.next();
      int maxi = Character.MIN_VALUE;
      for (final Transition t : p.getSortedTransitions(false)) {
        if (t.min>maxi) {
          p.transitions.add(new Transition((char) maxi, (char) (t.min-1), s));
        }
        if (t.max+1>maxi) {
          maxi = t.max+1;
        }
      }
      if (maxi<=Character.MAX_VALUE) {
        p.transitions.add(new Transition((char) maxi, Character.MAX_VALUE, s));
      }
    }
  }

  /**
   * Reduces this automaton.
   * An automaton is "reduced" by combining overlapping and adjacent edge intervals with same destination.
   */
  public void reduce()
  {
    final Set<State> states = getStates();
    setStateNumbers(states);
    for (final State s : states) { // i
      final List<Transition> sortedTransitions = s.getSortedTransitions(true);
      s.resetTransitions();
      State p = null;
      int min = -1, max = -1;
      for (final Transition t : sortedTransitions) { // j
        if (p==t.to) {
          if (t.min<=max+1) {
            if (t.max>max) {
              max = t.max;
            }
          } else {
            if (p!=null) {
              s.transitions.add(new Transition((char) min, (char) max, p));
            }
            min = t.min;
            max = t.max;
          }
        } else {
          if (p!=null) {
            s.transitions.add(new Transition((char) min, (char) max, p));
          }
          p = t.to;
          min = t.min;
          max = t.max;
        }
      }
      if (p!=null) {
        s.transitions.add(new Transition((char) min, (char) max, p));
      }
    }
  }

  /** Gets sorted array of all interval start points. */
  char[] getStartPoints()
  {
    final Set<Character> pointset = new HashSet<Character>();
    final Iterator<State> i = getStates().iterator();
    while (i.hasNext()) {
      final State s = i.next();
      pointset.add(new Character(Character.MIN_VALUE));
      final Iterator<Transition> j = s.transitions.iterator();
      while (j.hasNext()) {
        final Transition t = j.next();
        pointset.add(new Character(t.min));
        if (t.max<Character.MAX_VALUE) {
          pointset.add(new Character((char) (t.max+1)));
        }
      }
    }
    final char[] points = new char[pointset.size()];
    final Iterator<Character> k = pointset.iterator();
    int n = 0;
    while (k.hasNext()) {
      final Character m = k.next();
      points[n++] = m.charValue();
    }
    Arrays.sort(points);
    return points;
  }

  /**
   * Returns set of live states. A state is "live" if an accept state is reachable from it.
   * @return set of {@link State} objects
   */
  public Set<State> getLiveStates()
  {
    return getLiveStates(getStates());
  }

  Set<State> getLiveStates(final Set<State> states)
  {
    final HashMap<State, HashSet<State>> map = new HashMap<State, HashSet<State>>();
    Iterator<State> i = states.iterator();
    while (i.hasNext()) {
      final State s = i.next();
      map.put(s, new HashSet<State>());
    }
    i = states.iterator();
    while (i.hasNext()) {
      final State s = i.next();
      final Iterator<Transition> j = s.transitions.iterator();
      while (j.hasNext()) {
        final Transition t = j.next();
        map.get(t.to).add(s);
      }
    }
    final Set<State> live = new HashSet<State>(getAcceptStates());
    final LinkedList<State> worklist = new LinkedList<State>(live);
    while (worklist.size()>0) {
      final State s = worklist.removeFirst();
      for (final State p : map.get(s)) { // j
        if (!live.contains(p)) {
          live.add(p);
          worklist.add(p);
        }
      }
    }
    return live;
  }

  /**
   * Removes transitions to dead states and calls {@link #reduce()}
   * (a state is "dead" if no accept state is reachable from it).
   */
  public void removeDeadTransitions()
  {
    final Set<State> states = getStates();
    final Set<State> live = getLiveStates(states);
    final Iterator<State> i = states.iterator();
    while (i.hasNext()) {
      final State s = i.next();
      final Iterator<Transition> j = s.transitions.iterator();
      s.resetTransitions();
      while (j.hasNext()) {
        final Transition t = j.next();
        if (live.contains(t.to)) {
          s.transitions.add(t);
        }
      }
    }
    reduce();
  }

  /** Returns sorted array of transitions for each state (and sets state numbers). */
  Transition[][] getSortedTransitions(final Set<State> states)
  {
    setStateNumbers(states);
    final Transition[][] transitions = new Transition[states.size()][];
    final Iterator<State> i = states.iterator();
    while (i.hasNext()) {
      final State s = i.next();
      transitions[s.number] = s.getSortedTransitionArray(false);
    }
    return transitions;
  }

  /** Returns new (deterministic) automaton with the empty language. */
  public static Automaton makeEmpty()
  {
    final Automaton a = new Automaton();
    final State s = new State();
    a.initial = s;
    a.deterministic = true;
    return a;
  }

  /** Returns new (deterministic) automaton that accepts only the empty string. */
  public static Automaton makeEmptyString()
  {
    final Automaton a = new Automaton();
    final State s = new State();
    a.initial = s;
    s.accept = true;
    a.deterministic = true;
    return a;
  }

  /** Returns new (deterministic) automaton that accepts all strings. */
  public static Automaton makeAnyString()
  {
    final Automaton a = new Automaton();
    final State s = new State();
    a.initial = s;
    s.accept = true;
    s.transitions.add(new Transition(Character.MIN_VALUE, Character.MAX_VALUE, s));
    a.deterministic = true;
    return a;
  }

  /** Returns new (deterministic) automaton that accepts any single character. */
  public static Automaton makeAnyChar()
  {
    return makeCharRange(Character.MIN_VALUE, Character.MAX_VALUE);
  }

  /** Returns new (deterministic) automaton that accepts a single
        character of the given value. */
  public static Automaton makeChar(final char c)
  {
    return makeCharRange(c, c);
  }

  /** Returns new (deterministic) automaton that accepts a single
        char whose value is in the given interval (including both end points). */
  public static Automaton makeCharRange(final char min, final char max)
  {
    final Automaton a = new Automaton();
    final State s1 = new State();
    final State s2 = new State();
    a.initial = s1;
    s2.accept = true;
    if (min<=max) {
      s1.transitions.add(new Transition(min, max, s2));
    }
    a.deterministic = true;
    return a;
  }

  /** Returns new (deterministic) automaton that accepts
        a single character in the given set. */
  public static Automaton makeCharSet(final String set)
  {
    final Automaton a = new Automaton();
    final State s1 = new State();
    final State s2 = new State();
    a.initial = s1;
    s2.accept = true;
    for (int i = 0; i < set.length(); i++) {
      s1.transitions.add(new Transition(set.charAt(i), s2));
    }
    a.deterministic = true;
    a.reduce();
    return a;
  }

  /** Returns new (deterministic) automaton that accepts
        the single given string.
        <p>
        Complexity: linear in length of the string. */
  public static Automaton makeString(final String s)
  {
    final Automaton a = new Automaton();
    State p = new State();
    a.initial = p;
    for (int i = 0; i < s.length(); i++) {
      final State q = new State();
      p.transitions.add(new Transition(s.charAt(i), q));
      p = q;
    }
    p.accept = true;
    a.deterministic = true;
    return a;
  }

  /** Returns new automaton that accepts the concatenation of the languages
        of this	and the given automaton.
        <p>
        Complexity: linear in number of states. */
  public Automaton concatenate(Automaton a)
  {
    a = (Automaton) a.clone();
    final Automaton b = (Automaton) clone();
    final Iterator<State> i = b.getAcceptStates().iterator();
    while (i.hasNext()) {
      final State s = i.next();
      s.accept = false;
      s.addEpsilon(a.initial);
    }
    b.deterministic = false;
    b.checkMinimizeAlways();
    return b;
  }

  /** Returns new automaton that accepts the concatenation of the languages
        of the given automata.
        <p>
        Complexity: linear in total number of states. */
  static public Automaton concatenate(final List<Automaton> l)
  {
    if (l.isEmpty()) {
      return makeEmptyString();
    }
    final Iterator<Automaton> i = l.iterator();
    final Automaton b = (Automaton) i.next().clone();
    Iterator<State> j = b.getAcceptStates().iterator();
    while (i.hasNext()) {
      final Automaton a = (Automaton) i.next().clone();
      final Iterator<State> k = a.getAcceptStates().iterator();
      while (j.hasNext()) {
        final State s = j.next();
        s.accept = false;
        s.addEpsilon(a.initial);
      }
      j = k;
    }
    b.deterministic = false;
    b.checkMinimizeAlways();
    return b;
  }

  /** Returns new automaton that accepts the union of the empty string and
        the language of this automaton.
        <p>
        Complexity: linear in number of states. */
  public Automaton optional()
  {
    final Automaton a = (Automaton) clone();
    final State s = new State();
    s.addEpsilon(a.initial);
    s.accept = true;
    a.initial = s;
    a.deterministic = false;
    a.checkMinimizeAlways();
    return a;
  }

  /** Returns new automaton that accepts the Kleene star (zero or
        more concatenated repetitions) of the language of this automaton.
        <p>
        Complexity: linear in number of states. */
  public Automaton repeat()
  {
    final Automaton a = (Automaton) clone();
    final State s = new State();
    s.accept = true;
    s.addEpsilon(a.initial);
    final Iterator<State> i = a.getAcceptStates().iterator();
    while (i.hasNext()) {
      final State p = i.next();
      p.accept = false;
      p.addEpsilon(s);
    }
    a.initial = s;
    a.deterministic = false;
    a.checkMinimizeAlways();
    return a;
  }

  /** Returns new automaton that accepts <code>min</code> or more
        concatenated repetitions of the language of this automaton.
        <p>
        Complexity: linear in number of states and in <code>min</code>. */
  public Automaton repeat(int min)
  {
    Automaton a = repeat();
    while (min-->0) {
      a = concatenate(a);
    }
    return a;
  }

  /** Returns new automaton that accepts between <code>min</code> and <code>max</code>
        (including both) concatenated repetitions of the language of this automaton.
        <p>
        Complexity: linear in number of states and in <code>min</code>
        and <code>max</code>.  */
  public Automaton repeat(int min, int max)
  {
    if (min>max) {
      return makeEmpty();
    }
    max -= min;
    Automaton a;
    if (min==0) {
      a = makeEmptyString();
    } else if (min==1) {
      a = (Automaton) clone();
    } else {
      a = this;
      while (--min>0) {
        a = concatenate(a);
      }
    }
    if (max==0) {
      return a;
    }
    Automaton d = (Automaton) clone();
    while (--max>0) {
      final Automaton c = (Automaton) clone();
      final Iterator<State> i = c.getAcceptStates().iterator();
      while (i.hasNext()) {
        final State p = i.next();
        p.addEpsilon(d.initial);
      }
      d = c;
    }
    final Iterator<State> i = a.getAcceptStates().iterator();
    while (i.hasNext()) {
      final State p = i.next();
      p.addEpsilon(d.initial);
    }
    a.deterministic = false;
    a.checkMinimizeAlways();
    return a;
  }

  /** Returns new (deterministic) automaton that accepts the
        complement of the language of this automaton.
        <p>
        Complexity: linear in number of states (if already deterministic). */
  public Automaton complement()
  {
    final Automaton a = (Automaton) clone();
    a.totalize();
    a.determinize();
    final Iterator<State> i = a.getStates().iterator();
    while (i.hasNext()) {
      final State p = i.next();
      p.accept = !p.accept;
    }
    a.removeDeadTransitions();
    return a;
  }

  /** Returns new (deterministic) automaton that
        accepts the intersection of the languages of this and the given automaton.
        As a side-effect, both this and the given automaton are determinized,
        if not already deterministic.
        <p>
        Complexity: quadratic in number of states (if already deterministic). */
  public Automaton intersection(final Automaton a)
  {
    determinize();
    a.determinize();
    final Transition[][] transitions1 = getSortedTransitions(getStates());
    final Transition[][] transitions2 = getSortedTransitions(a.getStates());
    final Automaton c = new Automaton();
    final LinkedList<StatePair> worklist = new LinkedList<StatePair>();
    final HashMap<StatePair, StatePair> newstates = new HashMap<StatePair, StatePair>();
    final State s = new State();
    c.initial = s;
    StatePair p = new StatePair(s, initial, a.initial);
    worklist.add(p);
    newstates.put(p, p);
    while (worklist.size()>0) {
      p = worklist.removeFirst();
      p.s.accept = p.s1.accept && p.s2.accept;
      final Transition[] t1 = transitions1[p.s1.number];
      final Transition[] t2 = transitions2[p.s2.number];
      for (int n1 = 0, n2 = 0; n1<t1.length && n2<t2.length;) {
        if (t1[n1].max<t2[n2].min) {
          n1++;
        } else if (t2[n2].max<t1[n1].min) {
          n2++;
        } else {
          final StatePair q = new StatePair(t1[n1].to, t2[n2].to);
          StatePair r = newstates.get(q);
          if (r==null) {
            q.s = new State();
            worklist.add(q);
            newstates.put(q, q);
            r = q;
          }
          final char min = t1[n1].min>t2[n2].min ? t1[n1].min : t2[n2].min;
          final char max = t1[n1].max<t2[n2].max ? t1[n1].max : t2[n2].max;
          p.s.transitions.add(new Transition(min, max, r.s));
          if (t1[n1].max<t2[n2].max) {
            n1++;
          } else {
            n2++;
          }
        }
      }
    }
    c.deterministic = true;
    c.removeDeadTransitions();
    c.checkMinimizeAlways();
    return c;
  }

  /** Returns new automaton that accepts the union of the languages
        of this and the given automaton.
        <p>
        Complexity: linear in number of states. */
  public Automaton union(Automaton a)
  {
    a = (Automaton) a.clone();
    final Automaton b = (Automaton) clone();
    final State s = new State();
    s.addEpsilon(a.initial);
    s.addEpsilon(b.initial);
    a.initial = s;
    a.deterministic = false;
    a.checkMinimizeAlways();
    return a;
  }

  static public Automaton union(final List<Automaton> l) // metoda implementována provoláním původní metody
  /** Returns new automaton that accepts the union of the languages
        of the given automata.
        <p>
        Complexity: linear in number of states. */
  {
    return union(l, false);
  }

  /** Returns new automaton that accepts the union of the languages
        of the given automata.
        <p>
        @param aInPlace true ... provést na místě, čili neklonovat předané automaty
        Complexity: linear in number of states. */

  static public Automaton union(final List<Automaton> l, final boolean aInPlace) //MV přidán parametr řídící, zda klonovat
  {
    final State s = new State();
    for (final Automaton x : l) {
      final Automaton b =  (Automaton) (aInPlace ? x : x.clone());
      s.addEpsilon(b.initial);
    }
    final Automaton a = new Automaton();
    a.initial = s;
    a.deterministic = false;
    a.checkMinimizeAlways();
    return a;
  }

  /** Determinizes this automaton.
        <p>
        Complexity: exponential in number of states. */
  public Map<Set<State>, State> determinize() //MV přidán návratový parameter
  {
    if (deterministic) {
      return null;
    }
    totalize();
    final char[] points = getStartPoints();
    // subset construction
    final Map<Set<State>, Set<State>> sets = new HashMap<Set<State>, Set<State>>();
    final LinkedList<Set<State>> worklist = new LinkedList<Set<State>>();
    final Map<Set<State>, State> newstate = new HashMap<Set<State>, State>();
    final Set<State> singleton = new HashSet<State>();
    singleton.add(initial);
    sets.put(singleton, singleton);
    worklist.add(singleton);
    initial = new State();
    newstate.put(singleton, initial);
    while (worklist.size()>0) {
      final Set<State> s = worklist.removeFirst();
      final State r = newstate.get(s);
      for (final State q : s) { // i
        if (q.accept) {
          r.accept = true;
          break;
        }
      }
      for (int n = 0; n<points.length; n++) {
        final Set<State> p = new HashSet<State>();
        for (final State q : s) { // j
          for (final Transition t : q.transitions) { // k
            if (t.min<=points[n] && points[n]<=t.max) {
              p.add(t.to);
            }
          }
        }
        if (!sets.containsKey(p)) {
          sets.put(p, p);
          worklist.add(p);
          newstate.put(p, new State());
        }
        final State q = newstate.get(p);
        final char min = points[n];
        char max;
        if (n+1<points.length) {
          max = (char) (points[n+1]-1);
        } else {
          max = Character.MAX_VALUE;
        }
        r.transitions.add(new Transition(min, max, q));
      }
    }
    deterministic = true;
    removeDeadTransitions();
    return newstate; //MV
  }

  private boolean statesAgree(final Transition[][] transitions, final boolean[][] mark, final int n1, final int n2)
  {
    final Transition[] t1 = transitions[n1];
    final Transition[] t2 = transitions[n2];
    for (int k1 = 0, k2 = 0; k1<t1.length && k2<t2.length;) {
      if (t1[k1].max<t2[k2].min) {
        k1++;
      } else if (t2[k2].max<t1[k1].min) {
        k2++;
      } else {
        int m1 = t1[k1].to.number;
        int m2 = t2[k2].to.number;
        if (m1>m2) {
          final int t = m1;
          m1 = m2;
          m2 = t;
        }
        if (mark[m1][m2]) {
          return false;
        }
        if (t1[k1].max<t2[k2].max) {
          k1++;
        } else {
          k2++;
        }
      }
    }
    return true;
  }

  private void addTriggers(final Transition[][] transitions, final boolean[][] mark,
      final HashSet<IntPair>[][] triggers, final int n1, final int n2)
  {
    final Transition[] t1 = transitions[n1];
    final Transition[] t2 = transitions[n2];
    for (int k1 = 0, k2 = 0; k1<t1.length && k2<t2.length;) {
      if (t1[k1].max<t2[k2].min) {
        k1++;
      } else if (t2[k2].max<t1[k1].min) {
        k2++;
      } else {
        if (t1[k1].to!=t2[k2].to) {
          int m1 = t1[k1].to.number;
          int m2 = t2[k2].to.number;
          if (m1>m2) {
            final int t = m1;
            m1 = m2;
            m2 = t;
          }
          if (triggers[m1][m2]==null) {
            triggers[m1][m2] = new HashSet<IntPair>();
          }
          triggers[m1][m2].add(new IntPair(n1, n2));
        }
        if (t1[k1].max<t2[k2].max) {
          k1++;
        } else {
          k2++;
        }
      }
    }
  }

  private void markPair(final boolean[][] mark, final HashSet<IntPair>[][] triggers, final int n1, final int n2)
  {
    mark[n1][n2] = true;
    if (triggers[n1][n2]!=null) {
      for (final IntPair p : triggers[n1][n2]) {
        int m1 = p.n1;
        int m2 = p.n2;
        if (m1>m2) {
          final int t = m1;
          m1 = m2;
          m2 = t;
        }
        if (!mark[m1][m2]) {
          markPair(mark, triggers, m1, m2);
        }
      }
    }
  }

  /** Minimizes (and determinizes if not already deterministic) this automaton.
        <p>
        Complexity: quadratic in number of states (if already deterministic). */
  public void minimize()
  {
    determinize();
    totalize();
    final Set<State> ss = getStates();
    final Transition[][] transitions = new Transition[ss.size()][];
    final State[] states = ss.toArray(new State[0]);
    final boolean[][] mark = new boolean[states.length][states.length];
    @SuppressWarnings("unchecked")
    final
    HashSet<IntPair>[][] triggers = new HashSet[states.length][states.length];
    // initialize marks based on acceptance status and find transition arrays
    for (int n1 = 0; n1<states.length; n1++) {
      states[n1].number = n1;
      transitions[n1] = states[n1].getSortedTransitionArray(false);
      for (int n2 = n1+1; n2<states.length; n2++) {
        if (states[n1].accept!=states[n2].accept) {
          mark[n1][n2] = true;
        }
      }
    }
    // for all pairs, see if states agree
    for (int n1 = 0; n1<states.length; n1++) {
      for (int n2 = n1+1; n2<states.length; n2++) {
        if (!mark[n1][n2]) {
          if (statesAgree(transitions, mark, n1, n2)) {
            addTriggers(transitions, mark, triggers, n1, n2);
          } else {
            markPair(mark, triggers, n1, n2);
          }
        }
      }
    }
    // assign equivalence class numbers to states
    int numclasses = 0;
    for (int n = 0; n<states.length; n++) {
      states[n].number = -1;
    }
    for (int n1 = 0; n1<states.length; n1++) {
      if (states[n1].number==-1) {
        states[n1].number = numclasses;
        for (int n2 = n1+1; n2<states.length; n2++) {
          if (!mark[n1][n2]) {
            states[n2].number = numclasses;
          }
        }
        numclasses++;
      }
    }
    // make a new state for each equivalence class
    final State[] newstates = new State[numclasses];
    for (int n = 0; n<numclasses; n++) {
      newstates[n] = new State();
    }
    // select a class representative for each class and find the new initial state
    for (int n = 0; n<states.length; n++) {
      newstates[states[n].number].number = n;
      if (states[n]==initial) {
        initial = newstates[states[n].number];
      }
    }
    // build transitions and set acceptance
    for (int n = 0; n<numclasses; n++) {
      final State s = newstates[n];
      s.accept = states[s.number].accept;
      final Iterator<Transition> i = states[s.number].transitions.iterator();
      while (i.hasNext()) {
        final Transition t = i.next();
        s.transitions.add(new Transition(t.min, t.max, newstates[t.to.number]));
      }
    }
    removeDeadTransitions();
    // recompute hash code
    hash_code = getNumberOfStates()*3+getNumberOfTransitions()*2;
    if (hash_code==0) {
      hash_code = 1;
    }
  }

  /** Returns new automaton that accepts the single chars that occur
        in strings that are accepted in this automaton. */
  public Automaton singleChars()
  {
    final Automaton a = new Automaton();
    final State s = new State();
    a.initial = s;
    final State q = new State();
    q.accept = true;
    final Iterator<State> i = getStates().iterator();
    while (i.hasNext()) {
      final State p = i.next();
      final Iterator<Transition> j = p.transitions.iterator();
      while (j.hasNext()) {
        final Transition t = j.next();
        s.transitions.add(new Transition(t.min, t.max, q));
      }
    }
    a.deterministic = true;
    removeDeadTransitions();
    return a;
  }

  private void addSetTransitions(final State s, final String set, final State p)
  {
    for (int n = 0; n<set.length(); n++) {
      s.transitions.add(new Transition(set.charAt(n), p));
    }
  }

  /** Returns a new automaton that accepts the trimmed language of this automaton.
        The resulting automaton is constructed as follows:
        1) Whenever a <code>c</code> character is allowed in the original automaton,
        one or more <code>set</code> characters are allowed in the new automaton.
        2) The automaton is prefixed and postfixed with any number of
        <code>set</code> characters.
        @param set set of characters to be trimmed
        @param c canonical trim character (assumed to be in <code>set</code>) */
  public Automaton trim(final String set, final char c)
  {
    final Automaton a = (Automaton) clone();
    final State f = new State();
    addSetTransitions(f, set, f);
    f.accept = true;
    final Iterator<State> i = a.getStates().iterator();
    while (i.hasNext()) {
      final State s = i.next();
      final State r = s.step(c);
      if (r!=null) {
        // add inner
        final State q = new State();
        addSetTransitions(q, set, q);
        addSetTransitions(s, set, q);
        q.addEpsilon(r);
      }
      // add postfix
      if (s.accept) {
        s.addEpsilon(f);
      }
    }
    // add prefix
    final State p = new State();
    addSetTransitions(p, set, p);
    p.addEpsilon(a.initial);
    a.initial = p;
    a.deterministic = false;
    a.removeDeadTransitions();
    a.checkMinimizeAlways();
    return a;
  }

  /** Returns a new automaton that accepts the compressed language of this automaton.
        Whenever a <code>c</code> character is allowed in the original automaton,
        one or more <code>set</code> characters are allowed in the new automaton.
        @param set set of characters to be compressed
        @param c canonical compress character (assumed to be in <code>set</code>) */
  public Automaton compress(final String set, final char c)
  {
    final Automaton a = (Automaton) clone();
    final Iterator<State> i = a.getStates().iterator();
    while (i.hasNext()) {
      final State s = i.next();
      final State r = s.step(c);
      if (r!=null) {
        // add inner
        final State q = new State();
        addSetTransitions(q, set, q);
        addSetTransitions(s, set, q);
        q.addEpsilon(r);
      }
    }
    // add prefix
    a.deterministic = false;
    a.removeDeadTransitions();
    a.checkMinimizeAlways();
    return a;
  }

  /** finds the largest entry whose value is less than or equal to c */
  static int findIndex(final char c, final char[] points)
  {
    int a = 0;
    int b = points.length;
    while (b-a>1) {
      final int d = (a+b)/2;
      if (points[d]>c) {
        b = d;
      } else if (points[d]<c) {
        a = d;
      } else {
        return d;
      }
    }
    return a;
  }

  /** Returns new automaton accepting the homomorphic image of this automaton
        using the given function.
        <p>
        This method maps each transition label to a new value.
        <code>source</code> and <code>dest</code> are assumed to be
        arrays of same length, and <code>source</code> must be sorted in
        increasing order and contain no duplicates.
        <code>source</code> defines the starting points of char intervals,
        and the corresponding entries in <code>dest</code> define the
        starting points of corresponding new intervals.  */
  public Automaton homomorph(final char[] source, final char[] dest)
  {
    final Automaton a = (Automaton) clone();
    final Iterator<State> i = a.getStates().iterator();
    while (i.hasNext()) {
      final State s = i.next();
      final Iterator<Transition> j = s.transitions.iterator();
      s.resetTransitions();
      while (j.hasNext()) {
        final Transition t = j.next();
        int min = t.min;
        while (min<=t.max) {
          final int n = findIndex((char) min, source);
          final char nmin = (char) (dest[n]+min-source[n]);
          final int end = n+1==source.length ? Character.MAX_VALUE : source[n+1]-1;
          int length;
          if (end<t.max) {
            length = end+1 - min;
          } else {
            length = t.max+1 - min;
          }
          s.transitions.add(new Transition(nmin, (char) (nmin+length-1), t.to));
          min += length;
        }
      }
    }
    a.deterministic = false;
    a.removeDeadTransitions();
    a.checkMinimizeAlways();
    return a;
  }

  /** Returns new automaton with projected alphabet.
        The new automaton accepts all strings that are projections of
        strings accepted by this automaton onto the given characters
        (represented by <code>Character</code>). If <code>null</code> is
        in the set, it abbreviates the intervals u0000-uDFFF
        and uF900-uFFFF (i.e., the non-private code points). It is assumed that
        all other characters from <code>chars</code> are in the interval
        uE000-uF8FF. */
  public Automaton projectChars(final Set<Character> chars)
  {
    final Character[] c = chars.toArray(new Character[0]);
    final char[] cc = new char[c.length];
    boolean normalchars = false;
    for (int i = 0; i<c.length; i++) {
      if (c[i]==null) {
        normalchars = true;
      } else {
        cc[i] = c[i].charValue();
      }
    }
    Arrays.sort(cc);
    final HashSet<StatePair> epsilons = new HashSet<StatePair>();
    final Automaton a = (Automaton) clone();
    final Iterator<State> i = a.getStates().iterator();
    while (i.hasNext()) {
      final State s = i.next();
      final HashSet<Transition> new_transitions = new HashSet<Transition>();
      final Iterator<Transition> j = s.transitions.iterator();
      while (j.hasNext()) {
        final Transition t = j.next();
        boolean addepsilon = false;
        if (t.min<'\uf900' && t.max>'\udfff') {
          int w1 = Arrays.binarySearch(cc, t.min>'\ue000' ? t.min : '\ue000');
          if (w1<0) {
            w1 = -w1-1;
            addepsilon = true;
          }
          int w2 = Arrays.binarySearch(cc, t.max<'\uf8ff' ? t.max : '\uf8ff');
          if (w2<0) {
            w2 = -w2-2;
            addepsilon = true;
          }
          for (int w = w1; w<=w2; w++) {
            new_transitions.add(new Transition(cc[w], t.to));
            if (w>w1 && cc[w-1]+1!=cc[w]) {
              addepsilon = true;
            }
          }
        }
        if (normalchars) {
          if (t.min<='\udfff') {
            new_transitions.add(new Transition(t.min,
                t.max<'\udfff' ? t.max : '\udfff',
                    t.to));
          }
          if (t.max>='\uf900') {
            new_transitions.add(new Transition(t.min>'\uf900' ? t.min : '\uf900',
                t.max,
                t.to));
          }
        } else if (t.min<='\udfff' || t.max>='\uf900') {
          addepsilon = true;
        }
        if (addepsilon) {
          epsilons.add(new StatePair(s, t.to));
        }
      }
      s.transitions = new_transitions;
    }
    a.reduce();
    a.addEpsilons(epsilons);
    a.removeDeadTransitions();
    a.checkMinimizeAlways();
    return a;
  }

  /**
   * Adds epsilon transitions to this automaton.
   * This method adds extra character interval transitions that are equivalent to the given
   * set of epsilon transitions.
   * @param pairs collection of {@link StatePair} objects representing pairs of source/destination states
   *        where epsilon transitions should be added
   */
  public void addEpsilons(final Collection<StatePair> pairs)
  {
    final HashMap<State, HashSet<State>> forward = new HashMap<State, HashSet<State>>();
    final HashMap<State, HashSet<State>> back = new HashMap<State, HashSet<State>>();
    Iterator<StatePair> i = pairs.iterator();
    while (i.hasNext()) {
      final StatePair p = i.next();
      HashSet<State> to = forward.get(p.s1);
      if (to==null) {
        to = new HashSet<State>();
        forward.put(p.s1, to);
      }
      to.add(p.s2);
      HashSet<State> from = back.get(p.s2);
      if (from==null) {
        from = new HashSet<State>();
        back.put(p.s2, from);
      }
      from.add(p.s1);
    }
    // calculate epsilon closure
    final LinkedList<StatePair> worklist = new LinkedList<StatePair>(pairs);
    final HashSet<StatePair> workset = new HashSet<StatePair>(pairs);
    while (!worklist.isEmpty()) {
      final StatePair p = worklist.removeFirst();
      workset.remove(p);
      final HashSet<State> to = forward.get(p.s2);
      final HashSet<State> from = back.get(p.s1);
      if (to!=null) {
        for (final State s : to) { // j
          final StatePair pp = new StatePair(p.s1, s);
          if (!pairs.contains(pp)) {
            pairs.add(pp);
            forward.get(p.s1).add(s);
            back.get(s).add(p.s1);
            worklist.add(pp);
            workset.add(pp);
            if (from!=null) {
              for (final State q : from) { // k
                final StatePair qq = new StatePair(q, p.s1);
                if (!workset.contains(qq)) {
                  worklist.add(qq);
                  workset.add(qq);
                }
              }
            }
          }
        }
      }
    }
    // add transitions
    i = pairs.iterator();
    while (i.hasNext()) {
      final StatePair p = i.next();
      p.s1.addEpsilon(p.s2);
    }
    deterministic = false;
    checkMinimizeAlways();
  }

  /** Returns true if the given string is accepted by this automaton.
        As a side-effect, this automaton is determinized
        if not already deterministic.
        <p>
        Complexity: linear in length of string (if automaton is already deterministic)
        and in number of transitions. */
  public boolean run(final String s)
  {
    determinize();
    State p = initial;
    for (int i = 0; i < s.length(); i++) {
      final State q = p.step(s.charAt(i));
      if (q==null) {
        return false;
      }
      p = q;
    }
    return p.accept;
  }

  /** Returns number of states in this automaton. */
  public int getNumberOfStates()
  {
    return getStates().size();
  }

  /** Returns number of transitions in this automaton.
        This number is counted as the total number of edges, where one edge
        may be a character interval. */
  public int getNumberOfTransitions()
  {
    int c = 0;
    final Iterator<State> i = getStates().iterator();
    while (i.hasNext()) {
      final State s = i.next();
      c += s.transitions.size();
    }
    return c;
  }

  /** Returns true if this automaton accepts no strings. */
  public boolean isEmpty()
  {
    return initial.accept==false && initial.transitions.size()==0;
  }

  /** Returns true if this automaton accepts all strings. */
  public boolean isTotal()
  {
    if (initial.accept==true && initial.transitions.size()==1) {
      final Transition t = initial.transitions.iterator().next();
      return t.to==initial && t.min==Character.MIN_VALUE && t.max==Character.MAX_VALUE;
    }
    return false;
  }

  /** Returns true if the language of this automaton is finite. */
  public boolean isFinite()
  {
    return isFinite(initial, new HashSet<State>());
  }

  /** Returns set of accepted strings, assuming this automaton has a finite language.
        If the language is not finite, null is returned. */
  public Set<String> getFiniteStrings()
  {
    final HashSet<String> strings = new HashSet<String>();
    if (!getFiniteStrings(initial, new HashSet<State>(), strings, new StringBuffer(), -1)) {
      return null;
    }
    return strings;
  }

  /** Returns set of accepted strings, assuming that at most <tt>limit</tt> strings are accepted.
        If more than <tt>limit</tt> strings are accepted, null is returned.
        If <tt>limit</tt>&lt;0, then this methods works like {@link #getFiniteStrings()}.
   */
  public Set<String> getFiniteStrings(final int limit)
  {
    final HashSet<String> strings = new HashSet<String>();
    if (!getFiniteStrings(initial, new HashSet<State>(), strings, new StringBuffer(), limit)) {
      return null;
    }
    return strings;
  }

  /**
   * Returns a shortest accepted/rejected string.
   * If more than one string is found, the lexicographically first is returned.
   * @param accepted if true, look for accepted strings; otherwise, look for rejected strings
   * @return the string, null if none found
   */
  public String getShortestExample(final boolean accepted)
  {
    return getShortestExample(initial, accepted, new HashMap<State, String>());
  }

  String getShortestExample(final State s, final boolean accepted, final Map<State, String> map)
  {
    if (s.accept==accepted) {
      return "";
    }
    if (map.containsKey(s)) {
      return map.get(s);
    }
    map.put(s, null);
    String best = null;
    final Iterator<Transition> i = s.transitions.iterator();
    while (i.hasNext()) {
      final Transition t = i.next();
      String b = getShortestExample(t.to, accepted, map);
      if (b!=null) {
        b = t.min+b;
        if (best==null ||
            b.length()<best.length() ||
            b.length()==best.length() && b.compareTo(best)<0) {
          best = b;
        }
      }
    }
    map.put(s, best);
    return best;
  }

  /**
   * Returns the longest string that is a prefix of all accepted strings and
   * visits each state at most once.
   * @return common prefix
   */
  public String getCommonPrefix()
  {
    final StringBuffer b = new StringBuffer();
    final HashSet<State> visited = new HashSet<State>();
    State s = initial;
    boolean done;
    do {
      done = true;
      visited.add(s);
      if (!s.accept && s.transitions.size()==1) {
        final Transition t = s.transitions.iterator().next();
        if (t.min==t.max && !visited.contains(t.to)) {
          b.append(t.min);
          s = t.to;
          done = false;
        }
      }
    } while (!done);
    return b.toString();
  }

  /** Returns true if the language of this automaton is a subset of the
        language of the given automaton.
        Equivalent to <code>this.intersection(a.complement()).isEmpty()</code>. */
  public boolean subsetOf(final Automaton a)
  {
    return intersection(a.complement()).isEmpty();
  }

  /** Returns true if the language of this automaton is equal to the
        language of the given automaton.
        Equivalent to <code>this.hashCode()==a.hashCode() and this.subsetOf(a) and a.subsetOf(this)</code>. */
  @Override
  public boolean equals(final Object obj)
  {
    if (!(obj instanceof Automaton)) {
      return false;
    }
    final Automaton a = (Automaton) obj;
    return hashCode()==a.hashCode() && subsetOf(a) && a.subsetOf(this);
  }

  /** Returns hash code for this automaton.
        The hash code is based on the number of states and transitions in the minimized automaton. */
  @Override
  public int hashCode()
  {
    if (hash_code==0) {
      minimize();
    }
    return hash_code;
  }

  /** Returns a string representation of this automaton. */
  @Override
  public String toString()
  {
    final StringBuffer b = new StringBuffer();
    final Set<State> states = getStates();
    setStateNumbers(states);
    b.append("initial state: ").append(initial.number).append("\n");
    final Iterator<State> i = states.iterator();
    while (i.hasNext()) {
      final State s = i.next();
      b.append(s.toString());
    }
    return b.toString();
  }

  /** Returns <a href="http://www.research.att.com/sw/tools/graphviz/"
        target="_top">Graphviz Dot</a> representation of this automaton. */
  public String toDot()
  {
    final StringBuffer b = new StringBuffer("digraph Automaton {\n");
    b.append("  rankdir = LR;\n");
    final Set<State> states = getStates();
    setStateNumbers(states);
    final Iterator<State> i = states.iterator();
    while (i.hasNext()) {
      final State s = i.next();
      b.append("  ").append(s.number);
      if (s.accept) {
        b.append(" [shape=doublecircle,label=\"\"];\n");
      } else {
        b.append(" [shape=circle,label=\"\"];\n");
      }
      if (s==initial) {
        b.append("  initial [shape=plaintext,label=\"\"];\n");
        b.append("  initial -> ").append(s.number).append("\n");
      }
      final Iterator<Transition> j = s.transitions.iterator();
      while (j.hasNext()) {
        final Transition t = j.next();
        b.append("  ").append(s.number);
        t.appendDot(b);
      }
    }
    return b.append("}\n").toString();
  }

  /** Returns a clone of this automaton. */
  @Override
  public Object clone()
  {
    final Automaton a = new Automaton();
    final HashMap<State, State> m = new HashMap<State, State>();
    final Set<State> states = getStates();
    Iterator<State> i = states.iterator();
    while (i.hasNext()) {
      m.put(i.next(), new State());
    }
    i = states.iterator();
    while (i.hasNext()) {
      final State s = i.next();
      final State p = m.get(s);
      p.accept = s.accept;
      if (s==initial) {
        a.initial = p;
      }
      p.transitions = new HashSet<Transition>();
      final Iterator<Transition> j = s.transitions.iterator();
      while (j.hasNext()) {
        final Transition t = j.next();
        p.transitions.add(new Transition(t.min, t.max, m.get(t.to)));
      }
    }
    a.deterministic = deterministic;
    return a;
  }

  /**
   * Retrieves a serialized <code>Automaton</code> located by a URL.
   * @param url URL of serialized automaton
   * @exception IOException if input/output related exception occurs
   * @exception OptionalDataException if the data is not a serialized object
   * @exception InvalidClassException if the class serial number does not match
   * @exception ClassCastException if the data is not a serialized <code>Automaton</code>
   * @exception ClassNotFoundException if the class of the serialized object cannot be found
   */
  public static Automaton load(final URL url)
      throws IOException, OptionalDataException, ClassCastException,
      ClassNotFoundException, InvalidClassException
  {
    return load(url.openStream());
  }

  /**
   * Retrieves a serialized <code>Automaton</code> from a stream.
   * @param stream input stream with serialized automaton
   * @exception IOException if input/output related exception occurs
   * @exception OptionalDataException if the data is not a serialized object
   * @exception InvalidClassException if the class serial number does not match
   * @exception ClassCastException if the data is not a serialized <code>Automaton</code>
   * @exception ClassNotFoundException if the class of the serialized object cannot be found
   */
  public static Automaton load(final InputStream stream)
      throws IOException, OptionalDataException, ClassCastException,
      ClassNotFoundException, InvalidClassException
  {
    final ObjectInputStream s = new ObjectInputStream(stream);
    return (Automaton) s.readObject();
  }

  /**
   * Writes this <code>Automaton</code> to the given stream.
   * @param stream output stream for serialized automaton
   * @exception IOException if input/output related exception occurs
   */
  public void store(final OutputStream stream)
      throws IOException
  {
    final ObjectOutputStream s = new ObjectOutputStream(stream);
    s.writeObject(this);
    s.flush();
  }
}

class IntPair
{
  int n1;
  int n2;

  IntPair(final int n1, final int n2)
  {
    this.n1 = n1;
    this.n2 = n2;
  }
}
