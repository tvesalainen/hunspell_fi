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
import java.io.OutputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import static org.vesalainen.dict.hunspell.Sijamuoto.INFINITIIVI;
import org.vesalainen.hunspell.jaxb.EsimerkkiType;
import org.vesalainen.hunspell.jaxb.ObjectFactory;
import org.vesalainen.hunspell.jaxb.SijaType;
import org.vesalainen.hunspell.jaxb.Sijat;
import org.vesalainen.hunspell.jaxb.TaivutusTyyppiType;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SijatFile
{
    private static ObjectFactory factory = new ObjectFactory();

    private JAXBContext jaxbCtx;
    private Sijat sijat;
    private Path path;
    private Map<Integer,SijaType> map = new HashMap<>();

    public SijatFile()
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.hunspell.jaxb");
            sijat = factory.createSijat();
        }
        catch (JAXBException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public SijatFile(Path path)
    {
        this.path = path;
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.hunspell.jaxb");
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            sijat = (Sijat) unmarshaller.unmarshal(path.toFile());
            for (SijaType sija : sijat.getSija())
            {
                map.put(sija.getNumero(), sija);
            }
        }
        catch (JAXBException ex)
        {
            throw new RuntimeException(ex);
        }
    }
            
    public SijatFile(InputStream is)
    {
        this.path = path;
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.hunspell.jaxb");
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            sijat = (Sijat) unmarshaller.unmarshal(is);
            for (SijaType sija : sijat.getSija())
            {
                map.put(sija.getNumero(), sija);
            }
        }
        catch (JAXBException ex)
        {
            throw new RuntimeException(ex);
        }
    }
            
    public static MapList<Integer, TaivutusTyyppi> lataaTaivutukset()
    {
        MapList<Integer, TaivutusTyyppi> ml = new HashMapList<>();
        InputStream is = ClassLoader.getSystemResourceAsStream("taivutustyypit.xml");
        SijatFile sijat = new SijatFile(is);
        for (SijaType sija : sijat.sijat.getSija())
        {
            Integer numero = sija.getNumero();
            for (EsimerkkiType e : sija.getEsimerkki())
            {
                String perus = e.getPerus();
                String ehto = e.getEhto();
                for (TaivutusTyyppiType t : e.getTaivutus())
                {
                    String esim = t.getEsim();
                    Sijamuoto sijaMuoto = Sijamuoto.valueOf(t.getSija().toUpperCase());
                    ml.add(numero, new TaivutusTyyppi(sijaMuoto, Sfx.diff(perus, esim), Sfx.suffix(perus, esim), ehto));
                }
            }
        }
        return ml;
    }
    static SijatFile loadDefault() throws IOException
    {
        SijatFile sijat = new SijatFile();
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
            int verbiRaja = INFINITIIVI.ordinal();
            while (line != null)
            {
                if (Character.isDigit(line.charAt(0)))
                {
                    index = Integer.parseInt(line);
                    if (index == 52)
                    {
                        nomini = false;
                    }
                    sijaIndex = nomini ? 0 : verbiRaja;
                }
                else
                {
                    if (line.charAt(0) == '-')
                    {
                        sijaIndex = nomini ? 0 : verbiRaja;
                    }
                    else
                    {
                        switch (Sijamuoto.values()[sijaIndex])
                        {
                            case YKS_NOMINATIIVI:
                            case INFINITIIVI:
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
                            sijat.addEsim(nomini, index, sija, sana, str, ehto);
                        }
                        sijaIndex++;
                    }
                    
                }
                line = br.readLine();
            }
        }
        return sijat;
    }
    public void store(Path path)
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(sijat, path.toFile());
        }
        catch (JAXBException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public void print(OutputStream out)
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(sijat, out);
        }
        catch (JAXBException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public void addEsim(boolean nomini, int num, Sijamuoto sija, String perus, String esim, String ehto)
    {
        SijaType sijaType = map.get(num);
        if (sijaType == null)
        {
            sijaType = factory.createSijaType();
            sijat.getSija().add(sijaType);
            map.put(num, sijaType);
            sijaType.setNumero(num);
            sijaType.setTyyppi(nomini?"nomini":"verbi");
        }
        EsimerkkiType esimerkki = null;
        for (EsimerkkiType e : sijaType.getEsimerkki())
        {
            if (perus.equals(e.getPerus()))
            {
                esimerkki = e;
                break;
            }
        }
        if (esimerkki == null)
        {
            esimerkki = factory.createEsimerkkiType();
            sijaType.getEsimerkki().add(esimerkki);
            esimerkki.setPerus(perus);
            esimerkki.setEhto(ehto);
        }
        TaivutusTyyppiType taivutusTyyppi = factory.createTaivutusTyyppiType();
        esimerkki.getTaivutus().add(taivutusTyyppi);
        taivutusTyyppi.setSija(sija.toString().toLowerCase());
        taivutusTyyppi.setEsim(esim);
    }
}
