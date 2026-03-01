package de.jplag.emf.model.parser;

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jplag.ParsingException;
import de.jplag.emf.EmfLanguage;
import de.jplag.emf.dynamic.parser.DynamicEcoreParser;
import de.jplag.emf.util.AbstractModelView;
import de.jplag.emf.util.EMFUtil;
import de.jplag.emf.util.GenericEmfTreeView;

/**
 * Parser for EMF metamodels based on dynamically created tokens. This means each model element corresponds to a token
 * which is typed according to the model elements type.
 */
public class DynamicModelParser extends DynamicEcoreParser {
    private static final Logger logger = LoggerFactory.getLogger(DynamicModelParser.class);

    private static final String VIEW_FILE_WARNING = "Skipping view file {} as submission!";
    private static final String METAMODEL_WARNING = "Metamodel files like {} should be specified via the language options!";
    private static final String ALL_EXTENSIONS = "*";

    /**
     * Creates the parser.
     */
    public DynamicModelParser() {
        EMFUtil.registerModelExtension(ALL_EXTENSIONS);
    }

    @Override
    protected void parseModelFile(File file, boolean normalize) throws ParsingException {
        if (file.getName().endsWith(EmfLanguage.FILE_ENDING)) {
            logger.warn(METAMODEL_WARNING, file.getName());
        } else if (file.getName().endsWith(EmfLanguage.VIEW_FILE_EXTENSION)) {
            logger.warn(VIEW_FILE_WARNING, file.getName());
        } else {
            super.parseModelFile(file, normalize);
        }
    }

    @Override
    protected String getCorrespondingViewFileExtension() {
        return EmfLanguage.VIEW_FILE_EXTENSION;
    }

    @Override
    protected AbstractModelView createView(File file, Resource modelResource) {
        return new GenericEmfTreeView(file, modelResource);
    }
}
