package com.digitallib.manager;

import com.digitallib.exception.RepositoryException;
import com.digitallib.model.Classe;
import com.digitallib.model.SubClasse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CategoryManager {


    static Logger logger = LogManager.getLogger();

    static CategoryMapper mapper = loadMapper();

    static CategoryMapper loadMapper() {
        String configFilePath = "classes.yaml";
        InputStream inputStream;

        try {
            inputStream = LoadConfigAsStream(configFilePath);
            logger.log(Level.INFO,"Classes config file found.");
        } catch (FileNotFoundException e) {
            inputStream = CategoryManager.class.getResourceAsStream("/classes.yaml");
            logger.log(Level.INFO,"Classes config file not found. Using default.");
        }

        Yaml yaml = new Yaml();
        return yaml.loadAs(inputStream, CategoryMapper.class);

    }

    private static FileInputStream LoadConfigAsStream(String configFilePath) throws FileNotFoundException {
        InputStream inputStream;
        File configFile = new File(configFilePath);

        if (!configFile.exists()) {
            throw new FileNotFoundException("classes.yaml file not found!!!");
        }

        return new FileInputStream(configFile);
    }

    public List<Classe> getClasses() {
        return mapper.getClasses();
    }

    public String[] getClasseAsStringArray() {
        String[] classes = mapper.getClasses().stream()
                .map(Classe::getDesc)
                .toArray(String[]::new);
        return classes;
    }
    public String[] getClasseAndCodeAsStringArray() {
        String[] classes = mapper.getClasses().stream()
                .map(classe -> String.format("%s. %s", classe.getCode(), classe.getDesc()))
                .toArray(String[]::new);
        return classes;
    }


    public List<SubClasse> getSubClassesForIndex(int index) {
        return mapper.getClasses().get(index).getSubclasses();
    }


    public Classe getClasseForIndex(int index) {
        return mapper.getClasses().get(index);
    }


    public int getPositionInList(Classe classe) {
        int pos = IntStream.range(0, mapper.getClasses().size())
                .filter(i -> mapper.getClasses().get(i).equals(classe))
                .findFirst()
                .orElse(-1);
        return pos;
    }

    public Classe getClasseForName(String name) throws RepositoryException {
        List<Classe> filteredList = mapper.getClasses()
                .stream()
                .filter(classe -> classe.getName().equals(name))
                .collect(Collectors.toList());
        if (filteredList.isEmpty())
            throw new RepositoryException("Classe não encontrada presente em documento. Classe: " + name);
        return filteredList.get(0);
    }

    public SubClasse getSubClasseForName(String subName) throws RepositoryException {
        for (Classe classe : mapper.getClasses()) {
            List<SubClasse> filteredList = classe.getSubclasses()
                    .stream()
                    .filter(subClasse -> subClasse.getName().equals(subName))
                    .collect(Collectors.toList());
            if (!filteredList.isEmpty()) {
                return filteredList.get(0);
            }
        }
        throw new RepositoryException("Could not find category: " + subName);
    }


}
