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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.vesalainen.dict.hunspell.Sfx.nextFlag;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Pfx
{
    private static final Map<Set<String>,Pfx> map = new HashMap<>();

    private char flag;
    private Set<String> set;

    public static Pfx getInstance(Set<String> set)
    {
        Pfx pfx = map.get(set);
        if (pfx == null)
        {
            pfx = new Pfx(set);
            map.put(set, pfx);
        }
        return pfx;
    }

    public char getFlag()
    {
        return flag;
    }

    public static Collection<Pfx> getPrefixes()
    {
        return map.values();
    }
    
    private Pfx(Set<String> prefixes)
    {
        this.flag = nextFlag();
        this.set = prefixes;
    }

    void print(PrintWriter pw)
    {
        pw.println("# ");
        pw.format("PFX %c Y %d\n", flag, set.size());
        set.forEach((s)->pw.format("PFX %c 0 %s\n", flag, s));
    }
    
}
