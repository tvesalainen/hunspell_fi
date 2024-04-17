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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Integer.min;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class  Sfx implements Comparable<Sfx>
{
    private static final Map<String,Sfx> map = new TreeMap<>();
    private static final MapList<Integer,TaivutusTyyppi> taivutusTyyppi = new HashMapList<>();
    private static char flagSeq=' '+1;
    private int inflection;
    private char grade;
    private char flag;
    protected AsteVaihtelu asteVaihtelu;
    private Set<String> words = new HashSet<>();

    static
    {
        loadTaivutusTyypit();
    }
    public static Sfx getInstance(int inflection, char grade)
    {
        if (inflection <= 78)
        {
            String key = String.format("%d%c", inflection, grade);
            Sfx inf = map.get(key);
            if (inf == null)
            {
                switch (inflection)
                {
                    case 73:
                        inf = new Inflection73(inflection, grade, nextFlag());
                        break;
                    default:
                        inf = new Sfx(inflection, grade, nextFlag());
                        break;
                }
                map.put(key, inf);
            }
            return inf;
        }
        else
        {
            return null;
        }
    }
    public static char nextFlag()
    {
        while (!Character.isLetterOrDigit(flagSeq))
        {
            flagSeq++;
        }
        return flagSeq++;
    }
    public static List<Sfx> getAllInflections() 
    {
        List<Sfx> list = new ArrayList<>(map.values());
        list.sort(null);
        return list;
    }

    public static char getFlagSeq()
    {
        return flagSeq;
    }

    protected Sfx(int inflection, char grade, char flag)
    {
        this.inflection = inflection;
        this.grade = grade;
        this.flag = flag;
        this.asteVaihtelu = AsteVaihtelu.getInstance(grade);
    }

    public void print(PrintWriter pw)
    {
        pw.print("# ");
        pw.println(this);
        List<TaivutusTyyppi> list = taivutusTyyppi.get(inflection);
        Set<S> set = new TreeSet<>();
        for (String nom : words)
        {
            boolean hadIt = false;
            for (TaivutusTyyppi t : list)
            {
                if (t.ehto.equals(".") || nom.endsWith(t.ehto))
                {
                    switch (t.sija)
                    {
                        case YKS_NOMINATIIVI:
                        case INFINITIIVI:
                            break;
                        default:
                            try
                            {
                                String sana = asteVaihtelu.vaihtele(inflection, t.sija, nom);
                                String taiv = t.taivuta(sana);
                                set.add(new S(flag, nom, taiv, t));
                            }
                            catch (Exception ex)
                            {
                                throw ex;
                            }
                            break;
                    }
                    hadIt = true;
                }
            }
            if (!hadIt)
            {
                throw new RuntimeException(nom+" didn't have inflection "+this);
            }
        }
        pw.format("SFX %c Y %d\n", flag, set.size());
        set.forEach((s)->pw.println(s));
    }
    private static String suffix(String nom, String taiv)
    {
        String com = comPrf(nom, taiv);
        return taiv.substring(com.length());
    }
    private static String diff(String nom, String taiv)
    {
        if (taiv.startsWith(nom))
        {
            return null;
        }
        else
        {
            String com = comPrf(nom, taiv);
            return nom.substring(com.length(), nom.length());
        }
        
    }
    private static String comPrf(String nom, String taiv)
    {
        StringBuilder sb = new StringBuilder();
        int len = min(nom.length(), taiv.length());
        for (int ii=0;ii<len;ii++)
        {
            if (nom.charAt(ii) == taiv.charAt(ii))
            {
                sb.append(nom.charAt(ii));
            }
            else
            {
                break;
            }
        }
        return sb.toString();
    }
    public int getInflection()
    {
        return inflection;
    }

    public char getGrade()
    {
        return grade;
    }

    public char getFlag()
    {
        return flag;
    }

    public void addWord(String word)
    {
        words.add(word);
    }
    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + this.inflection;
        hash = 67 * hash + this.grade;
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
        final Sfx other = (Sfx) obj;
        if (this.inflection != other.inflection)
        {
            return false;
        }
        if (this.grade != other.grade)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        if (grade != 0)
        {
            return inflection + "*" + grade;
        }
        else
        {
            return inflection+"";
        }
    }

    @Override
    public int compareTo(Sfx inf)
    {
        if (inf.inflection == inflection)
        {
            return grade - inf.grade;
        }
        else
        {
            return inflection - inf.inflection;
        }
    }

    private static void loadTaivutusTyypit()
    {
        InputStream is = ClassLoader.getSystemResourceAsStream("taivutustyypit.txt");
        try (InputStreamReader isr = new InputStreamReader(is, UTF_8);
                BufferedReader br = new BufferedReader(isr);)
        {
            Sijamuoto sija = Sijamuoto.values()[0];
            int index = 0;
            int sijaIndex = 0;
            boolean nomini = true;
            String line = br.readLine();
            String sana = "";
            String ehto = null;
            while (line != null)
            {
                if (Character.isDigit(line.charAt(0)))
                {
                    index = Integer.parseInt(line);
                    if (index == 52)
                    {
                        nomini = false;
                    }
                    sijaIndex = nomini ? 0 : 8;
                }
                else
                {
                    if (line.charAt(0) == '-')
                    {
                        sijaIndex = nomini ? 0 : 8;
                    }
                    else
                    {
                        switch (sijaIndex)
                        {
                            case 0:
                            case 8:
                                line = line.replace(".", "");
                                int idx = line.indexOf('|');
                                if (idx != -1)
                                {
                                    ehto = line.substring(idx+1);
                                    line = line.replace("|", "");
                                }
                                else
                                {
                                    ehto = ".";
                                }
                                sana = line;
                                break;
                        }
                        sija = Sijamuoto.values()[sijaIndex];
                        String[] split = line.replaceAll("[\\.\\(\\)]", "").split("[ /]+");
                        for (String str : split)
                        {
                            taivutusTyyppi.add(index, new TaivutusTyyppi(sija, diff(sana, str), suffix(sana, str), ehto));
                        }
                        sijaIndex++;
                    }
                    
                }
                line = br.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(S.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static class S implements Comparable<S>
    {
        private char flag;
        private String base;
        private String full;
        private TaivutusTyyppi tyyppi;
        private String strip;
        private String suffix;
        private String cond;
        
        public S(char flag, String base, String full, TaivutusTyyppi tyyppi)
        {
            this.flag = flag;
            this.base = base;
            this.full = full;
            this.tyyppi = tyyppi;
            this.strip = diff(base, full);
            this.suffix = suffix(base, full);
            if (strip==null||tyyppi.ehto.length()>strip.length())
            {
                this.cond = tyyppi.ehto;
            }
            else
            {
                this.cond = strip;
            }
        }

        @Override
        public String toString()
        {
            return String.format("SFX %c %s %s %s", 
                    flag, 
                    strip==null?"0":strip,
                    suffix.isEmpty()?"0":suffix, 
                    cond==null?".":cond
            );
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 89 * hash + this.flag;
            hash = 89 * hash + Objects.hashCode(this.base);
            hash = 89 * hash + Objects.hashCode(this.full);
            hash = 89 * hash + Objects.hashCode(this.tyyppi);
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
            final S other = (S) obj;
            if (this.flag != other.flag)
            {
                return false;
            }
            if (!Objects.equals(this.base, other.base))
            {
                return false;
            }
            if (!Objects.equals(this.full, other.full))
            {
                return false;
            }
            if (!Objects.equals(this.tyyppi, other.tyyppi))
            {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(S o)
        {
            return toString().compareTo(o.toString());
        }

        
    }
    private static class TaivutusTyyppi
    {
        private Sijamuoto sija;
        private String strip;
        private String prefix;
        private String ehto;

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
                return sana.substring(0, sana.length()-strip.length())+prefix;
            }
            else
            {
                return sana+prefix;
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
}
