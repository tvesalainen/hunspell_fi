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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AsteVaihtelu
{
    private static final AsteVaihtelu NOLLA = new Nolla();
    public static AsteVaihtelu getInstance(char aste)
    {
        switch (aste)
        {
            case 'A':
                return new ABC("kk:k");
            case 'B':
                return new ABC("pp:p");
            case 'C':
                return new ABC("tt:t");
            case 'D':
                return new D("k:");
            case 'E':
                return new ABC("p:v");
            case 'F':
                return new ABC("t:d");
            case 'G':
                return new ABC("nk:ng");
            case 'H':
                return new ABC("mp:mm");
            case 'I':
                return new ABC("lt:ll");
            case 'J':
                return new ABC("nt:nn");
            case 'K':
                return new ABC("rt:rr");
            case 'L':
                return new ABC("k:j");
            case 'M':
                return new ABC("k:v");
            default:
                return NOLLA;
        }
    }
    
    protected String a;
    protected String b = "";
    protected Replacer replacer = this::repl;

    private AsteVaihtelu()
    {
    }

    private AsteVaihtelu(String aste)
    {
        String[] split = aste.split("[\\:]");
        a = split[0];
        if (split.length > 1)
        {
            b = split[1];
        }
    }
    public String vaihtele(int inflection, Sijamuoto sija, String sana)
    {
        switch (inflection)
        {        
            case 1:
            case 4:
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
            case 14:
            case 16:
            case 28:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 76:
                return xx2x(sija, sana);
            case 32:
            case 33:
            case 34:
            case 35:
            case 41:
            case 43:
            case 48:
            case 49:
            case 66:
            case 67:
            case 72:
            case 73:
            case 74:
            case 75:
                return x2xx(sija, sana);
            default:
                throw new RuntimeException(sana+" vaihtelu unknown in "+inflection);
        }
    }
    protected String x2xx(Sijamuoto sija, String sana)
    {
        return sana;
    }
    protected String xx2x(Sijamuoto sija, String sana)
    {
        return sana;
    }
    
    protected String xx2x(String sana)
    {
        return replacer.action(sana, a, b);
    }
    protected String x2xx(String sana)
    {
        return replacer.action(sana, b, a);
    }
    protected String repl(String str, String from, String to)
    {
        int idx = str.lastIndexOf(from);
        if (idx == -1)
        {
            if (str.endsWith(to))
            {
                return str;
            }
            System.err.println(str+" !!!");
            return str;
        }
        return str.substring(0, idx)+to+str.substring(idx+from.length());
    }
    public void setReplacer(Replacer replacer)
    {
        this.replacer = replacer;
    }
        
    private static class ABC extends AsteVaihtelu
    {
        private ABC(String xxx)
        {
            super(xxx);
        }

        @Override
        public String xx2x(Sijamuoto sija, String sana)
        {
            switch (sija)
            {
                case YKS_GENETIIVI:
                case MON_NOMINATIIVI:
                case AKT_IND_PREES_YKS_1_PERS:
                case PASS_IMPERF:
                    return xx2x(sana);
                default:
                    return sana;
            }
        }
        @Override
        public String x2xx(Sijamuoto sija, String sana)
        {
            switch (sija)
            {
                case YKS_GENETIIVI:
                case YKS_ILLATIIVI:
                case MON_NOMINATIIVI:
                case MON_GENETIIVI:
                case MON_PARTITIIVI:
                case MON_ILLATIIVI:
                case AKT_IND_PREES_YKS_1_PERS:
                case AKT_IND_INPERF_YKS_3_PERS:
                case AKT_KOND_PREES_YKS_3_PERS:
                    return x2xx(sana);
                default:
                    return sana;
            }
        }
    }
    private static class D extends ABC
    {
        private D(String xxx)
        {
            super(xxx);
        }

        @Override
        public String x2xx(Sijamuoto sija, String sana)
        {
            if ("aie".equals(sana))
            {
                System.err.println();
            }
            switch (sija)
            {
                case YKS_GENETIIVI:
                case YKS_ILLATIIVI:
                case MON_NOMINATIIVI:
                case MON_GENETIIVI:
                case MON_PARTITIIVI:
                case MON_ILLATIIVI:
                    if (sana.endsWith("e"))
                    {
                        return repl(sana, "e", "ke");
                    }
                    else
                    {
                        int len = sana.length();
                        String r = sana.substring(len-2, len-1);
                        return repl(sana, r, "k"+r);
                    }
                default:
                    return super.x2xx(sija, sana);
            }
        }
    }
    private static class Nolla extends AsteVaihtelu
    {

        @Override
        public String vaihtele(int inflection, Sijamuoto sija, String sana)
        {
            return sana;
        }
        
    }
}
