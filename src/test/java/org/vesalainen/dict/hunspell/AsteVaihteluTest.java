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
import static org.vesalainen.dict.hunspell.Sijamuoto.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AsteVaihteluTest
{
    
    public AsteVaihteluTest()
    {
    }

    @Test
    public void testA()
    {
        AsteVaihtelu a = AsteVaihtelu.getInstance('A');
        assertEquals("takki", a.vaihtele(1, YKS_NOMINATIIVI,"takki"));
        assertEquals("taki", a.vaihtele(1, YKS_GENETIIVI,"takki"));
        assertEquals("takki", a.vaihtele(1, YKS_PARTITIIVI,"takki"));
        assertEquals("takki", a.vaihtele(1, YKS_ILLATIIVI,"takki"));
        assertEquals("taki", a.vaihtele(1, MON_NOMINATIIVI,"takki"));
        assertEquals("takki", a.vaihtele(1, YKS_PARTITIIVI,"takki"));
        assertEquals("takki", a.vaihtele(1, MON_GENETIIVI,"takki"));
        assertEquals("takki", a.vaihtele(1, MON_ILLATIIVI,"takki"));
    }
    
}
