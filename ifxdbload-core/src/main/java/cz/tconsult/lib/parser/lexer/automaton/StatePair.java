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
 */

package cz.tconsult.lib.parser.lexer.automaton;


/**
 * Pair of states.
 * @author Anders Mřller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class StatePair
{
    State s;
    State s1;
    State s2;

    StatePair(State s, State s1, State s2)
    {
	this.s = s;
	this.s1 = s1;
	this.s2 = s2;
    }

    /**
     * Constructs new state pair.
     * @param s1 first state
     * @param s2 second state
     */
    public StatePair(State s1, State s2)
    {
	this.s1 = s1;
	this.s2 = s2;
    }

    /**
     * Returns first component of this pair.
     * @return first state
     */
    public State getFirstState()
    {
	return s1;
    }

    /**
     * Returns second component of this pair.
     * @return second state
     */
    public State getSecondState()
    {
	return s2;
    }

    /**
     * Checks for equality.
     * @param obj object to compare with
     * @return true if <tt>obj</tt> represents the same pair of states as this pair
     */
    public boolean equals(Object obj)
    {
	if (obj instanceof StatePair) {
	    StatePair p = (StatePair) obj;
	    return p.s1==s1 && p.s2==s2;
	}
	else
	    return false;
    }

    /**
     * Returns hash code.
     * @return hash code
     */
    public int hashCode()
    {
	return s1.hashCode()+s2.hashCode();
    }
}
