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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
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
    private Map<String,Word> map = new TreeMap<>();
    private NavigableMap<String,Word> reverseMap = new TreeMap<>();
    private List<Word> reverseList = new ArrayList<>();
    
    public void createFiles(Path dir) throws IOException
    {
        URL url = ClassLoader.getSystemResource("extra.csv");
        parse(url);
        url = ClassLoader.getSystemResource("nykysuomensanalista2022.csv");
        parse(url);
        reverseList.sort((k1,k2)->{return k2.getWord().length()-k1.getWord().length();});
        for (Word word : reverseList)
        {
            String reverse = reverse(word.getWord());
            String key = reverseMap.higherKey(reverse);
            while (key != null && key.startsWith(reverse))
            {
                Word wrd = reverseMap.get(key);
                wrd.setInflections(word.getInflections());
                reverseMap.remove(key);
                key = reverseMap.higherKey(key);
            }
        }
        reverseMap = null;
        reverseList = null;
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
            apw.println();
            allInflections.forEach((inf)->inf.print(apw));
        }
    }
    
    @Rule("word lf")
    protected void line(String word)
    {
        Word w = new Word(word, null, null, null);
        map.put(word, w);
    }
    @Rule("word '\t' homonym? '\t' wordClassList? '\t' inflectionList? lf")
    protected void line(String word, Integer homonym, List<WordClass> classes, List<Sfx> inflections)
    {
        if (!word.startsWith("-") && !word.endsWith("-"))
        {
            Word w = new Word(word, homonym, classes, inflections);
            map.put(word, w);
            if (inflections != null)
            {
                reverseList.add(w);
                inflections.forEach((inf)->inf.addWord(word));
            }
            else
            {
                reverseMap.put(reverse(word), w);
            }
        }
        else
        {
            System.err.println(word);
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
    @Terminal(expression = "[^\t]+")
    protected String word(String sana)
    {
        sana = sana.toLowerCase();
        switch (sana)
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
                return "terkku";
            case "farkut":
                return "farkku";
            case "menkut":
                return "menkku";
            case "reput":
                return "reppu";
            case "lemput":
                return "lemppu";
            case "urut":
                return "urku";
            case "pidot":
                return "pito";
            case "käädyt":
                return "kääty";
            case "opinnot":
                return "opinto";
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

    @ParseMethod(start = "dictionary", size = 1024, charSet = "UTF-8", features={WideIndex, UseDirectBuffer})
    protected abstract <I> void parse(I input);
    
    public static Dictionary newInstance()
    {
        return (Dictionary) GenClassFactory.loadGenInstance(Dictionary.class);
    }

}
