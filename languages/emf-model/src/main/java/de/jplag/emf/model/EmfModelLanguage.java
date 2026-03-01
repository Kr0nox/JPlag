package de.jplag.emf.model;

import java.io.File;
import java.util.List;
import java.util.Set;

import de.jplag.Language;
import de.jplag.ParsingException;
import de.jplag.Token;
import de.jplag.emf.dynamic.DynamicEmfLanguage;
import de.jplag.emf.model.parser.DynamicModelParser;
import de.jplag.options.LanguageOptions;

import com.google.auto.service.AutoService;

/**
 * Language for EMF metamodels from the Eclipse Modeling Framework (EMF). This language is based on a dynamically
 * created token set.
 * @author Timur Saglam
 */
@AutoService(Language.class)
public class EmfModelLanguage extends DynamicEmfLanguage {

    private final EmfLanguageOptions options = new EmfLanguageOptions();

    @Override
    public List<String> fileExtensions() {
        return List.of();
    }

    @Override
    public String getName() {
        return "EMF models (dynamically created token set)";
    }

    @Override
    public String getIdentifier() {
        return "emf-model";
    }

    @Override
    public String viewFileExtension() {
        return ".treeview";
    }

    @Override
    public LanguageOptions getOptions() {
        return options;
    }

    @Override
    public List<Token> parse(Set<File> files, boolean normalize) throws ParsingException {
        return new DynamicModelParser().parse(files, normalize);
    }

    @Override
    public boolean supportsMultiLanguage() {
        return false;
    }
}
