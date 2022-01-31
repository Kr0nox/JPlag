package de.jplag;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import de.jplag.exceptions.BasecodeException;
import de.jplag.exceptions.ExitException;
import de.jplag.exceptions.RootDirectoryException;
import de.jplag.exceptions.SubmissionException;
import de.jplag.options.JPlagOptions;

/**
 * Builder class for the creation of a {@link SubmissionSet}.
 * @author Timur Saglam
 */
public class SubmissionSetBuilder {

    private final Language language;
    private final JPlagOptions options;
    private final ErrorCollector errorCollector;
    private final Set<String> excludedFileNames; // Set of file names to be excluded in comparison.

    /**
     * Creates a builder for submission sets.
     * @param language is the language of the submissions.
     * @param options are the configured options.
     * @param errorCollector is the interface for error reporting.
     * @param excludedFileNames
     */
    public SubmissionSetBuilder(Language language, JPlagOptions options, ErrorCollector errorCollector, Set<String> excludedFileNames) {
        this.language = language;
        this.options = options;
        this.errorCollector = errorCollector;
        this.excludedFileNames = excludedFileNames;
    }

    /**
     * Builds a submission set for all submissions of a specific directory.
     * @return the newly built submission set.
     * @throws ExitException if the directory cannot be read.
     */
    public SubmissionSet buildSubmissionSet() throws ExitException {
        // Read the root directory and collect valid looking submission entries from it.
        File rootDirectory = new File(options.getRootDirectoryName());
        verifyRootdirExistence(rootDirectory);
        String[] fileNames = readSubmissionRootNames(rootDirectory);
        Map<String, Submission> foundSubmissions = processRootDirEntries(rootDirectory, fileNames);

        // Extract the basecode submission if necessary.
        Optional<Submission> baseCodeSubmission = Optional.empty();
        if (options.hasBaseCode()) {
            Submission baseCode = tryLoadBaseCodeAsPath();
            if (baseCode == null) {
                baseCode = tryLoadBaseCodeAsRootSubDirectory(foundSubmissions);

                String baseCodeName = options.getBaseCodeSubmissionName().get();
                if (baseCode == null) {
                    // No base code found at all, report an error.
                    throw new BasecodeException(
                            String.format("Basecode path \"%s\" relative to the working directory could not be found.", baseCodeName));
                } else {
                    // Found a base code as a submission, report about obsolete usage.
                    System.out.printf("Deprecated use of the -bc option found, please specify the basecode as \"%s%s%s\" instead.\n",
                            rootDirectory.toString(), File.separator, baseCodeName);
                }
            }
            baseCodeSubmission = Optional.of(baseCode);
            System.out.println(String.format("Basecode directory \"%s\" will be used.", baseCode.getRoot().toString()));

            // Basecode may also be registered as a user submission. If so, remove the latter.
            File baseCodeRoot = baseCode.getCanonicalRoot(); // Use canonical form for a more sane equality notion.
            Iterator<Entry<String, Submission>> submissionIterator = foundSubmissions.entrySet().iterator();
            while (submissionIterator.hasNext()) {
                Entry<String, Submission> entry = submissionIterator.next();
                if (baseCodeRoot.equals(entry.getValue().getCanonicalRoot())) {
                    submissionIterator.remove();
                    System.out.println(String.format("Skipping \"%s\" as user submission.", entry.getValue().getRoot().toString()));
                    break;
                }
            }
        }

        // Merge everything in a submission set.
        List<Submission> submissions = new ArrayList<>(foundSubmissions.values());
        return new SubmissionSet(submissions, baseCodeSubmission, errorCollector, options);
    }

    /**
     * Verify that the given root directory exists.
     */
    private void verifyRootdirExistence(File rootDir) throws ExitException {
        String rootDirectoryName = rootDir.getName();
        if (!rootDir.exists()) {
            throw new RootDirectoryException(String.format("Root directory \"%s\" does not exist!", rootDirectoryName));
        }
        if (!rootDir.isDirectory()) {
            throw new RootDirectoryException(String.format("Root directory \"%s\" is not a directory!", rootDirectoryName));
        }
    }

    /**
     * Try to load the basecode under the assumption of being a path.
     * @return Base code submission if the option value can be interpreted as global path, else {@code null}.
     * @throws ExitException when the option value is a path with errors.
     */
    private Submission tryLoadBaseCodeAsPath() throws ExitException {
        String baseCodeName = options.getBaseCodeSubmissionName().get();
        File basecodeSubmission = new File(baseCodeName);
        if (!basecodeSubmission.exists()) {
            return null;
        }

        String errorMessage = isExcludedEntry(basecodeSubmission);
        if (errorMessage != null) {
            throw new BasecodeException(errorMessage); // Stating an excluded path as basecode isn't very useful.
        }

        try {
            return processDirEntry(basecodeSubmission);

        } catch (SubmissionException exception) {
            throw new BasecodeException(exception.getMessage(), exception); // Change thrown exception to basecode exception.

        } catch (ExitException ex) {
            throw ex;
        }
    }

