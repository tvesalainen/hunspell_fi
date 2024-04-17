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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Inflection73 extends Sfx
{

    protected Inflection73(int inflection, char grade, char nextFlag)
    {
        super(inflection, grade, nextFlag);
        this.asteVaihtelu.setReplacer(this::repl73);
    }
    protected String repl73(String str, String from, String to)
    {
        int idx = str.lastIndexOf(from, str.length()-3);
        return str.substring(0, idx)+to+str.substring(idx+from.length());
    }
    
}
