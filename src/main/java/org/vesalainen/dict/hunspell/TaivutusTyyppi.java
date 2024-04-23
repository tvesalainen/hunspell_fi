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

import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TaivutusTyyppi
{
    
    Sijamuoto sija;
    private String strip;
    private String prefix;
    String ehto;

    public TaivutusTyyppi(Sijamuoto sija, String strip, String prefix, String ehto)
    {
        this.sija = sija;
        this.strip = strip;
        this.prefix = prefix;
        this.ehto = ehto;
    }

    public String taivuta(String sana)
    {
        if (strip != null)
        {
            if (sana.length() < strip.length())
            {
                throw new IllegalArgumentException(sana);
            }
            return sana.substring(0, sana.length() - strip.length()) + prefix;
        }
        else
        {
            return sana + prefix;
        }
    }

    @Override
    public String toString()
    {
        return "sija=" + sija + ", taivutus=" + prefix + '}';
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.sija);
        hash = 59 * hash + Objects.hashCode(this.strip);
        hash = 59 * hash + Objects.hashCode(this.prefix);
        hash = 59 * hash + Objects.hashCode(this.ehto);
        return hash;
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
        final TaivutusTyyppi other = (TaivutusTyyppi) obj;
        if (!Objects.equals(this.strip, other.strip))
        {
            return false;
        }
        if (!Objects.equals(this.prefix, other.prefix))
        {
            return false;
        }
        if (!Objects.equals(this.ehto, other.ehto))
        {
            return false;
        }
        if (this.sija != other.sija)
        {
            return false;
        }
        return true;
    }
    
}
