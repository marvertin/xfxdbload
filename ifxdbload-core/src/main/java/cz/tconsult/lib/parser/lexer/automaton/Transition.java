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


import java.io.Serializable;
import java.util.Comparator;

/**
 * <tt>Automaton</tt> transition.
 * <p>
 * A transition, which belongs to a source state, consists of a Unicode character interval
 * and a destination state.
 * @author Anders Mřller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class Transition implements Serializable
{
  private static final long serialVersionUID = 8859587236061384499L;
  
    /*
     * CLASS INVARIANT: min<=max
     */

    char min;
    char max;

    State to;

    /**
     * Constructs new singleton interval transition.
     * @param c transition character
     * @param to destination state
     */
    public Transition(char c, State to)
    {
	min = max = c;
	this.to = to;
    }

    /**
     * Constructs new transition.
     * Both end points are included in the interval.
     * @param min transition interval minimum
     * @param max transition interval maximum
     * @param to destination state
     */
    public Transition(char min, char max, State to)
    {
	if (max<min) {
	    char t = max;
	    max = min;
	    min = t;
	}
	this.min = min;
	this.max = max;
	this.to = to;
    }

    /** Returns minimum of this transition interval. */
    public char getMin()
    {
	return min;
    }

    /** Returns maximum of this transition interval. */
    public char getMax()
    {
	return max;
    }

    /** Returns destination of this transition. */
    public State getDest()
    {
	return to;
    }

    /**
     * Checks for equality.
     * @param obj object to compare with
     * @return true if <tt>obj</tt> is a transition with same
     *         character interval and destination state as this transition.
     */
    public boolean equals(Object obj)
    {
	if (obj instanceof Transition) {
	    Transition t = (Transition) obj;
	    return t.min==min && t.max==max && t.to==to;
	} else
	    return false;
    }

    /**
     * Returns hash code.
     * The hash code is based on the character interval (not the destination state).
     * @return hash code
     */
    public int hashCode()
    {
	return min*2+max*3;
    }

    /**
     * Clones this transition.
     * @return clone with same character interval and destination state
     */
    public Object clone()
    {
	return new Transition(min, max, to);
    }

    static void appendCharString(char c, StringBuffer b)
    {
	if (c>=0x21 && c<=0x7e && c!='\\')
	    b.append(c);
	else {
	    b.append("\\u");
	    String s = Integer.toHexString(c);
	    if (c<0x10)
		b.append("000").append(s);
	    else if (c<0x100)
		b.append("00").append(s);
	    else if (c<0x1000)
		b.append("0").append(s);
	    else
		b.append(s);
	}
    }

    /**
     * Returns string describing this state. Normally invoked via
     * {@link Automaton#toString()}.
     */
    public String toString()
    {
	StringBuffer b = new StringBuffer();
	appendCharString(min, b);
	if (min!=max){
	    b.append("-");
	    appendCharString(max, b);
	}
	b.append(" -> ").append(to.number);
	return b.toString();
    }

    void appendDot(StringBuffer b)
    {
	b.append(" -> ").append(to.number).append(" [label=\"");
	appendCharString(min, b);
	if (min!=max){
	    b.append("-");
	    appendCharString(max, b);
	}
	b.append("\"]\n");
    }
}

class TransitionComparator implements Comparator<Transition>
{
    boolean to_first;

    /** compare by (min, reverse max, to) or (to, min, reverse max) */
    public int compare(Transition t1, Transition t2)
    {
    	    if (to_first) {
    		if (t1.to.number<t2.to.number)
    		    return -1;
    		else if (t1.to.number>t2.to.number)
    		    return 1;
    	    }
    	    if (t1.min<t2.min)
    		return -1;
    	    if (t1.min>t2.min)
    		return 1;
    	    if (t1.max>t2.max)
    		return -1;
    	    if (t1.max<t2.max)
    		return 1;
    	    if (!to_first) {
    		if (t1.to.number<t2.to.number)
    		    return -1;
    		else if (t1.to.number>t2.to.number)
    		    return 1;
    	    }
    	    return 0;
    }
}
