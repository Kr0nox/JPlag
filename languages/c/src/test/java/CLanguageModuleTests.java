import de.jplag.c.CLanguage;
import de.jplag.c.CTokenType;
import de.jplag.testutils.LanguageModuleTest;
import de.jplag.testutils.datacollector.TestDataCollector;
import de.jplag.testutils.datacollector.TestSourceIgnoredLinesCollector;

public class CLanguageModuleTests extends LanguageModuleTest {
    public CLanguageModuleTests() {
        super(new CLanguage(), CTokenType.class);
    }

    @Override
    protected void collectTestData(TestDataCollector collector) {
        collector.testFile("minimal.c", "progpedia-00081_00003.c").testSourceCoverage();
    }

    @Override
    protected void configureIgnoredLines(TestSourceIgnoredLinesCollector collector) {
        collector.ignoreLinesByPrefix("#");
        collector.ignoreLinesByContains("//ignore");
    }
}
