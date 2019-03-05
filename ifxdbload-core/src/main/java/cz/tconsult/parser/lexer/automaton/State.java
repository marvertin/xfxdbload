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
 * Changed: 17.2.2003 Martin Veverka, TurboConsult Brno, Czech Respublic:
 *    - Přidána metoda vracející číslo stavu, abych se dal implementovat lexikální analyzátor.
 *
 */

package cz.tconsult.parser.lexer.automaton;


import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <tt>Automaton</tt> state.
 * @author Anders Möller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class State implements Serializable, Comparable<State>
{

  private static final long serialVersionUID = -5430829556004927214L;
  
    boolean accept;
    HashSet<Transition> transitions;

    int number;

    int id;
    static int next_id;

    /** Constructs new state. Initially, the new state is a reject state. */
    public State()
    {
        resetTransitions();
        id = next_id++;
    }

    /** Resets transition set. */
    void resetTransitions()
    {
        transitions = new HashSet<Transition>();
    }

    /**
     * Returns set of outgoing transitions.
     * Subsequent changes are reflected in the automaton.
     * @return transition set
     */
    public Set<Transition> getTransitions()
    {
        return transitions;
    }

    /**
     * Adds outgoing transition.
     * @param t transition
     */
    public void addTransition(Transition t)
    {
        transitions.add(t);
    }

    /**
     * Sets acceptance for this state.
     * @param accept if true, this state is an accept state
     */
    public void setAccept(boolean accept)
    {
        this.accept = accept;
    }

    /**
     * Returns acceptance status.
     * @return true is this is an accept state
     */
    public boolean isAccept()
    {
        return accept;
    }

    /**
     * Performs lookup in transitions.
     * @param c character to look up
     * @return destination state, null if no matching outgoing transition
     */
    public State step(char c)
    {
        Iterator<Transition> i = transitions.iterator();
        while (i.hasNext()) {
            Transition t = i.next();
            if (t.min<=c && c<=t.max)
                return t.to;
        }
        return null;
    }

    void addEpsilon(State to)
    {
        if (to.accept)
            accept = true;
        Iterator<Transition> i = to.transitions.iterator();
        while (i.hasNext()) {
            Transition t = i.next();
            transitions.add((Transition)t.clone());
        }
    }

    /** Returns transitions sorted by (min, reverse max, to) or (to, min, reverse max) */
    Transition[] getSortedTransitionArray(boolean to_first)
    {
        Transition[] e = transitions.toArray(new Transition[0]);
        TransitionComparator c = new TransitionComparator();
        c.to_first = to_first;
        Arrays.sort(e, c);
        return e;
    }

    List<Transition> getSortedTransitions(boolean to_first)
    {
        return Arrays.asList(getSortedTransitionArray(to_first));
    }

    /**
     * Returns string describing this state. Normally invoked via
     * {@link Automaton#toString()}.
     */
    public String toString()
    {
        StringBuffer b = new StringBuffer();
        b.append("state ").append(number);
                      b.append( "[" + id + "]");  //MV
        if (accept)
            b.append(" [accept]");
        else
            b.append(" [reject]");
        b.append(":\n");
        Iterator<Transition> i = transitions.iterator();
        while (i.hasNext()) {
            Transition t = i.next();
            b.append("  ").append(t.toString()).append("\n");
        }
        return b.toString();
    }

    /**
     * Returns string describing this state. Normally invoked via
     * {@link Automaton#toString()}.
    public String toString()
    {
        StringBuffer b = new StringBuffer();
        b.append("state ").append(number);
                      b.append( "[" + id + "]");  //MV
        if (accept)
            b.append(" [accept]");
        else
            b.append(" [reject]");
        return b.toString();
    }
     */

    /**
     * Compares this object with the specified object for order.
     * States are ordered by the time of construction.
     */
    public int compareTo(State o)
    {
        return o.id-id;
    }

    /**
     * Vrací číslo stavu. Pozor na volání této metody, aby stavy
     * byly vraceno, až jsou číslovány.
     * @return Číslo stavu nebo také nesmysl.
     */
    public int getNumber() { //MV
      return number;
    }

}
