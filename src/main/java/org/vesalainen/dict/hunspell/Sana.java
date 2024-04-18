/*
 * Copyright (C) 2024 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.dict.hunspell;

import static java.lang.Integer.min;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vesalainen.util.fi.Hyphenator;
import static org.vesalainen.util.fi.Hyphenator.HYPHEN;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Sana implements Comparable<Sana>
{
    private static final Locale FI = Locale.forLanguageTag("fi");
    private final String[] syllables;
    private final String toString;
    private final String hyphenated;

    public Sana(String sana)
    {
        this.toString = sana;
        hyphenated = Hyphenator.hyphenate(sana.replace("-", ""), FI);
        this.syllables = hyphenated.split(""+HYPHEN);
    }

    public Sana(String... syllables)
    {
        this.syllables = syllables;
        this.toString = Stream.of(syllables).collect(Collectors.joining());
        hyphenated = Hyphenator.hyphenate(toString, FI);
    }

    public Sana concat(Sana oth)
    {
        return new Sana(toString+oth.toString);
    }
    public String[] getSyllables()
    {
        return syllables;
    }

    public String getHyphenated()
    {
        return hyphenated;
    }

    public boolean startsWith(Sana oth)
    {
        if (syllables.length < oth.syllables.length)
        {
            return false;
        }
        int len = oth.syllables.length;
        for (int ii=0;ii<len;ii++)
        {
            if (!syllables[ii].equals(oth.syllables[ii]))
            {
                return false;
            }
        }
        return true;
    }
    public boolean endsWith(Sana oth)
    {
        if (syllables.length < oth.syllables.length)
        {
            return false;
        }
        int len1 = oth.syllables.length;
        int jj = syllables.length-1;
        for (int ii=len1-1;ii>=0;ii--)
        {
            if (!syllables[jj--].equals(oth.syllables[ii]))
            {
                return false;
            }
        }
        return true;
    }

    public int length()
    {
        return toString.length();
    }

    public int indexOf(int ch)
    {
        return toString.indexOf(ch);
    }

    public int lastIndexOf(int ch)
    {
        return toString.lastIndexOf(ch);
    }

    public int lastIndexOf(int ch, int fromIndex)
    {
        return toString.lastIndexOf(ch, fromIndex);
    }

    public int indexOf(String str)
    {
        return toString.indexOf(str);
    }

    public int indexOf(String str, int fromIndex)
    {
        return toString.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str)
    {
        return toString.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex)
    {
        return toString.lastIndexOf(str, fromIndex);
    }

    public boolean startsWith(String prefix)
    {
        return toString.startsWith(prefix);
    }

    public boolean endsWith(String suffix)
    {
        return toString.endsWith(suffix);
    }

    public Sana substring(int beginIndex)
    {
        return new Sana(toString.substring(beginIndex));
    }

    public Sana substring(int beginIndex, int endIndex)
    {
        return new Sana(toString.substring(beginIndex, endIndex));
    }
    
    @Override
    public int hashCode()
    {
        return toString.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Sana other = (Sana) obj;
        if (!Objects.equals(this.toString, other.toString))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return toString;
    }

    @Override
    public int compareTo(Sana o)
    {
        int min = min(syllables.length, o.syllables.length);
        for (int ii=0;ii<min;ii++)
        {
            int c = syllables[ii].compareTo(o.syllables[ii]);
            if (c != 0)
            {
                return c;
            }
        }
        return syllables.length - o.syllables.length;
    }
    
}
