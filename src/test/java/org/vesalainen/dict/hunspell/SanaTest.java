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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SanaTest
{
    
    public SanaTest()
    {
    }

    @Test
    public void test1()
    {
        Sana a = new Sana("aivastus");
        Sana b = new Sana("huuto");
        assertEquals("aivastus", a.toString());
        assertArrayEquals(new String[]{"ai", "vas", "tus"}, a.getSyllables());
        assertTrue(a.compareTo(b) != 0);
        assertEquals(0, a.compareTo(a));
    }
    @Test
    public void test2()
    {
        Sana a = new Sana("suuri");
        Sana b = new Sana("suur");
        Sana c = new Sana("suu");
        assertFalse(a.startsWith(b));
        assertFalse(b.startsWith(a));
        assertTrue(a.startsWith(c));
    }    
    @Test
    public void test3()
    {
        Sana a = new Sana("pikkutakki");
        Sana b = new Sana("akki");
        Sana c = new Sana("takki");
        assertFalse(a.endsWith(b));
        assertTrue(a.endsWith(c));
    }    
    @Test
    public void test4()
    {
        Sana a = new Sana("takki");
        Sana b = new Sana("talvi");
        Sana c = new Sana("talvitakki");
        assertEquals(c, b.concat(a));
    }    
}
