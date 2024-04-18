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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Reps
{
    private static final Map<String,String> map = new HashMap<>();

    static
    {
        map.put("alfa-alfa", "alfalfa");
        map.put("alppinismi", "alpinismi");
        map.put("alppinisti", "alpinisti");
        map.put("aplikaattori", "applikaattori");
        map.put("asteettain", "asteittain");
        map.put("betaversio", "beetaversio");
        map.put("enään", "enää");
        map.put("huoneusto", "huoneisto");
        map.put("hälyyttää", "hälyttää");
        map.put("hälyytys", "hälytys");
        map.put("joni", "ioni");
        map.put("jute", "juutti");
        map.put("kansantiede", "kansatiede");
        map.put("kehoittaa", "kehottaa");
        map.put("kehoitus", "kehotus");
        map.put("kemikalio", "kemikaalikauppa");
        map.put("kertoin", "kerroin");
        map.put("kuullostaa", "kuulostaa");
        map.put("mitalli", "mitali");
        map.put("naivi", "naiivi");
        map.put("ongelmatiikka", "ongelmat");
        map.put("paneli", "paneeli");
        map.put("paria", "paaria");
        map.put("pure", "pyree");
        map.put("rationoida", "rationaalistaa");
        map.put("renesanssi", "renessanssi");
        map.put("sairalloinen", "sairaalloinen");
        map.put("sairaloinen", "sairaalloinen");
        map.put("scientologia", "skientologia");
        map.put("seetri", "setri");
        map.put("tiitteri", "titteri");
        map.put("vaakita", "vaaita");
        map.put("vaakitus", "vaaitus");
        map.put("verytellä", "verrytellä");
        map.put("veryttely", "verryttely");
    }

    public static void print(PrintWriter p)
    {
        p.format("REP %d\n", map.size());
        map.forEach((r, w)->p.format("REP %s %s\n", r, w));
    }
    
}
