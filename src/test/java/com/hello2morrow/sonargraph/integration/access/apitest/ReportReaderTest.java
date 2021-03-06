/*
 * Sonargraph Integration Access
 * Copyright (C) 2016-2018 hello2morrow GmbH
 * mailto: support AT hello2morrow DOT com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hello2morrow.sonargraph.integration.access.apitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Test;

import com.hello2morrow.sonargraph.integration.access.controller.ControllerAccess;
import com.hello2morrow.sonargraph.integration.access.controller.IModuleInfoProcessor;
import com.hello2morrow.sonargraph.integration.access.controller.ISonargraphSystemController;
import com.hello2morrow.sonargraph.integration.access.controller.ISystemInfoProcessor;
import com.hello2morrow.sonargraph.integration.access.foundation.Result;
import com.hello2morrow.sonargraph.integration.access.foundation.TestFixture;
import com.hello2morrow.sonargraph.integration.access.foundation.TestUtility;
import com.hello2morrow.sonargraph.integration.access.model.AnalyzerExecutionLevel;
import com.hello2morrow.sonargraph.integration.access.model.IAnalyzer;
import com.hello2morrow.sonargraph.integration.access.model.IComponentFilter;
import com.hello2morrow.sonargraph.integration.access.model.IFilter;
import com.hello2morrow.sonargraph.integration.access.model.IIssue;
import com.hello2morrow.sonargraph.integration.access.model.IMetricId;
import com.hello2morrow.sonargraph.integration.access.model.IMetricLevel;
import com.hello2morrow.sonargraph.integration.access.model.IMetricValue;
import com.hello2morrow.sonargraph.integration.access.model.IModule;
import com.hello2morrow.sonargraph.integration.access.model.INamedElement;
import com.hello2morrow.sonargraph.integration.access.model.IPlugin;
import com.hello2morrow.sonargraph.integration.access.model.IResolution;
import com.hello2morrow.sonargraph.integration.access.model.ISoftwareSystem;
import com.hello2morrow.sonargraph.integration.access.model.IWildcardPattern;
import com.hello2morrow.sonargraph.integration.access.model.PluginExecutionPhase;
import com.hello2morrow.sonargraph.integration.access.model.ResolutionType;

public final class ReportReaderTest
{
    @Test
    public void processReportWithCycleGroup()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.TEST_REPORT_INTEGRATION_ACCESS_WITH_CYCLE_GROUP));
        assertTrue(result.toString(), result.isSuccess());
        final ISystemInfoProcessor info = controller.createSystemInfoProcessor();
        assertEquals("Wrong number of issues", 11, info.getIssues(null).size());
    }

    @Test
    public void processReportWithNoIssues()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.TEST_REPORT_WITHOUT_ISSUES));
        assertTrue(result.toString(), result.isSuccess());
        final ISystemInfoProcessor info = controller.createSystemInfoProcessor();
        assertEquals("Wrong number of issues", 0, info.getIssues(null).size());
    }

    @Test
    public void processReportWithoutElements()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.TEST_REPORT_WITHOUT_ELEMENTS));
        assertTrue(result.toString(), result.isSuccess());
        assertEquals("Wrong number of modules", 1, controller.getSoftwareSystem().getModules().size());
    }

    @Test
    public void processReportCreatedWith9_3()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.TEST_REPORT_9_3));
        assertTrue(result.toString(), result.isSuccess());
        assertEquals("Wrong number of modules", 4, controller.getSoftwareSystem().getModules().size());
    }

    private Predicate<IIssue> createUnresolvedIssueFilter(final String categoryPresentationName)
    {
        return (final IIssue i) -> !i.hasResolution() && i.getIssueType().getCategory().getPresentationName().equals(categoryPresentationName);
    }

    @Test
    public void processCSharpReport() throws Exception
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.CSHARP_REPORT));
        assertTrue(result.toString(), result.isSuccess());
        assertEquals("Wrong number of modules", 4, controller.getSoftwareSystem().getModules().size());

        final ISystemInfoProcessor info = controller.createSystemInfoProcessor();
        assertEquals("Wrong number of cycle issues", 86, info.getIssues(createUnresolvedIssueFilter("Cycle Group")).size());
        assertEquals("Wrong number of duplicates", 200, info.getIssues(createUnresolvedIssueFilter("Duplicate Code")).size());
        assertEquals("Wrong number of script based issues", 109, info.getIssues(createUnresolvedIssueFilter("Script Based")).size());
        assertEquals("Wrong number of threshold violations", 47, info.getIssues(createUnresolvedIssueFilter("Threshold Violation")).size());
        assertEquals("Wrong number of workspace issues", 1, info.getIssues(createUnresolvedIssueFilter("Workspace")).size());

        assertEquals("Wrong number of resolutions", 1, info.getResolutions((final IResolution r) -> r.getType() == ResolutionType.TODO).size());

        final IModule nhibernate = controller.getSoftwareSystem().getModule("NHibernate")
                .orElseThrow(() -> new Exception("Module NHibernate not found"));
        final IModuleInfoProcessor moduleProcessor = controller.createModuleInfoProcessor(nhibernate);
        assertEquals("Wrong number of namespace cycle groups", 2,
                moduleProcessor.getIssues((final IIssue i) -> i.getIssueType().getPresentationName().equals("Namespace Cycle Group")).size());
    }

    @Test
    public void processCppReport() throws Exception
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.CPP_REPORT));
        assertTrue(result.toString(), result.isSuccess());
        assertEquals("Wrong number of modules", 1, controller.getSoftwareSystem().getModules().size());

        final ISystemInfoProcessor info = controller.createSystemInfoProcessor();
        assertEquals("Wrong number of cycle issues", 5, info.getIssues(createUnresolvedIssueFilter("Cycle Group")).size());
        assertEquals("Wrong number of duplicates", 4, info.getIssues(createUnresolvedIssueFilter("Duplicate Code")).size());
        assertEquals("Wrong number of 'no definition found'", 22,
                info.getIssues((final IIssue i) -> i.getIssueType().getPresentationName().equals("No definition found")).size());
        assertEquals("Wrong number of workspace issues", 22, info.getIssues(createUnresolvedIssueFilter("Workspace")).size());

        final IModule generic = controller.getSoftwareSystem().getModule("Generic").orElseThrow(() -> new Exception("Module 'Generic' not found"));
        final Map<String, INamedElement> cppMemberFunctions = TestUtility.getFqNameToNamedElement(generic, "CppMemberFunction");
        assertTrue("no elements found", cppMemberFunctions.size() > 0);

        //printLogicalElements(cppMemberFunctions);

        final String functionFqName = "Workspace:Generic:./src:kernel:gsingltn:(header)./src/../include/gsingltn.h:class G_Singleton<T1>:G_Singleton(const char*, const char*)";
        assertNotNull("Logical element not found", cppMemberFunctions.get(functionFqName));
        final IModuleInfoProcessor moduleInfoProcessor = controller.createModuleInfoProcessor(generic);
        final IMetricLevel level = moduleInfoProcessor.getMetricLevel("Routine").get();
        final IMetricId metricId = moduleInfoProcessor.getMetricId(level, "CoreParameters").get();
        final IMetricValue value = moduleInfoProcessor.getMetricValueForElement(metricId, level, functionFqName).get();

        assertEquals("Wrong metric value", 2, value.getValue().intValue());
    }

    @Test
    public void processCppReportWithLogicalNamespaces() throws Exception
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.CPP_REPORT_HILO));
        assertTrue(result.toString(), result.isSuccess());
        assertEquals("Wrong number of modules", 4, controller.getSoftwareSystem().getModules().size());

        final ISystemInfoProcessor info = controller.createSystemInfoProcessor();
        assertEquals("Wrong number of duplicates", 4, info.getIssues(createUnresolvedIssueFilter("Duplicate Code")).size());
        assertEquals("Wrong number of 'no definition found'", 5,
                info.getIssues((final IIssue i) -> i.getIssueType().getPresentationName().equals("No definition found")).size());
        assertEquals("Wrong number of workspace issues", 7, info.getIssues(createUnresolvedIssueFilter("Workspace")).size());

        final IModule browser = controller.getSoftwareSystem().getModule("Browser").orElseThrow(() -> new Exception("Module 'Browser' not found"));
        final Map<String, INamedElement> cppNamespaces = TestUtility.getFqNameToNamedElement(browser, "CPlusPlusLogicalModuleNamespace");
        assertTrue("no elements found", cppNamespaces.size() > 0);

        //printLogicalElements(cppMemberFunctions);

        final IModuleInfoProcessor moduleInfoProcessor = controller.createModuleInfoProcessor(browser);
        final IMetricLevel level = moduleInfoProcessor.getMetricLevel("CppNamespace").orElseThrow(() -> new Exception("Metric level not found"));
        final IMetricId metricId = moduleInfoProcessor.getMetricId(level, "CoreTypesModule").orElseThrow(() -> new Exception("Metric not found"));
        assertEquals("Wrong number of types for module namespace", 22, moduleInfoProcessor
                .getMetricValueForElement(metricId, level, "Logical module namespaces:Browser:Hilo:AsyncLoader").get().getValue().intValue());
    }

    @Test
    public void processClassFileIssuesReport()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.ALARM_CLOCK_CLASS_FILE_ISSUES_REPORT));
        assertTrue(result.toString(), result.isSuccess());
    }

    @Test
    public void testReportStandard()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.TEST_REPORT_STANDARD));
        assertTrue(result.toString(), result.isSuccess());
    }

    @Test
    public void testDirectoryIssues()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.TEST_REPORT_WITH_DERIVED));
        assertTrue(result.toString(), result.isSuccess());
        assertEquals("Wrong number of modules", 2, controller.getSoftwareSystem().getModules().size());

        final Map<String, Map<String, List<IIssue>>> moduleToIssues = new HashMap<>();

        final ISystemInfoProcessor systemInfoProcessor = controller.createSystemInfoProcessor();
        for (final IModule nextModule : systemInfoProcessor.getModules().values())
        {
            final IModuleInfoProcessor nextModuleInfoProcessor = controller.createModuleInfoProcessor(nextModule);
            final Map<String, List<IIssue>> issueMap = nextModuleInfoProcessor
                    .getIssuesForDirectories(issue -> !issue.isIgnored() && !"Workspace".equals(issue.getIssueType().getCategory().getName()));
            moduleToIssues.put(nextModule.getName(), issueMap);
        }

        final Map<String, List<IIssue>> m1IssueMap = moduleToIssues.get("M1");
        assertNotNull("Module M1 not found", m1IssueMap);
        assertTrue("3 directories with issues expected for module M1", m1IssueMap.size() == 3);
        final Map<String, List<IIssue>> m5IssueMap = moduleToIssues.get("M5");
        assertNotNull("Module M5 not found", m5IssueMap);
        assertTrue("1 directory with issue expected for module M5", m5IssueMap.size() == 1);
    }

    @Test
    public void testReportWithPackageTodo()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.TEST_REPORT_WITH_PACKAGE_TODO));
        assertTrue(result.toString(), result.isSuccess());

        final Map<IModuleInfoProcessor, Map<String, List<IIssue>>> moduleToIssues = new HashMap<>();

        final ISystemInfoProcessor systemInfoProcessor = controller.createSystemInfoProcessor();
        for (final IModule nextModule : systemInfoProcessor.getModules().values())
        {
            final IModuleInfoProcessor nextModuleInfoProcessor = controller.createModuleInfoProcessor(nextModule);
            final Map<String, List<IIssue>> issueMap = nextModuleInfoProcessor
                    .getIssuesForDirectories(issue -> !issue.isIgnored() && !"Workspace".equals(issue.getIssueType().getCategory().getName()));
            moduleToIssues.put(nextModuleInfoProcessor, issueMap);
        }

        assertTrue("1 module expected", moduleToIssues.size() == 1);

        for (final Entry<IModuleInfoProcessor, Map<String, List<IIssue>>> nextEntry : moduleToIssues.entrySet())
        {
            final Map<String, List<IIssue>> issueMap = nextEntry.getValue();
            assertTrue("1 directory with issue expected", issueMap.size() == 1);
            for (final Entry<String, List<IIssue>> nextIssueMapEntry : issueMap.entrySet())
            {
                for (final IIssue nextIssue : nextIssueMapEntry.getValue())
                {
                    final IResolution nextResolution = nextEntry.getKey().getResolution(nextIssue);
                    assertNotNull("Resolution expected", nextResolution);
                }
            }
        }
    }

    @Test
    public void processReportWithPluginInfoAndAnalyzerExecutionLevel()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.REPORT_WITH_PLUGINS));
        assertTrue(result.toString(), result.isSuccess());

        final ISystemInfoProcessor systemInfoProcessor = controller.createSystemInfoProcessor();
        final ISoftwareSystem softwareSystem = systemInfoProcessor.getSoftwareSystem();
        final Map<String, IPlugin> plugins = softwareSystem.getPlugins();
        assertEquals("Wrong number of plugins", 2, plugins.size());

        final IPlugin swagger = plugins.get("SwaggerPlugin");
        assertNotNull("Swagger plugin not found", swagger);
        assertEquals("Wrong name", "SwaggerPlugin", swagger.getName());
        assertEquals("Wrong presentation name", "Swagger Plugin", swagger.getPresentationName());
        assertEquals("Wrong description", "Plugin that exposes webresources and dependencies between them.", swagger.getDescription());
        assertEquals("Wrong version", "9.9.2.533_2018-12-12", swagger.getVersion());
        assertEquals("Wrong vendor", "hello2morrow GmbH", swagger.getVendor());
        assertFalse("Must not be enabled", swagger.isEnabled());
        assertTrue("Must not be executed", swagger.getActiveExecutionPhases().isEmpty());
        assertEquals("Wrong type of available execution phase", EnumSet.of(PluginExecutionPhase.MODEL), swagger.getSupportedExecutionPhases());
        assertTrue("Must be licensed", swagger.isLicensed());

        final IPlugin spotbugsPlugin = plugins.get("SpotbugsPlugin");
        assertNotNull("Spotbugs plugin not found", spotbugsPlugin);
        assertEquals("Wrong type of available execution phases", EnumSet.of(PluginExecutionPhase.ANALYZER),
                spotbugsPlugin.getSupportedExecutionPhases());

        assertEquals("Wrong analyzer execution level", AnalyzerExecutionLevel.ADVANCED,
                systemInfoProcessor.getSoftwareSystem().getAnalyzerExecutionLevel());
        final Map<String, IAnalyzer> analyzers = softwareSystem.getAnalyzers();
        final IAnalyzer spotbugs = analyzers.get("SpotbugsPlugin");
        assertNotNull("Spotbugs analyzer must exist", spotbugs);
        assertTrue("Must be licensed", spotbugs.isLicensed());
        assertFalse("Must not be executed", spotbugs.isExecuted());
    }

    @Test
    public void processWorkspaceFilters()
    {
        final ISonargraphSystemController controller = ControllerAccess.createController();
        final Result result = controller.loadSystemReport(new File(TestFixture.ALARM_CLOCK_WITH_WORKSPACE_FILTERS));
        assertTrue(result.toString(), result.isSuccess());

        final ISystemInfoProcessor systemInfoProcessor = controller.createSystemInfoProcessor();
        final ISoftwareSystem softwareSystem = systemInfoProcessor.getSoftwareSystem();
        {
            final Optional<IFilter> workspaceFilterOpt = softwareSystem.getWorkspaceFilter();
            assertTrue("Workspace filter must exist", workspaceFilterOpt.isPresent());
            final IFilter workspaceFilter = workspaceFilterOpt.get();
            assertEquals("Wrong description", "Exclude files from the workspace", workspaceFilter.getDescription());
            assertEquals("Wrong information", "Excluded 0 files(s)", workspaceFilter.getInformation());
            assertEquals("Wrong number of excluded elements", 0, workspaceFilter.getNumberOfExcludedElements());
            final List<IWildcardPattern> includePatterns = workspaceFilter.getIncludePatterns();
            assertEquals("Wrong number of include patterns", 1, includePatterns.size());
            final IWildcardPattern include = includePatterns.get(0);
            assertEquals("Wrong pattern", "**", include.getPattern());
            assertEquals("Wrong number of matched elements", 21, include.getNumberOfMatches());
            final List<IWildcardPattern> excludePatterns = workspaceFilter.getExcludePatterns();
            assertEquals("Wrong number of exclude patterns", 1, excludePatterns.size());
            final IWildcardPattern exclude = excludePatterns.get(0);
            assertEquals("Wrong pattern", "**/bla/**", exclude.getPattern());
            assertEquals("Wrong number of matched elements", 0, exclude.getNumberOfMatches());
        }
        {
            final Optional<IComponentFilter> productionCodeFilterOpt = softwareSystem.getProductionCodeFilter();
            assertTrue("Production code filter must exist", productionCodeFilterOpt.isPresent());
            final IComponentFilter productionCodeFilter = productionCodeFilterOpt.get();
            assertEquals("Wrong description", "Exclude internal components containing test code", productionCodeFilter.getDescription());
            assertEquals("Wrong information", "Excluded 1 internal component(s) (processed 10)", productionCodeFilter.getInformation());
            assertEquals("Wrong number of included elements", 9, productionCodeFilter.getNumberOfIncludedElements());
            assertEquals("Wrong number of excluded elements", 1, productionCodeFilter.getNumberOfExcludedElements());
            final List<IWildcardPattern> includePatterns = productionCodeFilter.getIncludePatterns();
            assertEquals("Wrong number of include patterns", 1, includePatterns.size());
            final IWildcardPattern include = includePatterns.get(0);
            assertEquals("Wrong pattern", "**", include.getPattern());
            assertEquals("Wrong number of matched elements", 10, include.getNumberOfMatches());
            final List<IWildcardPattern> excludePatterns = productionCodeFilter.getExcludePatterns();
            assertEquals("Wrong number of exclude patterns", 1, excludePatterns.size());
            final IWildcardPattern exclude = excludePatterns.get(0);
            assertEquals("Wrong number of matched elements", 1, exclude.getNumberOfMatches());
            assertEquals("Wrong pattern", "**/test/java/**1", exclude.getPattern());
        }
        {
            final Optional<IComponentFilter> issueFilterOpt = softwareSystem.getIssueFilter();
            assertTrue("Issue filter must exist", issueFilterOpt.isPresent());
            final IComponentFilter issueFilter = issueFilterOpt.get();
            assertEquals("Wrong description", "Ignore issues of internal components containing legacy/generated code", issueFilter.getDescription());
            assertEquals("Wrong information", "Ignoring issues of 2 internal component(s) (processed 9)", issueFilter.getInformation());
            assertEquals("Wrong number of included elements", 7, issueFilter.getNumberOfIncludedElements());
            assertEquals("Wrong number of excluded elements", 2, issueFilter.getNumberOfExcludedElements());
            final List<IWildcardPattern> includePatterns = issueFilter.getIncludePatterns();
            assertEquals("Wrong number of include patterns", 1, includePatterns.size());
            final IWildcardPattern include = includePatterns.get(0);
            assertEquals("Wrong pattern", "**/*", include.getPattern());
            assertEquals("Wrong number of matched elements", 9, include.getNumberOfMatches());
            final List<IWildcardPattern> excludePatterns = issueFilter.getExcludePatterns();
            assertEquals("Wrong number of exclude patterns", 1, excludePatterns.size());
            final IWildcardPattern exclude = excludePatterns.get(0);
            assertEquals("Wrong number of matched elements", 2, exclude.getNumberOfMatches());
            assertEquals("Wrong pattern", "**/test/java/**", exclude.getPattern());
        }
    }
}
