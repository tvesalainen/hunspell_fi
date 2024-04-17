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

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Word
{
    private String word;
    private int homonym;
    private WordClass[] classes;
    private Sfx[] inflections;

    public Word(String word, Integer homonym, List<WordClass> classes, List<Sfx> inflections)
    {
        this.word = word;
        if (homonym != null)
        {
            this.homonym = homonym;
        }
        if (classes != null)
        {
            this.classes = classes.toArray(new WordClass[classes.size()]);
        }
        if (inflections != null)
        {
            this.inflections = inflections.toArray(new Sfx[inflections.size()]);
        }
    }

    public void setInflections(Sfx[] inflections)
    {
        this.inflections = inflections;
    }

    public String getWord()
    {
        return word;
    }

    public int getHomonym()
    {
        return homonym;
    }

    public WordClass[] getClasses()
    {
        return classes;
    }

    public Sfx[] getInflections()
    {
        return inflections;
    }

    @Override
    public String toString()
    {
        if (inflections == null || inflections.length == 0)
        {
            return word;
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(word);
            sb.append('/');
            for (Sfx ii : inflections)
            {
                sb.append(ii.getFlag());
            }
            return sb.toString();
        }
    }
    
}
