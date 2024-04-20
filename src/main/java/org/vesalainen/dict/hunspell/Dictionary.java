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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.vesalainen.parser.GenClassFactory;
import static org.vesalainen.parser.ParserFeature.UseDirectBuffer;
import static org.vesalainen.parser.ParserFeature.WideIndex;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.AbstractParser;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname("org.vesalainen.dict.hunspell.DictionaryImpl")
@GrammarDef()
@Rules(
{
    @Rule(left = "dictionary", value = "header lf line*"),
})
public abstract class Dictionary extends AbstractParser
{
    private NavigableMap<Sana,WordEntry> map = new TreeMap<>();
    private NavigableMap<String,WordEntry> reverseMap = new TreeMap<>();
    private List<WordEntry> reverseList = new ArrayList<>();
    private Set<Sana> prefixes = new TreeSet<>();
    private Map<Sana,WordEntry> suffixes = new HashMap<>();
    
    public void createFiles(Path dir) throws IOException
    {
        URL url = ClassLoader.getSystemResource("extra.csv");
        parse(url);
        url = ClassLoader.getSystemResource("nykysuomensanalista2022.csv");
        parse(url);
        reverseList.sort((k1,k2)->{return k2.getWord().length()-k1.getWord().length();});
        for (WordEntry word : reverseList)
        {
            String reverse = reverse(word.getWord().toString());
            String key = reverseMap.higherKey(reverse);
            while (key != null && key.startsWith(reverse))
            {
                WordEntry wrd = reverseMap.get(key);
                wrd.setInflections(word.getInflections());
                key = reverseMap.higherKey(key);
            }
        }
        reverseList = null;
        processPrefixes();
        reverseMap = null;
        Path dic = dir.resolve("fi_FI.dic");
        try (BufferedWriter dicWriter = Files.newBufferedWriter(dic, UTF_8);
            PrintWriter pw = new PrintWriter(dicWriter);)
        {
            pw.format("%d\n", map.size());
            map.forEach((k,w)->
            {
                pw.println(w);
            });
        }
        // aff
        List<Sfx> allInflections = Sfx.getAllInflections();
        Path aff = dir.resolve("fi_FI.aff");
        try (BufferedWriter affWriter = Files.newBufferedWriter(aff, UTF_8);
            PrintWriter apw = new PrintWriter(affWriter);)
        {
            apw.println("SET UTF-8");
            apw.println("FLAG UTF-8");
            apw.println("KEY qwertyuiopå|asdfghjklöä|zxcvbnm");
            Reps.print(apw);
            apw.println();
            Pfx.getPrefixes().forEach((p)->p.print(apw));
            apw.println();
            allInflections.forEach((inf)->inf.print(apw));
        }
    }
    private void processPrefixes()
    {
        MapSet<Sana,Sana> sfxMap = new HashMapSet<>();
        prefixes.forEach((pfx)->
        {
            int len = pfx.length();
            NavigableMap<Sana, WordEntry> tailMap = map.tailMap(pfx, false);
            for (Entry<Sana, WordEntry> e : tailMap.entrySet())
            {
                Sana word = e.getKey();
                if (!word.startsWith(pfx))
                {
                    break;
                }
                Sana suf = word.substring(len);
                sfxMap.add(suf, pfx);
            }
        });
        suffixes.forEach((s, we)->
        {
            Sana sfx = we.getWord();
            String rev = reverse(sfx.toString());
            int len = sfx.length();
            SortedMap<String, WordEntry> tailMap = reverseMap.tailMap(rev);
            for (Entry<String, WordEntry> e : tailMap.entrySet())
            {
                String key = e.getKey();
                if (!key.startsWith(rev))
                {
                    break;
                }
                WordEntry entry = e.getValue();
                Sana word = entry.getWord();
                if (word.endsWith(sfx))
                {
                    Sana pref = word.substring(0, word.length()-len);
                    sfxMap.add(sfx, pref);
                }
            }
        });
        for (Entry<Sana,Set<Sana>> e : sfxMap.entrySet())
        {
            Sana base = e.getKey();
            Set<Sana> set = e.getValue();
            if (set.size() > 1)
            {
                boolean sameType = true;
                WordEntry newWord = null;
                for (Sana prf : set)
                {
                    Sana w = prf.concat(base);
                    WordEntry word = map.get(w);
                    if (word == null)
                    {
                        sameType = false;
                        break;
                    }
                    if (newWord == null)
                    {
                        newWord = word.copy(base);
                    }
                    else
                    {
                        if (!word.typeEquals(newWord))
                        {
                            sameType = false;
                            break;
                        }
                    }
                }
                if (sameType)
                {
                    for (Sana prf : set)
                    {
                        Sana w = prf.concat(base);
                        map.remove(w);
                        System.err.println("remove "+w);
                    }
                    Pfx pfx = Pfx.getInstance(set);
                    WordEntry se = suffixes.get(base);
                    if (se == null)
                    {
                        newWord.setPfx(pfx);
                        map.put(base, newWord);
                    }
                    else
                    {
                        se.setPfx(pfx);
                        map.put(base, se);
                    }
                }
            }
        }
    }
    @Rule("word lf")
    protected void line(Sana word)
    {
        WordEntry w = new WordEntry(word, null, null, null);
        map.put(word, w);
    }
    @Rule("word '\t' homonym? '\t' wordClassList? '\t' inflectionList? lf")
    protected void line(Sana word, Integer homonym, List<WordClass> classes, List<Sfx> inflections)
    {
        if (!word.startsWith("-") && !word.endsWith("-"))
        {
            WordEntry w = new WordEntry(word, homonym, classes, inflections);
            map.put(word, w);
            if (inflections != null)
            {
                reverseList.add(w);
                inflections.forEach((inf)->inf.addWord(word));
            }
            else
            {
                reverseMap.put(reverse(word.toString()), w);
            }
        }
        else
        {
            if (word.startsWith("-"))
            {
                Sana s = word.substring(1);
                suffixes.put(s, new WordEntry(s, homonym, classes, inflections));
            }
            else
            {
                prefixes.add(word.substring(0, word.length()-1));
            }
        }
    }
    private static String reverse(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=str.length();ii>0;ii--)
        {
            sb.append(str.charAt(ii-1));
        }
        return sb.toString();
    }
    @Rule("wordClass")
    protected List<WordClass> wordClassList(WordClass wordClass)
    {
        ArrayList<WordClass> list = new ArrayList<WordClass>();
        list.add(wordClass);
        return list;
    }
    @Rule("wordClassList ls wordClass")
    protected List<WordClass> wordClassList(List<WordClass> list, WordClass wordClass)
    {
        list.add(wordClass);
        return list;
    }
    @Terminal(expression = "[A-V]+")
    protected WordClass wordClass(String value)
    {
        return WordClass.valueOf(value);
    }

    @Rule("inflection")
    protected List<Sfx> inflectionList(Sfx inflection)
    {
        ArrayList<Sfx> list = new ArrayList<Sfx>();
        if (inflection != null) list.add(inflection);
        return list;
    }
    @Rule("inflectionList ls inflection")
    protected List<Sfx> inflectionList(List<Sfx> list, Sfx inflection)
    {
        if (inflection != null) list.add(inflection);
        return list;
    }
    @Rule("uint")
    @Rule("'\\(' uint '\\)'")
    @Rule("'\\(mon.' uint '\\)'")
    @Rule("'\\(mon. ' uint '\\)'")
    protected Sfx inflection(int inflection)
    {
        return Sfx.getInstance(inflection, (char)0);
    }
    @Rule("uint '\\*' grade")
    @Rule("uint '\\*' '\\(' grade '\\)'")
    @Rule("'\\(' uint '\\*' grade '\\)'")
    protected Sfx inflection(int inflection, char grade)
    {
        return Sfx.getInstance(inflection, grade);
    }
    @Terminal(expression = "[^\t\r\n]+")
    protected Sana word(String str)
    {
        Sana sana = new Sana(str.toLowerCase());
        switch (sana.toString())
        {
            case "housut":
            case "torut":
            case "chinot":
            case "kemut":
            case "kimpsut":
            case "kampsut":
            case "pöksyt":
            case "vällyt":
            case "etkot":
            case "jortsut":
            case "kasvot":
            case "aivot":
            case "hilut":
            case "fudut":
            case "bailut":
            case "mensut":
                return sana.substring(0, sana.length()-1);
            case "terkut":
                return new Sana("terkku");
            case "farkut":
                return new Sana("farkku");
            case "menkut":
                return new Sana("menkku");
            case "reput":
                return new Sana("reppu");
            case "lemput":
                return new Sana("lemppu");
            case "urut":
                return new Sana("urku");
            case "pidot":
                return new Sana("pito");
            case "käädyt":
                return new Sana("kääty");
            case "opinnot":
                return new Sana("opinto");
            default:
                return sana;
        }
    }

    @Terminal(expression = "[A-Z]")
    protected abstract char grade(char value);

    @Terminal(left="homonym", expression = "[\\+]?[0-9]+", signed=false)
    protected Integer homonym(int value)
    {
        return new Integer(value);
    }
    
    @Rule("'Hakusana' wsp 'Homonymia' wsp 'Sanaluokka' wsp 'Taivutustiedot'")
    protected void header()
    {
        
    }
    @Terminal(expression = "\\,[ \t]*")
    protected abstract void ls();

    @Terminal(expression = "[ \t]+")
    protected abstract void wsp();

    @Terminal(expression = "[\r\n]+")
    protected abstract void lf();

    @ParseMethod(start = "dictionary", size = 4096, charSet = "UTF-8", features={WideIndex, UseDirectBuffer})
    protected abstract <I> void parse(I input);
    
    public static Dictionary newInstance()
    {
        return (Dictionary) GenClassFactory.loadGenInstance(Dictionary.class);
    }

}