    /**
     * Try to load the basecode under the assumption of being a sub-directory in the user submissions directory.
     * @return Base code submission if the option value can be interpreted as a sub-directory, else {@code null}.
     * @throws ExitException when the option value is a sub-directory with errors.
     */
    private Submission tryLoadBaseCodeAsRootSubDirectory(Map<String, Submission> foundSubmissions) throws ExitException {
        String baseCodeName = options.getBaseCodeSubmissionName().get();

        // Is the option value a single name after trimming spurious separators?
        String name = baseCodeName;
        while (name.startsWith(File.separator)) {
            name = name.substring(1);
        }
        while (name.endsWith(File.separator)) {
            name = name.substring(0, name.length() - 1);
        }

        // If it is not a name of a single sub-directory, bail out.
        if (name.isEmpty() || name.contains(File.separator))
            return null;

        if (name.contains(".")) {
            throw new BasecodeException("The basecode directory name \"" + name + "\" cannot contain dots!");
        }

        // Grab the basecode submission from the regular submissions.
        return foundSubmissions.get(name);
    }

    /**
     * Read entries in the given root directory.
     */
    private String[] readSubmissionRootNames(File rootDirectory) throws ExitException {
        if (!rootDirectory.isDirectory()) {
            throw new AssertionError("Given root is not a directory.");
        }

        String[] fileNames;

        try {
            fileNames = rootDirectory.list();
        } catch (SecurityException exception) {
            throw new RootDirectoryException("Cannot list files of the root directory! " + exception.getMessage(), exception);
        }

        if (fileNames == null) {
            throw new RootDirectoryException("Cannot list files of the root directory!");
        }

        Arrays.sort(fileNames);
        return fileNames;
    }

    /**
     * Check that the given submission entry is not invalid due to exclusion names or bad suffix.
     * @param submissionEntry Entry to check.
     * @return Error message if the entry should be ignored.
     */
    private String isExcludedEntry(File submissionEntry) {
        if (isFileExcluded(submissionEntry)) {
            return "Exclude submission: " + submissionEntry.getName();
        }

        if (submissionEntry.isFile() && !hasValidSuffix(submissionEntry)) {
            return "Ignore submission with invalid suffix: " + submissionEntry.getName();
        }
        return null;
    }

    /**
     * Process the given directory entry as a submission root, the path MUST not be excluded.
     * @param submissionEntry Entry to process.
     * @return The entry converted to a submission.
     * @throws ExitException when an error has been found with the entry.
     */
    private Submission processDirEntry(File submissionEntry) throws ExitException {
        if (isExcludedEntry(submissionEntry) != null) {
            throw new AssertionError("Pre-condition of non-exclusion is violated.");
        }

        String fileName = submissionEntry.getName();
        if (submissionEntry.isDirectory() && options.getSubdirectoryName() != null) {
            // Use subdirectory instead
            submissionEntry = new File(submissionEntry, options.getSubdirectoryName());

            if (!submissionEntry.exists()) {
                throw new SubmissionException(
                        String.format("Submission %s does not contain the given subdirectory '%s'", fileName, options.getSubdirectoryName()));
            }

            if (!submissionEntry.isDirectory()) {
                throw new SubmissionException(String.format("The given subdirectory '%s' is not a directory!", options.getSubdirectoryName()));
            }
        }

        return new Submission(fileName, submissionEntry, parseFilesRecursively(submissionEntry), language, errorCollector);
    }

    /**
     * Process entries in the root directory to check whether they qualify as submissions.
     * @param rootDirectory Root directory being examined.
     * @param fileNames Entries found in the root directory.
     * @return Candidate submissions ordered by their name.
     */
    private Map<String, Submission> processRootDirEntries(File rootDirectory, String[] fileNames) throws ExitException {
        Map<String, Submission> foundSubmissions = new LinkedHashMap<>(fileNames.length); // Capacity is an over-estimate.

        for (String fileName : fileNames) {
            File submissionFile = new File(rootDirectory, fileName);

            String errorMessage = isExcludedEntry(submissionFile);
            if (errorMessage != null) {
                System.out.println(errorMessage);
                continue;
            }

            foundSubmissions.put(fileName, processDirEntry(submissionFile));
        }

        return foundSubmissions;
    }

    /**
     * Checks if a file has a valid suffix for the current language.
     * @param file is the file to check.
     * @return true if the file suffix matches the language.
     */
    private boolean hasValidSuffix(File file) {
        String[] validSuffixes = options.getFileSuffixes();

        // This is the case if either the language frontends or the CLI did not set the valid suffixes array in options
        if (validSuffixes == null || validSuffixes.length == 0) {
            return true;
        }
        return Arrays.stream(validSuffixes).anyMatch(suffix -> file.getName().endsWith(suffix));
    }

    /**
     * Checks if a file is excluded or not.
     */
    private boolean isFileExcluded(File file) {
        return excludedFileNames.stream().anyMatch(excludedName -> file.getName().endsWith(excludedName));
    }

    /**
     * Recursively scan the given directory for nested files. Excluded files and files with an invalid suffix are ignored.
     * <p>
     * If the given file is not a directory, the input will be returned as a singleton list.
     * @param file - File to start the scan from.
     * @return a list of nested files.
     */
    private Collection<File> parseFilesRecursively(File file) {
        if (isFileExcluded(file)) {
            return Collections.emptyList();
        }

        if (file.isFile() && hasValidSuffix(file)) {
            return Collections.singletonList(file);
        }

        String[] nestedFileNames = file.list();

        if (nestedFileNames == null) {
            return Collections.emptyList();
        }

        Collection<File> files = new ArrayList<>();

        for (String fileName : nestedFileNames) {
            files.addAll(parseFilesRecursively(new File(file, fileName)));
        }

        return files;
    }

}